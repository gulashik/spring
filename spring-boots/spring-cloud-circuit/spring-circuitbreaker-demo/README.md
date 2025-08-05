# Spring Cloud Circuit Breaker

mvn clean install


mvn spring-boot:run


java -jar target/spring-circuit-breaker-demo-1.0.0.jar



Проверка запуска
Приложение будет доступно по адресу: http://localhost:8080
Информация о приложении: http://localhost:8080/api/info

# Получение поста (работает через jsonplaceholder.typicode.com)
GET http://localhost:8080/api/external/posts/1

# Получение всех постов
GET http://localhost:8080/api/external/posts


# Получение пользователя
GET http://localhost:8080/api/users/1

# Получение всех пользователей
GET http://localhost:8080/api/users

# Создание пользователя
POST http://localhost:8080/api/users
Content-Type: application/json

{
"name": "Тест Пользователь",
"email": "test@example.com",
"additionalInfo": "Тестовые данные"
}


# Отправка email
POST http://localhost:8080/api/email/send
Content-Type: application/json

{
"to": "recipient@example.com",
"from": "sender@example.com",
"subject": "Тестовое сообщение",
"body": "Это тестовое сообщение для проверки Circuit Breaker"
}

# Health check (включая Circuit Breakers)
GET http://localhost:8080/actuator/health

# Состояние Circuit Breakers
GET http://localhost:8080/actuator/circuitbreakers

# Метрики Prometheus
GET http://localhost:8080/actuator/prometheus

# Все доступные endpoints
GET http://localhost:8080/actuator

GET http://localhost:8080/api/health/services


Мониторинг и метрики
Логирование
Приложение выводит подробные логи работы Circuit Breaker:

Изменения состояния (CLOSED → OPEN → HALF_OPEN)
Успешные и неудачные вызовы
Срабатывания fallback механизмов

Метрики
Доступны метрики Prometheus для:

Состояния Circuit Breaker
Количества вызовов
Времени выполнения
Частоты ошибок

🔧 Настройка в продакшене
Важные рекомендации

Timeout'ы: Circuit Breaker timeout должен быть больше HTTP timeout
Мониторинг: Настройте алерты на изменения состояния
Fallback: Реализуйте meaningful fallback данные
Тестирование: Регулярно тестируйте fallback сценарии


Образовательные моменты
Ключевые концепции

Состояния Circuit Breaker: CLOSED, OPEN, HALF_OPEN
Fallback стратегии: graceful degradation
Метрики и мониторинг: важность отслеживания
Конфигурация: правильная настройка параметров

Типичные ошибки

Неправильные timeout'ы - могут привести к ложным срабатываниям
Плохие fallback'и - null или исключения вместо разумных значений
Игнорирование мониторинга - невозможность диагностики проблем
Один размер для всех - разные сервисы требуют разных настроек




