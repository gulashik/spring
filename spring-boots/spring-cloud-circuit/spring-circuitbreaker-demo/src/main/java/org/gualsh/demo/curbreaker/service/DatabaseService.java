package org.gualsh.demo.curbreaker.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gualsh.demo.curbreaker.model.User;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Сервис для работы с базой данных через Circuit Breaker.
 *
 * <h3>Образовательный момент:</h3>
 * <p>
 * Данный сервис имитирует работу с базой данных и демонстрирует использование
 * Circuit Breaker для защиты от проблем с БД. В реальных приложениях это могут быть
 * проблемы с connection pool, медленные запросы, блокировки таблиц.
 * </p>
 *
 * <p><strong>Типичные проблемы с БД, от которых защищает Circuit Breaker:</strong></p>
 * <ul>
 *   <li>Исчерпание connection pool</li>
 *   <li>Медленные запросы (N+1 problem, missing indexes)</li>
 *   <li>Блокировки и deadlocks</li>
 *   <li>Недоступность БД (network issues, maintenance)</li>
 * </ul>
 *
 * <p><strong>Важные принципы для операций с БД:</strong></p>
 * <ul>
 *   <li>Операции чтения - можно предоставлять fallback данные</li>
 *   <li>Операции записи - осторожность с fallback, лучше выбросить исключение</li>
 *   <li>Bulk операции - защита от cascade failures</li>
 *   <li>Правильная настройка timeouts</li>
 * </ul>
 *
 * <p><strong>Пример использования:</strong></p>
 * <pre>{@code
 * @Autowired
 * private DatabaseService dbService;
 *
 * // Поиск пользователя
 * User user = dbService.findById(1L);
 *
 * // Получение всех пользователей
 * List<User> users = dbService.findAll();
 *
 * // Создание пользователя
 * User newUser = User.builder()
 *     .name("Иван Иванов")
 *     .email("ivan@example.com")
 *     .build();
 * User saved = dbService.save(newUser);
 *
 * // Удаление
 * boolean deleted = dbService.deleteById(1L);
 * }</pre>
 *
 * <p><strong>Подводные камни при работе с БД через Circuit Breaker:</strong></p>
 * <ul>
 *   <li>Транзакции - Circuit Breaker может сработать в середине транзакции</li>
 *   <li>Readonly операции vs write операции - разные стратегии fallback</li>
 *   <li>Кэширование - fallback данные должны быть актуальными</li>
 *   <li>Connection leaks - важно правильно освобождать ресурсы</li>
 * </ul>
 *
 * @author Educational Demo
 * @version 1.0.0
 * @see CircuitBreaker
 * @see User
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseService {

    /**
     * Circuit Breaker для операций с базой данных.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * @Qualifier используется для инжекции конкретного Circuit Breaker bean,
     * так как в приложении может быть несколько Circuit Breaker для разных сервисов.
     * </p>
     */
    @Qualifier("databaseCircuitBreaker")
    private final CircuitBreaker circuitBreaker;

    /**
     * Имитация in-memory базы данных для демонстрации.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * В реальном приложении здесь был бы JPA Repository, MyBatis Mapper,
     * или другой DAO layer. ConcurrentHashMap используется для thread-safety
     * в многопоточной среде.
     * </p>
     */
    private final Map<Long, User> database = new ConcurrentHashMap<>();

    /**
     * Инициализация тестовых данных при создании сервиса.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * Блок инициализации выполняется при создании экземпляра класса.
     * В реальном приложении данные бы загружались из реальной БД.
     * </p>
     */
    {
        initializeTestData();
    }

    /**
     * Поиск пользователя по ID с Circuit Breaker защитой.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * Метод демонстрирует использование CircuitBreaker.executeSupplier() для
     * синхронных операций с БД. При срабатывании Circuit Breaker возвращается
     * cached пользователь через recover() метод.
     * </p>
     *
     * <p><strong>Важные аспекты реализации:</strong></p>
     * <ul>
     *   <li>executeSupplier() - для операций, возвращающих значение</li>
     *   <li>recover() - fallback логика при ошибках</li>
     *   <li>Логирование на разных уровнях для мониторинга</li>
     *   <li>Graceful degradation с meaningful данными</li>
     * </ul>
     *
     * <p><strong>Альтернативные подходы:</strong></p>
     * <ul>
     *   <li>@CircuitBreaker аннотация на методе (декларативный подход)</li>
     *   <li>Reactive approach с CircuitBreakerOperator</li>
     *   <li>Manual circuit breaker state management</li>
     * </ul>
     *
     * @param id идентификатор пользователя
     * @return пользователь или fallback объект если не найден
     */
    public User findById(Long id) {
        log.debug("Поиск пользователя с ID: {}", id);

        return circuitBreaker.executeSupplier(() -> {
            // Симуляция работы с БД с возможными проблемами
            simulateDatabaseOperation("findById");

            User user = database.get(id);
            if (user != null) {
                log.debug("Пользователь найден: {} ({})", user.getName(), user.getEmail());
            } else {
                log.debug("Пользователь с ID {} не найден в базе данных", id);
            }

            return user;
        }).recover(throwable -> {
            log.warn("Database Circuit Breaker fallback для пользователя {}: {}",
                id, throwable.getMessage());

            // Fallback: возвращаем cached данные или системного пользователя
            return createFallbackUser(id);
        });
    }

    /**
     * Получение всех пользователей с Circuit Breaker защитой.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * Для операций, возвращающих коллекции, важно предоставлять разумные fallback
     * значения. Пустой список часто предпочтительнее исключения, но в данном случае
     * мы возвращаем минимальный набор cached данных.
     * </p>
     *
     * <p><strong>Стратегии fallback для коллекций:</strong></p>
     * <ul>
     *   <li>Пустая коллекция - самый безопасный вариант</li>
     *   <li>Cached данные - лучший UX, но могут быть устаревшими</li>
     *   <li>Subset of data - часть данных лучше, чем ничего</li>
     *   <li>Placeholder данные - показывают проблему пользователю</li>
     * </ul>
     *
     * @return список всех пользователей или fallback список
     */
    public List<User> findAll() {
        log.debug("Получение списка всех пользователей");

        return circuitBreaker.executeSupplier(() -> {
            simulateDatabaseOperation("findAll");

            List<User> users = new ArrayList<>(database.values());
            // Сортируем по ID для предсказуемого порядка
            users.sort(Comparator.comparing(User::getId));

            log.debug("Найдено {} пользователей в базе данных", users.size());
            return users;

        }).recover(throwable -> {
            log.warn("Database Circuit Breaker fallback для списка пользователей: {}",
                throwable.getMessage());

            // Fallback: возвращаем минимальный набор cached данных
            return createFallbackUserList();
        });
    }

    /**
     * Сохранение пользователя с Circuit Breaker защитой.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * Операции записи требуют особого внимания при использовании Circuit Breaker.
     * Важно различать временные и постоянные ошибки, и НЕ создавать ложное
     * впечатление успеха при fallback.
     * </p>
     *
     * <p><strong>Принципы для write операций:</strong></p>
     * <ul>
     *   <li>Не делать fallback с фиктивными данными</li>
     *   <li>Лучше выбросить исключение, чем создать ложное впечатление</li>
     *   <li>Рассмотреть retry pattern для критических операций</li>
     *   <li>Использовать queue для eventual consistency</li>
     * </ul>
     *
     * <p><strong>Альтернативные стратегии:</strong></p>
     * <ul>
     *   <li>Message queue для async processing</li>
     *   <li>Optimistic locking with retry</li>
     *   <li>Write-behind caching</li>
     *   <li>Event sourcing approach</li>
     * </ul>
     *
     * @param user пользователь для сохранения
     * @return сохраненный пользователь
     * @throws RuntimeException если не удалось сохранить
     */
    public User save(User user) {
        log.debug("Сохранение пользователя: {} ({})", user.getName(), user.getEmail());

        return circuitBreaker.executeSupplier(() -> {
            simulateDatabaseOperation("save");

            // Генерируем ID если его нет (имитация auto-increment)
            if (user.getId() == null) {
                user.setId(generateNextId());
            }

            // Создаем копию для сохранения (defensive copying)
            User userToSave = User.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .additionalInfo(user.getAdditionalInfo())
                .build();

            database.put(userToSave.getId(), userToSave);
            log.info("Пользователь сохранен успешно с ID: {}", userToSave.getId());

            return userToSave;

        }).recover(throwable -> {
            log.error("Ошибка сохранения пользователя '{}' через Circuit Breaker: {}",
                user.getName(), throwable.getMessage());

            // Для операций записи НЕ делаем fallback с фиктивными данными
            // Лучше честно сообщить об ошибке
            throw new RuntimeException(
                "Невозможно сохранить пользователя: сервис базы данных временно недоступен. " +
                    "Попробуйте повторить операцию через несколько минут.",
                throwable
            );
        });
    }

    /**
     * Удаление пользователя с Circuit Breaker защитой.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * Операции удаления также требуют осторожности с fallback логикой.
     * Неудачное удаление не должно возвращать false, так как это может
     * создать ложное впечатление о состоянии данных.
     * </p>
     *
     * <p><strong>Стратегии для delete операций:</strong></p>
     * <ul>
     *   <li>Идемпотентность - повторное удаление не должно вызывать ошибку</li>
     *   <li>Soft delete vs hard delete</li>
     *   <li>Audit trail для отслеживания операций</li>
     *   <li>Cascade delete considerations</li>
     * </ul>
     *
     * @param id идентификатор пользователя для удаления
     * @return true если удален успешно
     * @throws RuntimeException если операция не может быть выполнена
     */
    public boolean deleteById(Long id) {
        log.debug("Удаление пользователя с ID: {}", id);

        return circuitBreaker.executeSupplier(() -> {
            simulateDatabaseOperation("deleteById");

            User removed = database.remove(id);
            boolean success = removed != null;

            if (success) {
                log.info("Пользователь {} ('{}') удален успешно", id, removed.getName());
            } else {
                log.debug("Пользователь {} не найден для удаления (уже удален или не существовал)", id);
                // Для идемпотентности возвращаем true даже если пользователь не найден
                success = true;
            }

            return success;

        }).recover(throwable -> {
            log.error("Ошибка удаления пользователя {} через Circuit Breaker: {}",
                id, throwable.getMessage());

            // Для операций удаления не возвращаем false, так как это может ввести в заблуждение
            // Лучше выбросить исключение и позволить клиенту решить, что делать
            throw new RuntimeException(
                "Невозможно удалить пользователя: сервис базы данных временно недоступен. " +
                    "Попробуйте повторить операцию через несколько минут.",
                throwable
            );
        });
    }

    /**
     * Поиск пользователей по email домену.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * Демонстрация более сложной бизнес-логики с Circuit Breaker.
     * Показывает как обрабатывать поисковые запросы с fallback.
     * </p>
     *
     * @param domain домен email для поиска
     * @return список пользователей с указанным доменом
     */
    public List<User> findByEmailDomain(String domain) {
        log.debug("Поиск пользователей с email доменом: {}", domain);

        return circuitBreaker.executeSupplier(() -> {
            simulateDatabaseOperation("findByEmailDomain");

            List<User> result = database.values().stream()
                .filter(user -> user.getEmail().endsWith("@" + domain))
                .sorted(Comparator.comparing(User::getId))
                .toList();

            log.debug("Найдено {} пользователей с доменом {}", result.size(), domain);
            return result;

        }).recover(throwable -> {
            log.warn("Database Circuit Breaker fallback для поиска по домену {}: {}",
                domain, throwable.getMessage());

            // Для поисковых запросов возвращаем пустой список
            return Collections.emptyList();
        });
    }

    /**
     * Проверка доступности базы данных.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * Health check операции полезны для мониторинга и могут использоваться
     * системами оркестрации (Kubernetes, Docker Swarm) для принятия решений
     * о маршрутизации трафика и автоматическом восстановлении.
     * </p>
     *
     * <p><strong>Best practices для health checks:</strong></p>
     * <ul>
     *   <li>Легковесные операции - не нагружать систему</li>
     *   <li>Быстрое выполнение - timeout должен быть маленьким</li>
     *   <li>Meaningful результат - не просто ping, а реальная проверка</li>
     *   <li>Graceful degradation - не падать при проблемах</li>
     * </ul>
     *
     * @return true если БД доступна и работает корректно
     */
    public boolean isHealthy() {
        try {
            return circuitBreaker.executeSupplier(() -> {
                simulateDatabaseOperation("healthCheck");

                // Простая проверка - подсчет количества записей
                int recordCount = database.size();
                log.debug("Database health check: {} записей в базе", recordCount);

                return recordCount >= 0; // Всегда true для демонстрации
            }).recover(throwable -> {
                log.warn("Database health check failed: {}", throwable.getMessage());
                return false;
            });
        } catch (Exception e) {
            log.error("Unexpected error during database health check", e);
            return false;
        }
    }

    /**
     * Получение статистики базы данных.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * Демонстрация как можно получать метрики и статистику через Circuit Breaker.
     * Полезно для monitoring и capacity planning.
     * </p>
     *
     * @return Map с статистикой БД
     */
    public Map<String, Object> getDatabaseStats() {
        return circuitBreaker.executeSupplier(() -> {
            simulateDatabaseOperation("getDatabaseStats");

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", database.size());
            stats.put("emailDomains", database.values().stream()
                .map(user -> user.getEmail().substring(user.getEmail().indexOf("@") + 1))
                .distinct()
                .count());
            stats.put("lastUpdate", java.time.LocalDateTime.now());

            return stats;
        }).recover(throwable -> {
            log.warn("Database stats Circuit Breaker fallback: {}", throwable.getMessage());

            // Fallback статистика
            Map<String, Object> fallbackStats = new HashMap<>();
            fallbackStats.put("status", "unavailable");
            fallbackStats.put("message", "Database temporarily unavailable");
            fallbackStats.put("lastUpdate", java.time.LocalDateTime.now());

            return fallbackStats;
        });
    }

    /**
     * Имитация операций с базой данных с возможными ошибками.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * Этот метод симулирует реальные проблемы БД для образовательных целей.
     * В реальном приложении такие проблемы происходят непредсказуемо из-за:
     * network issues, high load, resource contention, hardware failures.
     * </p>
     *
     * <p><strong>Типы симулируемых проблем:</strong></p>
     * <ul>
     *   <li>Connection timeout - проблемы с сетью</li>
     *   <li>Lock timeout - конкуренция за ресурсы</li>
     *   <li>Connection pool exhausted - превышение лимитов</li>
     *   <li>Slow queries - неоптимальные запросы</li>
     * </ul>
     *
     * @param operation название операции для логирования
     * @throws RuntimeException при симуляции ошибок БД
     */
    private void simulateDatabaseOperation(String operation) {
        try {
            // Симуляция нормального времени выполнения запроса (50-200ms)
            int delay = ThreadLocalRandom.current().nextInt(50, 200);
            Thread.sleep(delay);

            // Симуляция случайных ошибок (5% вероятность)
            if (ThreadLocalRandom.current().nextDouble() < 0.05) {
                // Различные типы ошибок БД
                double errorType = ThreadLocalRandom.current().nextDouble();

                if (errorType < 0.3) {
                    throw new RuntimeException("Database connection timeout: Unable to acquire connection from pool");
                } else if (errorType < 0.6) {
                    throw new RuntimeException("Database lock timeout: Lock wait timeout exceeded");
                } else if (errorType < 0.8) {
                    throw new RuntimeException("Database connection pool exhausted: Maximum pool size reached");
                } else {
                    throw new RuntimeException("Database query timeout: Query execution time exceeded limit");
                }
            }

            // Симуляция медленных запросов (2% вероятность)
            if (ThreadLocalRandom.current().nextDouble() < 0.02) {
                log.warn("Обнаружен медленный запрос БД для операции: {} (выполняется > 3s)", operation);
                Thread.sleep(4000); // больше timeout Circuit Breaker
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Database operation interrupted: " + operation, e);
        }
    }

    /**
     * Создание fallback пользователя для graceful degradation.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * Fallback данные должны быть meaningful и понятными для пользователя.
     * Они должны ясно указывать на временную природу проблемы.
     * </p>
     *
     * @param id идентификатор запрашиваемого пользователя
     * @return fallback пользователь с информативными данными
     */
    private User createFallbackUser(Long id) {
        return User.builder()
            .id(id)
            .name("Пользователь временно недоступен")
            .email("unavailable@system.local")
            .additionalInfo("Данные временно недоступны из-за проблем с базой данных. " +
                "Попробуйте обновить страницу через несколько минут.")
            .build();
    }

    /**
     * Создание fallback списка пользователей.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * Для списков можно возвращать либо пустой список, либо минимальный набор
     * cached данных. Выбор зависит от бизнес-требований.
     * </p>
     *
     * @return минимальный список с системным пользователем
     */
    private List<User> createFallbackUserList() {
        return Arrays.asList(
            User.builder()
                .id(1L)
                .name("Системный пользователь")
                .email("system@fallback.local")
                .additionalInfo("Отображаются кэшированные данные из-за временных проблем с БД")
                .build()
        );
    }

    /**
     * Генерация следующего ID для новых пользователей.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * В реальном приложении ID генерируется базой данных (AUTO_INCREMENT, SEQUENCE).
     * Здесь мы имитируем эту логику для демонстрационных целей.
     * </p>
     *
     * @return новый уникальный ID
     */
    private Long generateNextId() {
        return database.keySet().stream()
            .mapToLong(Long::longValue)
            .max()
            .orElse(0L) + 1;
    }

    /**
     * Инициализация тестовых данных для демонстрации.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * Тестовые данные помогают сразу начать тестирование приложения
     * без необходимости создания пользователей вручную.
     * </p>
     */
    private void initializeTestData() {
        database.put(1L, User.builder()
            .id(1L)
            .name("Иван Петров")
            .email("ivan.petrov@example.com")
            .additionalInfo("Администратор системы")
            .build());

        database.put(2L, User.builder()
            .id(2L)
            .name("Мария Сидорова")
            .email("maria.sidorova@company.ru")
            .additionalInfo("Менеджер проекта")
            .build());

        database.put(3L, User.builder()
            .id(3L)
            .name("Алексей Иванов")
            .email("alexey.ivanov@company.ru")
            .additionalInfo("Ведущий разработчик")
            .build());

        database.put(4L, User.builder()
            .id(4L)
            .name("Елена Козлова")
            .email("elena.kozlova@gmail.com")
            .additionalInfo("Аналитик данных")
            .build());

        log.info("Инициализированы тестовые данные: {} пользователей", database.size());
    }
}