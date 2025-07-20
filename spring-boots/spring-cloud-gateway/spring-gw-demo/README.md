# Spring Cloud Gateway Demo
### Ключевые концепции

1. Route (Маршрут) - базовая единица Gateway, определяющая куда направить запрос
2. **Predicate (Предикат)** - условие для применения маршрута
3. **Filter (Фильтр)** - модификация запросов и ответов
4. **Gateway Filter** - фильтры для конкретных маршрутов
5. **Global Filter** - фильтры для всех маршрутов


```bash
# Запуск Redis контейнера
podman rm -f redis-gateway-demo

podman run -d \
  --name redis-gateway-demo \
  -p 6379:6379 \
  redis:7-alpine \
  redis-server --appendonly yes
  
podman ps 
```

```bash
# Логи 
podman logs redis-gateway-demo
```

```bash
# Завершаем, если запущено 
clear 
JPID=$(jps | grep GatewayDemoApplication | cut -d ' ' -f 1)
echo $JPID
if [ -n "$JPID" ]; then
    kill $JPID
fi
```

```
# Сборка и запуск приложения
mvn clean install spring-boot:run
```

```bash
# Проверка работоспособности
clear
curl -s http://localhost:8080/actuator/health | jq
```
```bash
# Маршруты
clear
curl -s http://localhost:8080/actuator/gateway/routes | jq
```

```bash
# Базовый маршрут
curl -X GET http://localhost:8080/demo/get
```

```bash
# Программный маршрут
curl -X GET http://localhost:8080/programmatic/get
```

```bash
# Модификация заголовков
curl -X GET http://localhost:8080/transform/get
```


```bash
# Модификация заголовков
curl -X GET http://localhost:8080/transform-yml/get
```

```bash
# Модификация заголовков
curl -X GET http://localhost:8080/request-info-filter/get
```

```bash
# Маршрут с условиями
curl -X GET "http://localhost:8080/conditional/get?env=dev"
```

```bash
# Rate limiting
curl -X GET http://localhost:8080/rate-limited/get
```

```bash
# Circuit breaker проверка работоспособности
clear
curl -X GET http://localhost:8080/circuit-breaker/get
```
```bash
# Проверка метрик Circuit Breaker
clear
curl -s http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.state | jq
```
```bash 
#!/bin/bash
echo "Текущее состояние Circuit Breaker 'demo-circuit-breaker':"

states=("closed" "open" "half_open" "disabled" "forced_open" "metrics_only")

for state in "${states[@]}"; 
do
  value=$(curl -s "http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.state?tag=name:demo-circuit-breaker&tag=state:$state" | jq -r '.measurements[0].value')
  if [ "$value" != "0" ] && [ "$value" != "0.0" ] && [ "$value" != "null" ]; then
    clear
    echo "✅ Состояние: $state (значение: $value)"
  fi
done
```
```bash
# Все метрики Circuit Breaker
curl -s http://localhost:8080/actuator/metrics | grep circuitbreaker | jq
```
```bash 
# Prometheus метрики
clear
curl -s http://localhost:8080/actuator/prometheus | grep circuitbreaker
```
```bash 
# Prometheus метрики
clear
curl -s http://localhost:8080/actuator/prometheus | grep "resilience4j_circuitbreaker_state"
```


```bash
# Cached пока не понял зачем
curl -X GET http://localhost:8080/cached/get
```

## ⚙️ Конфигурация

### Конфигурационные свойства (GatewayProperties)

Проект использует типизированные конфигурационные свойства через `@ConfigurationProperties`. Все настройки находятся в секции `gateway.demo`:

```yaml
gateway:
  demo:
    enabled: true
    rate-limiting:
      enabled: true
      default-rate: 100
      burst-capacity: 200
    security:
      enabled: true
      api-key-auth-enabled: true
      api-keys:
        "demo-key-1": "user1"
        "admin-key": "admin"
    circuit-breaker:
      enabled: true
      failure-rate-threshold: 50
    monitoring:
      enabled: true
      prometheus-enabled: true
    caching:
      enabled: false
      default-ttl: PT5M
    logging:
      detailed-logging-enabled: true
```

### Как используются GatewayProperties

#### 1. **Условное создание компонентов**
```java
@Bean
public GlobalFilter securityHeadersGlobalFilter() {
    return (exchange, chain) -> {
        if (!gatewayProperties.getSecurity().isEnabled()) {
            return chain.filter(exchange);
        }
        // Добавление security заголовков
    };
}
```

#### 2. **Динамическая конфигурация фильтров**
```java
@Bean
public RedisRateLimiter configuredRedisRateLimiter() {
    var config = gatewayProperties.getRateLimiting();
    return new RedisRateLimiter(
        config.getDefaultRate(),
        config.getBurstCapacity()
    );
}
```

#### 3. **CORS настройки из конфигурации**
```java
headers.add("Access-Control-Allow-Origin", 
    String.join(", ", gatewayProperties.getSecurity().getAllowedOrigins()));
```

#### 4. **Валидация при запуске**
```java
@Component
public class PropertiesValidationConfig implements CommandLineRunner {
    @Override
    public void run(String... args) {
        validateAndLogConfiguration();
    }
}
```

### Основная конфигурация (application.yml)

Проект использует `application.yml` для конфигурации. Основные секции:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: demo-service
          uri: https://httpbin.org
          predicates:
            - Path=/demo/**
          filters:
            - StripPrefix=1
```

### Настройки производительности

```yaml
server:
  netty:
    connection-timeout: 2s
    h2c-max-content-length: 0B
    initial-buffer-size: 128
    max-chunk-size: 8192
```

### Конфигурация через Java

```java
@Bean
public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
    return builder.routes()
        .route("programmatic-route", r -> r
            .path("/programmatic/**")
            .filters(f -> f.stripPrefix(1))
            .uri("https://httpbin.org")
        )
        .build();
}
```

## 🔧 Возможности

### 1. Маршрутизация

#### Типы предикатов:
- **Path** - по пути запроса
- **Method** - по HTTP методу
- **Header** - по заголовкам
- **Query** - по параметрам запроса
- **After/Before** - по времени
- **Weight** - весовая маршрутизация

#### Пример:
```yaml
predicates:
  - Path=/api/v1/**
  - Method=GET,POST
  - Header=X-Request-Type, demo
  - Query=version, 2
  - After=2024-01-01T00:00:00+00:00[UTC]
```

### 2. Фильтрация

#### Встроенные фильтры:
- **AddRequestHeader/AddResponseHeader** - добавление заголовков
- **StripPrefix** - удаление префикса из пути
- **RewritePath** - перезапись пути
- **RequestRateLimiter** - ограничение скорости запросов
- **CircuitBreaker** - circuit breaker pattern
- **Retry** - повторные попытки

#### Пример:
```yaml
filters:
  - StripPrefix=1
  - AddRequestHeader=X-Custom-Header, value
  - name: RequestRateLimiter
    args:
      redis-rate-limiter.replenishRate: 10
      redis-rate-limiter.burstCapacity: 20
```

### 3. Глобальные фильтры

Проект включает несколько образовательных глобальных фильтров:

- **LoggingGlobalFilter** - логирование всех запросов
- **SecurityHeadersGlobalFilter** - добавление security заголовков
- **CorsGlobalFilter** - обработка CORS

### 4. Кастомные фильтры

#### RequestInfoGatewayFilterFactory
Добавляет информацию о запросе в заголовки:

```yaml
filters:
  - name: RequestInfo
    args:
      prefix: "X-Custom"
      includeHeaders: true
```

#### MetricsGatewayFilterFactory
Собирает метрики запросов:

```yaml
filters:
  - name: Metrics
    args:
      logMetrics: true
```

#### ApiKeyAuthGatewayFilterFactory
Простая аутентификация по API ключу:

```yaml
filters:
  - name: ApiKeyAuth
    args:
      headerName: "X-API-Key"
```

### 5. Rate Limiting

Использует Redis для ограничения скорости запросов:

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379

# В маршруте
filters:
  - name: RequestRateLimiter
    args:
      redis-rate-limiter.replenishRate: 10
      redis-rate-limiter.burstCapacity: 20
      redis-rate-limiter.requestedTokens: 1
```

### 6. Circuit Breaker

Интеграция с Resilience4j:

```yaml
resilience4j:
  circuitbreaker:
    instances:
      demo-circuit-breaker:
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
        sliding-window-size: 10

# В маршруте
filters:
  - name: CircuitBreaker
    args:
      name: demo-circuit-breaker
      fallbackUri: forward:/fallback
```

### 7. Обработка ошибок

Глобальный обработчик ошибок обеспечивает:
- Единообразный формат ошибок
- Логирование с контекстом
- Безопасность (скрытие внутренних деталей)
- Корреляция с request ID

## 📊 Мониторинг

### Actuator Endpoints

Проект включает настроенные endpoints для мониторинга:

```bash
# Общее здоровье
curl http://localhost:8080/actuator/health

# Метрики
curl http://localhost:8080/actuator/metrics

# Prometheus метрики
curl http://localhost:8080/actuator/prometheus

# Gateway маршруты
curl http://localhost:8080/actuator/gateway/routes

# Фильтры
curl http://localhost:8080/actuator/gateway/globalfilters
```

### Кастомные метрики

Проект собирает метрики:
- Количество запросов по маршрутам
- Время ответа
- Количество ошибок
- Rate limiting статистики

### Логирование

Структурированное логирование включает:
- Request ID для трассировки
- Информацию о маршрутах
- Метрики производительности
- Детали ошибок

## 🔒 Безопасность

### CORS

Настроена глобальная CORS политика:

```yaml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins: "*"
            allowedMethods: "*"
            allowedHeaders: "*"
```

### Security Headers

Автоматическое добавление security заголовков:
- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `X-XSS-Protection: 1; mode=block`
- `Strict-Transport-Security`
- `Content-Security-Policy`

### API Key Authentication

Простая аутентификация по API ключу:

```bash
curl -H "X-API-Key: demo-key-1" http://localhost:8080/demo/get
```

## ⚡ Производительность

### Настройки Netty

```yaml
server:
  netty:
    connection-timeout: 2s
    initial-buffer-size: 128
    max-chunk-size: 8192
    validate-headers: true
```

### Пулы соединений

```yaml
spring:
  data:
    redis:
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
```

### Рекомендации по производительности

1. **Используйте неблокирующие операции** - избегайте блокирующих вызовов в фильтрах
2. **Оптимизируйте предикаты** - размещайте наиболее селективные предикаты первыми
3. **Настройте пулы соединений** - для Redis и HTTP клиентов
4. **Мониторьте метрики** - особенно время ответа и использование памяти

## 🛠️ Troubleshooting

### Частые проблемы

#### 1. Блокирующие операции в WebFlux

**Проблема**: `IllegalStateException: block()/blockFirst()/blockLast() are blocking`

**Решение**: Используйте reactive типы:
```java
// Неправильно
String result = webClient.get().retrieve().bodyToMono(String.class).block();

// Правильно
return webClient.get().retrieve().bodyToMono(String.class);
```

#### 2. Circuit Breaker не работает

**Проблема**: Fallback не срабатывает

**Решение**: Проверьте конфигурацию и убедитесь, что:
- Circuit breaker правильно настроен
- Fallback endpoint существует
- Имена circuit breaker совпадают

#### 3. Rate Limiting не работает

**Проблема**: Запросы не ограничиваются

**Решение**: Убедитесь, что:
- Redis доступен и работает
- Конфигурация Redis корректна
- Rate limiter правильно настроен

#### 4. Маршруты не применяются

**Проблема**: Запросы не попадают в ожидаемые маршруты

**Решение**: Проверьте:
- Порядок маршрутов (более специфичные должны быть первыми)
- Предикаты правильно настроены
- Нет конфликтующих маршрутов

### Отладка

#### Включение debug логов

```yaml
logging:
  level:
    org.springframework.cloud.gateway: DEBUG
    reactor.netty: DEBUG
```

#### Проверка маршрутов

```bash
curl http://localhost:8080/actuator/gateway/routes | jq
```

#### Мониторинг Redis

```bash
redis-cli monitor
```

## 🔍 Образовательные моменты

### Reactive Programming

Gateway основан на Project Reactor. Ключевые концепции:
- `Mono<T>` - 0 или 1 элемент
- `Flux<T>` - 0 или много элементов
- Неблокирующие операции
- Backpressure

### Netty

Понимание Netty важно для:
- Настройки производительности
- Отладки сетевых проблем
- Оптимизации ресурсов

### Микросервисная архитектура

Gateway демонстрирует паттерны:
- API Gateway
- Service Discovery
- Circuit Breaker
- Rate Limiting
- Distributed Tracing

## 📚 Дополнительные ресурсы

### Документация
- [Spring Cloud Gateway Reference](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/)
- [Spring WebFlux Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html)
- [Project Reactor Documentation](https://projectreactor.io/docs)

### Примеры использования
- [Spring Cloud Samples](https://github.com/spring-cloud/spring-cloud-gateway)
- [Reactive Programming Guide](https://github.com/reactor/reactor-core)

## 🤝 Участие в проекте

Этот проект создан для образовательных целей. Вклад приветствуется:

1. Fork проекта
2. Создайте feature branch
3. Внесите изменения
4. Добавьте тесты
5. Обновите документацию
6. Создайте Pull Request

## 📄 Лицензия

Этот проект распространяется под MIT лицензией. См. файл LICENSE для подробностей.

## 📧 Контакты

Для вопросов и предложений создайте Issue в репозитории проекта.

---

**Важно**: Этот проект предназначен для образовательных целей. Для production использования необходимо адаптировать конфигурацию под конкретные требования безопасности и производительности.