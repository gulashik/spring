# Spring WebClient Demo Project

Комплексный проект для демонстрации всех возможностей Spring WebClient с практическими примерами, best practices и real-world сценариями использования.

## 📋 Содержание

- [Обзор проекта](#обзор-проекта)
- [Технологии и зависимости](#технологии-и-зависимости)
- [Настройка и запуск](#настройка-и-запуск)
- [Архитектура проекта](#архитектура-проекта)
- [Примеры использования](#примеры-использования)
- [API Endpoints](#api-endpoints)
- [Тестирование](#тестирование)
- [Мониторинг](#мониторинг)
- [Docker](#docker)
- [Best Practices](#best-practices)
- [Подводные камни](#подводные-камни)

## 🎯 Обзор проекта

Этот проект демонстрирует:

### Ключевые возможности WebClient
- **Reactive HTTP клиент** на основе Project Reactor
- **Connection pooling** и настройка производительности
- **Retry механизмы** с экспоненциальным backoff
- **Кэширование** с Spring Cache и Caffeine
- **Обработка ошибок** и graceful degradation
- **Мониторинг** и логирование запросов

### Демонстрируемые паттерны
- **ParameterizedTypeReference** для generic типов
- **Фильтры** для логирования и обработки ошибок
- **Rate limiting** и circuit breaker паттерны
- **Композиция** нескольких HTTP запросов
- **Streaming** с Server-Sent Events

## 🔧 Технологии и зависимости

### Основные технологии
- **Spring Boot 3.3.4** - основной фреймворк
- **Spring WebFlux** - реактивный веб стек
- **WebClient** - HTTP клиент
- **Project Reactor** - реактивные стримы
- **Java 17** - язык программирования

### Зависимости

```xml
<!-- Spring Boot Web Starter - Spring WebMVC, Jackson, Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

        <!-- Spring Boot WebFlux Starter - WebClient, Reactive Streams, Netty -->
<dependency>
<groupId>org.springframework.boot</groupId>
<artifactId>spring-boot-starter-webflux</artifactId>
</dependency>

        <!-- Spring Boot Cache Starter - Spring Cache, Caffeine -->
<dependency>
<groupId>org.springframework.boot</groupId>
<artifactId>spring-boot-starter-cache</artifactId>
</dependency>

        <!-- Spring Retry - @Retryable, RetryTemplate -->
<dependency>
<groupId>org.springframework.retry</groupId>
<artifactId>spring-retry</artifactId>
</dependency>

        <!-- Spring AOP - для работы @Retryable -->
<dependency>
<groupId>org.springframework.boot</groupId>
<artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

### Зависимости для тестирования
```xml
<!-- WireMock - моки HTTP сервисов -->
<dependency>
    <groupId>com.github.tomakehurst</groupId>
    <artifactId>wiremock-jre8</artifactId>
    <scope>test</scope>
</dependency>

        <!-- TestContainers - интеграционные тесты -->
<dependency>
<groupId>org.testcontainers</groupId>
<artifactId>junit-jupiter</artifactId>
<scope>test</scope>
</dependency>
```

## 🚀 Настройка и запуск

### Предварительные требования
- Java 17 или выше
- Maven 3.6+
- Docker и Docker Compose (опционально)

### Локальный запуск

1. **Клонирование репозитория**
```bash
git clone <repository-url>
cd webclient-demo
```

2. **Конфигурация**
```bash
# Скопируйте конфигурацию
cp src/main/resources/application-example.yml src/main/resources/application-local.yml

# Установите API ключ для погоды (опционально)
export WEATHER_API_KEY=your-openweathermap-api-key
```

3. **Сборка и запуск**
```bash
# Сборка проекта
mvn clean compile

# Запуск с профилем local
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Или сборка JAR и запуск
mvn clean package -DskipTests
java -jar target/webclient-demo-1.0.0.jar --spring.profiles.active=local
```

4. **Проверка работоспособности**
```bash
curl http://localhost:8080/actuator/health
```

### Docker запуск

1. **Запуск с Docker Compose**
```bash
# Запуск всей инфраструктуры
docker-compose up -d

# Просмотр логов
docker-compose logs -f webclient-demo

# Остановка
docker-compose down
```

2. **Только приложение в Docker**
```bash
# Сборка образа
docker build -t webclient-demo .

# Запуск контейнера
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e WEATHER_API_KEY=your-api-key \
  webclient-demo
```

## 🏗 Архитектура проекта

```
src/main/java/org/gualsh/demo/webclient/
├── WebClientDemoApplication.java          # Главный класс (@EnableCaching, @EnableRetry)
├── config/
│   └── WebClientConfig.java               # Конфигурация WebClient
├── dto/                                   # Data Transfer Objects
│   ├── UserDto.java                       # Пользователь JSONPlaceholder
│   ├── PostDto.java                       # Пост
│   ├── CommentDto.java                    # Комментарий
│   ├── WeatherDto.java                    # Данные о погоде
│   └── PagedResponseDto.java              # Пагинированный ответ
├── service/                               # Бизнес логика
│   ├── JsonPlaceholderService.java        # Сервис JSONPlaceholder API
│   └── WeatherService.java               # Сервис OpenWeatherMap API
├── controller/                            # REST контроллеры
│   ├── JsonPlaceholderController.java     # Контроллер JSONPlaceholder
│   └── WeatherController.java             # Контроллер погоды
└── exception/
    └── GlobalExceptionHandler.java        # Глобальная обработка ошибок
```

### Ключевые компоненты

#### WebClientConfig
- **Connection pooling** с настройкой лимитов
- **Таймауты** чтения и записи
- **Фильтры** для логирования и обработки ошибок
- **Кодеки** Jackson для JSON
- **Rate limiting** фильтр

#### JsonPlaceholderService
- Демонстрация **ParameterizedTypeReference**
- **Кэширование** с Spring Cache
- **Retry** механизмы
- **Композиция** запросов
- **Batch** операции

#### WeatherService
- Работа с **API ключами**
- **Graceful degradation**
- **Rate limiting** обработка
- **Конвертация данных**

## 💡 Примеры использования

### ParameterizedTypeReference для коллекций

```java
// Проблема: Type erasure в Java
Mono<List<UserDto>> users = webClient
        .get()
        .uri("/users")
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<UserDto>>() {});
// ParameterizedTypeReference сохраняет информацию о generic типах
```

### Кэширование с Spring Cache

```java
@Cacheable(value = "users", key = "#userId")
public Mono<UserDto> getUserById(Long userId) {
    return webClient.get()
        .uri("/users/{id}", userId)
        .retrieve()
        .bodyToMono(UserDto.class);
}
```

### Retry с экспоненциальным backoff

```java
@Retryable(
    value = {WebClientResponseException.ServerError.class},
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2.0)
)
public Mono<UserDto> getUserWithRetry(Long userId) {
    return webClient.get()
        .uri("/users/{id}", userId)
        .retrieve()
        .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
        .bodyToMono(UserDto.class);
}
```

### Композиция нескольких запросов

```java
public Mono<Map<String, Object>> getUserProfile(Long userId) {
    Mono<UserDto> userMono = getUserById(userId);
    Mono<List<PostDto>> postsMono = getPostsByUserId(userId).collectList();

    return Mono.zip(userMono, postsMono)
        .map(tuple -> Map.of(
            "user", tuple.getT1(),
            "posts", tuple.getT2(),
            "postsCount", tuple.getT2().size()
        ));
}
```

### Фильтры для логирования

```java
private ExchangeFilterFunction loggingFilter() {
    return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
        log.debug("Request: {} {}", clientRequest.method(), clientRequest.url());
        return Mono.just(clientRequest);
    });
}
```

## 🔗 API Endpoints

### JSONPlaceholder API

| Метод | Endpoint | Описание | Пример |
|-------|----------|----------|---------|
| GET | `/api/v1/jsonplaceholder/users` | Все пользователи | `curl http://localhost:8080/api/v1/jsonplaceholder/users` |
| GET | `/api/v1/jsonplaceholder/users/{id}` | Пользователь по ID | `curl http://localhost:8080/api/v1/jsonplaceholder/users/1` |
| GET | `/api/v1/jsonplaceholder/users/{id}/posts` | Посты пользователя (SSE) | `curl -N http://localhost:8080/api/v1/jsonplaceholder/users/1/posts` |
| GET | `/api/v1/jsonplaceholder/posts?page=0&size=10` | Посты с пагинацией | `curl "http://localhost:8080/api/v1/jsonplaceholder/posts?page=0&size=5"` |
| POST | `/api/v1/jsonplaceholder/posts` | Создать пост | См. пример ниже |
| PUT | `/api/v1/jsonplaceholder/posts/{id}` | Обновить пост | См. пример ниже |
| PATCH | `/api/v1/jsonplaceholder/posts/{id}` | Частично обновить | См. пример ниже |
| DELETE | `/api/v1/jsonplaceholder/posts/{id}` | Удалить пост | `curl -X DELETE http://localhost:8080/api/v1/jsonplaceholder/posts/1` |

### Weather API

| Метод | Endpoint | Описание | Пример |
|-------|----------|----------|---------|
| GET | `/api/v1/weather/current?city=London` | Текущая погода | `curl "http://localhost:8080/api/v1/weather/current?city=London"` |
| GET | `/api/v1/weather/coordinates?lat=51.5&lon=-0.1` | Погода по координатам | `curl "http://localhost:8080/api/v1/weather/coordinates?lat=51.5&lon=-0.1"` |
| GET | `/api/v1/weather/health` | Статус weather API | `curl http://localhost:8080/api/v1/weather/health` |

### Примеры запросов

**Создание поста:**
```bash
curl -X POST http://localhost:8080/api/v1/jsonplaceholder/posts \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "title": "My New Post",
    "body": "This is the content of my new post"
  }'
```

**Частичное обновление поста:**
```bash
curl -X PATCH http://localhost:8080/api/v1/jsonplaceholder/posts/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Updated Title"
  }'
```

**Batch запрос пользователей:**
```bash
curl -X POST http://localhost:8080/api/v1/jsonplaceholder/users/batch \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -d '[1, 2, 3, 4, 5]'
```

## 🧪 Тестирование

### Запуск тестов

```bash
# Все тесты
mvn test

# Только unit тесты
mvn test -Dtest="*Test"

# Только интеграционные тесты
mvn test -Dtest="*IntegrationTest"

# С покрытием кода
mvn test jacoco:report
```

### Типы тестов

#### Unit тесты с WireMock
```java
@Test
@DisplayName("Should successfully fetch all users")
void shouldFetchAllUsers() {
    // Given: настраиваем мок
    wireMockServer.stubFor(get(urlEqualTo("/users"))
        .willReturn(aResponse()
            .withStatus(200)
            .withBody(usersJson)));

    // When & Then: проверяем с StepVerifier
    StepVerifier.create(jsonPlaceholderService.getAllUsers())
        .expectNextMatches(users -> users.size() == 1)
        .verifyComplete();
}
```

#### Интеграционные тесты
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class WebClientIntegrationTest {

    @Test
    void shouldGetAllUsers() {
        webTestClient
            .get()
            .uri("/api/v1/jsonplaceholder/users")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(UserDto.class)
            .hasSize(10);
    }
}
```

## 📊 Мониторинг

### Actuator Endpoints

- **Health**: `http://localhost:8080/actuator/health`
- **Metrics**: `http://localhost:8080/actuator/metrics`
- **Info**: `http://localhost:8080/actuator/info`
- **Caches**: `http://localhost:8080/actuator/caches`

### Метрики WebClient

```bash
# HTTP клиентские метрики
curl http://localhost:8080/actuator/metrics/http.client.requests

# Метрики кэша
curl http://localhost:8080/actuator/metrics/cache.gets

# JVM метрики
curl http://localhost:8080/actuator/metrics/jvm.memory.used
```

### Grafana Dashboard

После запуска Docker Compose:
- **URL**: http://localhost:3000
- **Логин**: admin / admin123
- Дашборды включают метрики WebClient, JVM, и HTTP запросов

## 🐳 Docker

### Структура Docker

```yaml
services:
  webclient-demo:     # Основное приложение
  redis:              # Кэширование
  prometheus:         # Сбор метрик
  grafana:           # Визуализация
  wiremock:          # Моки для разработки
  nginx:             # Reverse proxy
```

### Полезные команды

```bash
# Запуск в фоне
docker-compose up -d

# Просмотр логов
docker-compose logs -f webclient-demo

# Перезапуск сервиса
docker-compose restart webclient-demo

# Масштабирование
docker-compose up -d --scale webclient-demo=3

# Очистка
docker-compose down -v --remove-orphans
```

## ✅ Best Practices

### 1. Конфигурация WebClient

```java
// ✅ Хорошо: переиспользование WebClient
@Bean
public WebClient webClient() {
    return WebClient.builder()
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .exchangeStrategies(strategies)
        .filter(loggingFilter())
        .build();
}

// ❌ Плохо: создание нового WebClient для каждого запроса
WebClient.create().get().uri("...").retrieve()...
```

### 2. Connection Pooling

```java
// ✅ Настройка connection pool
ConnectionProvider connectionProvider = ConnectionProvider.builder("custom")
        .maxConnections(100)
        .pendingAcquireTimeout(Duration.ofSeconds(45))
        .maxIdleTime(Duration.ofSeconds(20))
        .build();
```

### 3. Обработка ошибок

```java
// ✅ Структурированная обработка ошибок
return webClient.get()
    .uri("/users/{id}", userId)
    .retrieve()
    .onStatus(HttpStatus.NOT_FOUND::equals,
              response -> Mono.error(new UserNotFoundException(userId)))
    .onStatus(HttpStatus::is5xxServerError,
              response -> Mono.error(new ExternalServiceException()))
    .bodyToMono(UserDto.class);
```

### 4. Retry стратегии

```java
// ✅ Умный retry только для подходящих ошибок
.retryWhen(Retry.backoff(maxAttempts, Duration.ofMillis(delay))
    .filter(throwable -> throwable instanceof WebClientResponseException.ServerError)
    .onRetryExhaustedThrow((spec, signal) ->
    new RuntimeException("Retry exhausted after " + signal.totalRetries() + " attempts")))
```

### 5. Кэширование

```java
// ✅ Кэширование с правильными ключами
@Cacheable(value = "users", key = "#userId", unless = "#result == null")
public Mono<UserDto> getUserById(Long userId) {
    // Не кэшируем null результаты
}
```

### 6. Reactive лучшие практики

```java
// ✅ Правильная композиция
public Mono<ProfileDto> getUserProfile(Long userId) {
    return getUserById(userId)
        .flatMap(user -> getPostsByUserId(userId)
            .collectList()
            .map(posts -> new ProfileDto(user, posts)));
}

// ❌ Блокирующие вызовы в reactive цепочке
.map(user -> blockingDatabaseCall(user)) // НЕ ДЕЛАЙТЕ ТАК!
```

## ⚠️ Подводные камни

### 1. Memory Leaks

**Проблема**: Неправильное управление подписками
```java
// ❌ Потенциальная утечка памяти
webClient.get().uri("/data").retrieve().bodyToMono(String.class)
    .subscribe(); // Подписка никогда не отменяется
```

**Решение**: Правильная обработка lifecycle
```java
// ✅ Управление подписками
Disposable subscription = webClient.get()...subscribe();
// В методе cleanup:
if (subscription != null && !subscription.isDisposed()) {
    subscription.dispose();
}
```

### 2. Connection Pool Exhaustion

**Проблема**: Исчерпание пула соединений
```java
// ❌ Каждый WebClient создает свой пул
for (int i = 0; i < 1000; i++) {
    WebClient.create().get()... // 1000 connection pools!
    }
```

**Решение**: Переиспользование WebClient
```java
// ✅ Один WebClient с настроенным пулом
@Bean
public WebClient sharedWebClient() {
    return WebClient.builder()
        .clientConnector(new ReactorClientHttpConnector(
            HttpClient.create(connectionProvider)))
        .build();
}
```

### 3. Spring Version Compatibility Issues

**Проблема**: HttpStatusCode.getReasonPhrase() не существует в Spring 6.x
```java
// ❌ Ошибка компиляции в Spring Boot 3.x
clientResponse.statusCode().getReasonPhrase() // Cannot resolve method
```

**Решение**: Безопасное приведение типов
```java
// ✅ Правильная проверка типа
private String getReasonPhrase(HttpStatusCode statusCode) {
    if (statusCode instanceof HttpStatus) {
        return ((HttpStatus) statusCode).getReasonPhrase();
    }
    return "";
}

// ✅ Еще лучше - кастомные исключения
.onStatus(HttpStatus.NOT_FOUND::equals,
    response -> Mono.error(new ResourceNotFoundException("Not found")))
```

### 4. WebClientResponseException Constructor Issues

**Проблема**: Непубличные конструкторы исключений
```java
// ❌ Ошибка компиляции - конструктор не публичный
new WebClientResponseException.TooManyRequests(429, "Rate limit", null, null, null)
```

**Решение**: Кастомные исключения
```java
// ✅ Публичные кастомные исключения
public class RateLimitExceededException extends RuntimeException {
    private final HttpStatus status = HttpStatus.TOO_MANY_REQUESTS;
    // публичные конструкторы
}

.onStatus(status -> status.value() == 429,
    response -> Mono.error(new RateLimitExceededException("Rate limit exceeded")))
```

### 3. Блокирующие операции в Reactive цепочке

**Проблема**: Блокировка event loop
```java
// ❌ Блокирующий вызов в reactive цепочке
return webClient.get().uri("/users").retrieve()
    .bodyToFlux(User.class)
    .map(user -> {
    // Блокирующий вызов БД - блокирует event loop!
    return jdbcTemplate.queryForObject("SELECT...", String.class);
    });
```

**Решение**: Использование schedulers
```java
// ✅ Вынос блокирующих операций в отдельный thread pool
return webClient.get().uri("/users").retrieve()
    .bodyToFlux(User.class)
    .flatMap(user -> Mono.fromCallable(() ->
    jdbcTemplate.queryForObject("SELECT...", String.class))
    .subscribeOn(Schedulers.boundedElastic()))
```

### 4. Неправильная обработка backpressure

**Проблема**: Переполнение буферов
```java
// ❌ Может вызвать OutOfMemoryError
Flux.range(1, Integer.MAX_VALUE)
    .flatMap(i -> webClient.get().uri("/item/" + i)
        .retrieve().bodyToMono(String.class))
    .subscribe();
```

**Решение**: Ограничение concurrency
```java
// ✅ Контроль параллелизма
Flux.range(1, Integer.MAX_VALUE)
    .flatMap(i -> webClient.get().uri("/item/" + i)
        .retrieve().bodyToMono(String.class), 
        10) // Максимум 10 параллельных запросов
            .subscribe();
```

### 5. Неправильная настройка таймаутов

**Проблема**: Зависшие запросы
```java
// ❌ Без таймаутов запросы могут висеть вечно
webClient.get().uri("/slow-endpoint").retrieve()...
```

**Решение**: Множественные уровни таймаутов
```java
// ✅ Комплексная настройка таймаутов
HttpClient httpClient = HttpClient.create()
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // Connection timeout
        .doOnConnected(conn ->
            conn.addHandlerLast(new ReadTimeoutHandler(30, TimeUnit.SECONDS)) // Read timeout
                .addHandlerLast(new WriteTimeoutHandler(30, TimeUnit.SECONDS))); // Write timeout

// + response timeout на уровне WebClient
webClient.get().uri("/endpoint").retrieve()
    .bodyToMono(String.class)
    .timeout(Duration.ofSeconds(10)); // Response timeout
```

### 6. Неправильное использование @Cacheable с Reactive

**Проблема**: Кэширование Mono/Flux вместо данных
```java
// ❌ Кэшируется Mono, а не данные
@Cacheable("users")
public Mono<User> getUser(Long id) {
    return webClient.get()... // Кэшируется объект Mono!
}
```

**Решение**: Кэширование результата
```java
// ✅ Правильное кэширование для reactive
@Cacheable("users")
public Mono<User> getUser(Long id) {
    return Mono.fromCallable(() -> getCachedOrFetch(id))
        .subscribeOn(Schedulers.boundedElastic());
}
```

### 7. JSON Deserialization проблемы

**Проблема**: Потеря generic информации
```java
// ❌ Type erasure - не работает для List<User>
.bodyToMono(List.class) // Получим List<LinkedHashMap>
```

**Решение**: ParameterizedTypeReference
```java
// ✅ Сохранение generic информации
.bodyToMono(new ParameterizedTypeReference<List<User>>() {})
```

## 📚 Дополнительные ресурсы

### Документация
- [Spring WebClient Reference](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#webflux-client)
- [Project Reactor Documentation](https://projectreactor.io/docs)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)

### Полезные статьи
- [WebClient Memory Leaks](https://spring.io/blog/2019/12/13/flight-of-the-flux-3-hopping-threads-and-schedulers)
- [Reactive Streams Backpressure](https://www.reactive-streams.org/)
- [Connection Pool Tuning](https://projectreactor.io/docs/netty/release/reference/index.html#_connection_pool_2)

## 🤝 Участие в разработке

1. Fork репозиторий
2. Создайте feature branch: `git checkout -b feature/amazing-feature`
3. Commit изменения: `git commit -m 'Add amazing feature'`
4. Push в branch: `git push origin feature/amazing-feature`
5. Создайте Pull Request

## 📄 Лицензия

Этот проект распространяется под лицензией MIT. См. файл [LICENSE](LICENSE) для деталей.

## 👥 Авторы

- Demo Team - *Первоначальная работа*

## 🙏 Благодарности

- Spring Team за отличную документацию
- JSONPlaceholder за бесплатный тестовый API
- OpenWeatherMap за API погоды
- Сообщество Spring Boot за примеры и best practices

---

**Примечание**: Этот проект создан в образовательных целях для демонстрации возможностей Spring WebClient. В production окружении убедитесь в соответствии всех настроек вашим требованиям безопасности и производительности.