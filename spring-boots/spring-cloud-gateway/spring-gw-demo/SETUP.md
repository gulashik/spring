                                                                                                                                                                                                    # Инструкция по настройке и запуску Spring Cloud Gateway Demo

## 📋 Системные требования

### Минимальные требования

- **Java**: 17 или выше
- **Maven**: 3.8.0 или выше
- **RAM**: 2GB минимум, 4GB рекомендуется
- **Дисковое пространство**: 1GB свободного места
- **ОС**: Windows 10+, macOS 10.14+, Linux (Ubuntu 18.04+)

### Дополнительные зависимости

- **Redis**: 6.0 или выше (для Rate Limiting)
- **Docker**: 20.10+ (опционально, для Redis)
- **Git**: 2.20+ (для клонирования проекта)

## 🔧 Установка зависимостей

### 1. Установка Java 17

#### Windows
```bash
# Через Chocolatey
choco install openjdk17

# Или скачать с https://adoptium.net/
```

#### macOS
```bash
# Через Homebrew
brew install openjdk@17

# Добавить в PATH
echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
```

#### Linux (Ubuntu/Debian)
```bash
# Установка OpenJDK 17
sudo apt update
sudo apt install openjdk-17-jdk

# Проверка версии
java -version
```

### 2. Установка Maven

#### Windows
```bash
# Через Chocolatey
choco install maven

# Или скачать с https://maven.apache.org/download.cgi
```

#### macOS
```bash
# Через Homebrew
brew install maven
```

#### Linux (Ubuntu/Debian)
```bash
sudo apt update
sudo apt install maven

# Проверка версии
mvn -version
```

### 3. Установка Redis

#### Вариант 1: Через Docker (рекомендуется)
```bash
# Запуск Redis контейнера
docker run -d \
  --name redis-gateway-demo \
  -p 6379:6379 \
  redis:7-alpine \
  redis-server --appendonly yes

# Проверка работы
docker logs redis-gateway-demo
```

#### Вариант 2: Локальная установка

**Windows:**
```bash
# Через Chocolatey
choco install redis-64

# Или скачать с https://github.com/microsoftarchive/redis/releases
```

**macOS:**
```bash
# Через Homebrew
brew install redis

# Запуск
brew services start redis
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install redis-server

# Запуск
sudo systemctl start redis-server
sudo systemctl enable redis-server
```

### 4. Проверка установки Redis

```bash
# Подключение к Redis
redis-cli ping

# Ожидаемый ответ: PONG
```

## 🚀 Настройка и запуск проекта

### 1. Клонирование репозитория

```bash
# Клонирование проекта
git clone https://github.com/your-repo/spring-cloud-gateway-demo.git

# Переход в директорию
cd spring-cloud-gateway-demo
```

### 2. Конфигурация проекта

#### Базовая конфигурация (application.yml)

Проект поставляется с готовой конфигурацией, но вы можете изменить следующие параметры:

```yaml
# Порт приложения
server:
  port: 8080

# Настройки Redis
spring:
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
```

#### Настройка для различных окружений

Создайте файл `application-dev.yml` для development:

```yaml
logging:
  level:
    org.springframework.cloud.gateway: DEBUG
    org.gualsh.demo.gw: DEBUG
    
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

### 3. Сборка проекта

```bash
# Очистка и сборка
mvn clean install

# Или с пропуском тестов
mvn clean install -DskipTests
```

### 4. Запуск приложения

#### Вариант 1: Через Maven
```bash
# Запуск с профилем по умолчанию
mvn spring-boot:run

# Запуск с dev профилем
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

#### Вариант 2: Через Java
```bash
# Сборка JAR
mvn clean package

# Запуск JAR
java -jar target/spring-cloud-gateway-demo-1.0.0.jar

# С профилем
java -jar target/spring-cloud-gateway-demo-1.0.0.jar --spring.profiles.active=dev
```

#### Вариант 3: Через IDE

1. Импортируйте проект в IDE (IntelliJ IDEA, VS Code, Eclipse)
2. Убедитесь, что настроен JDK 17
3. Найдите класс `GatewayDemoApplication`
4. Запустите main метод

## ✅ Проверка работоспособности

### 1. Базовые проверки

```bash
# Проверка здоровья приложения
curl http://localhost:8080/actuator/health

# Ожидаемый ответ:
{
  "status": "UP",
  "components": {
    "diskSpace": {"status": "UP"},
    "ping": {"status": "UP"},
    "redis": {"status": "UP"}
  }
}
```

```bash
# Информация о приложении
curl http://localhost:8080/info

# Проверка маршрутов
curl http://localhost:8080/actuator/gateway/routes
```

### 2. Тестирование маршрутов

```bash
# Базовый маршрут
curl -X GET http://localhost:8080/demo/get

# Программный маршрут
curl -X GET http://localhost:8080/programmatic/get

# Маршрут с условиями
curl -X GET "http://localhost:8080/conditional/get?env=dev"

# Проверка заголовков
curl -H "X-Custom-Header: test" http://localhost:8080/custom/get
```

### 3. Тестирование фильтров

```bash
# Rate Limiting
for i in {1..15}; do curl -X GET http://localhost:8080/rate-limited/get; done

# Circuit Breaker (симуляция ошибки)
curl -X GET http://localhost:8080/circuit-breaker/status/500

# Fallback endpoint
curl -X GET http://localhost:8080/fallback
```

### 4. Тестирование аутентификации

```bash
# Без API ключа (должен вернуть 401)
curl -X GET http://localhost:8080/secured/get

# С API ключом
curl -H "X-API-Key: demo-key-1" http://localhost:8080/secured/get
```

## 🔧 Настройка окружения

### 1. Переменные окружения

Создайте файл `.env` в корне проекта:

```bash
# Настройки Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_DATABASE=0

# Настройки приложения
SERVER_PORT=8080
LOGGING_LEVEL=INFO

# Настройки безопасности
API_KEY_HEADER=X-API-Key
JWT_SECRET=your-secret-key-here
```

### 2. Профили Spring

#### development (application-dev.yml)
```yaml
spring:
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true

logging:
  level:
    org.springframework.cloud.gateway: DEBUG
    org.gualsh.demo.gw: DEBUG
```

#### production (application-prod.yml)
```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      ssl: true
      
logging:
  level:
    root: WARN
    org.gualsh.demo.gw: INFO
    
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

### 3. Docker Compose для полного стека

Создайте файл `docker-compose.yml`:

```yaml
version: '3.8'

services:
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes
    volumes:
      - redis_data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5

  gateway:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_DATA_REDIS_HOST=redis
    depends_on:
      redis:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

volumes:
  redis_data:
```

Запуск через Docker Compose:
```bash
# Запуск всех сервисов
docker-compose up -d

# Просмотр логов
docker-compose logs -f gateway

# Остановка
docker-compose down
```

## 🐛 Troubleshooting

### Проблема: Порт 8080 уже занят

**Решение:**
```bash
# Проверить какой процесс использует порт
lsof -i :8080  # macOS/Linux
netstat -ano | findstr :8080  # Windows

# Изменить порт в application.yml
server:
  port: 8081
```

### Проблема: Redis недоступен

**Симптомы:**
- Ошибка подключения к Redis
- Rate limiting не работает

**Решение:**
```bash
# Проверить статус Redis
redis-cli ping

# Проверить настройки подключения
redis-cli -h localhost -p 6379 ping

# Перезапустить Redis
docker restart redis-gateway-demo
```

### Проблема: OutOfMemoryError

**Решение:**
```bash
# Увеличить память для JVM
java -Xmx2g -Xms1g -jar target/spring-cloud-gateway-demo-1.0.0.jar

# Или через Maven
export MAVEN_OPTS="-Xmx2g -Xms1g"
mvn spring-boot:run
```

### Проблема: Маршруты не работают

**Диагностика:**
```bash
# Проверить активные маршруты
curl http://localhost:8080/actuator/gateway/routes

# Проверить фильтры
curl http://localhost:8080/actuator/gateway/globalfilters

# Включить debug логи
# В application.yml:
logging:
  level:
    org.springframework.cloud.gateway: DEBUG
```

## 📊 Мониторинг и метрики

### Настройка Prometheus

1. Создайте файл `prometheus.yml`:

```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'gateway-demo'
    static_configs:
      - targets: ['localhost:8080']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 5s
```

2. Запустите Prometheus:
```bash
docker run -d \
  --name prometheus \
  -p 9090:9090 \
  -v $(pwd)/prometheus.yml:/etc/prometheus/prometheus.yml \
  prom/prometheus
```

### Настройка Grafana

1. Запустите Grafana:
```bash
docker run -d \
  --name grafana \
  -p 3000:3000 \
  grafana/grafana
```

2. Откройте http://localhost:3000 (admin/admin)
3. Добавьте Prometheus data source: http://localhost:9090
4. Импортируйте дашборд для Spring Boot

## 🔐 Настройка безопасности

### SSL/TLS

Для production окружения настройте HTTPS:

```yaml
server:
  port: 8443
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: changeit
    key-store-type: PKCS12
    key-alias: gateway-demo
```

### API Keys

Настройте валидные API ключи в application.yml:

```yaml
gateway:
  demo:
    security:
      api-keys:
        "your-api-key-1": "user1"
        "your-api-key-2": "user2"
        "admin-key": "admin"
```

## 🚀 Deployment

### Подготовка к production

1. **Создайте production конфигурацию:**

```yaml
# application-prod.yml
spring:
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}
      password: ${REDIS_PASSWORD}
      ssl: true

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: never

logging:
  level:
    root: INFO
    org.gualsh.demo.gw: INFO
```

2. **Создайте Dockerfile:**

```dockerfile
FROM openjdk:17-jre-slim

WORKDIR /app

COPY target/spring-cloud-gateway-demo-1.0.0.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
```

3. **Сборка Docker образа:**

```bash
# Сборка приложения
mvn clean package

# Сборка Docker образа
docker build -t gateway-demo:1.0.0 .

# Запуск контейнера
docker run -d \
  --name gateway-demo \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e REDIS_HOST=redis-server \
  gateway-demo:1.0.0
```

### Kubernetes deployment

1. **Создайте deployment.yaml:**

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: gateway-demo
spec:
  replicas: 3
  selector:
    matchLabels:
      app: gateway-demo
  template:
    metadata:
      labels:
        app: gateway-demo
    spec:
      containers:
      - name: gateway-demo
        image: gateway-demo:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: REDIS_HOST
          value: "redis-service"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
---
apiVersion: v1
kind: Service
metadata:
  name: gateway-demo-service
spec:
  selector:
    app: gateway-demo
  ports:
  - port: 80
    targetPort: 8080
  type: LoadBalancer
```

2. **Развертывание:**

```bash
kubectl apply -f deployment.yaml
kubectl get pods -l app=gateway-demo
kubectl get services
```

## 📈 Оптимизация производительности

### JVM настройки

```bash
# Рекомендуемые JVM параметры для production
java -server \
  -Xmx2g \
  -Xms2g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+UseCompressedOops \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/tmp/heapdump.hprof \
  -jar target/spring-cloud-gateway-demo-1.0.0.jar
```

### Netty настройки

```yaml
server:
  netty:
    connection-timeout: 2s
    h2c-max-content-length: 0B
    initial-buffer-size: 128
    max-chunk-size: 8192
    max-initial-line-length: 4096
    validate-headers: true
```

### Redis настройки

```yaml
spring:
  data:
    redis:
      lettuce:
        pool:
          max-active: 16
          max-idle: 8
          min-idle: 2
          max-wait: -1ms
        cluster:
          refresh:
            adaptive: true
            period: 30s
```

## 🔍 Отладка и логирование

### Включение детального логирования

```yaml
logging:
  level:
    org.springframework.cloud.gateway: DEBUG
    org.springframework.cloud.gateway.filter: TRACE
    org.springframework.cloud.gateway.route: DEBUG
    org.springframework.cloud.gateway.handler: DEBUG
    reactor.netty: DEBUG
    org.gualsh.demo.gw: DEBUG
```

### Структурированное логирование

Для production рекомендуется использовать JSON формат:

```yaml
logging:
  pattern:
    console: '{"timestamp":"%d","level":"%-5level","thread":"%thread","logger":"%logger{36}","message":"%msg","context":"%X"}%n'
```

### Мониторинг логов

```bash
# Просмотр логов в реальном времени
tail -f logs/spring.log

# Фильтрация по уровню
grep "ERROR" logs/spring.log

# Анализ производительности
grep "Request completed" logs/spring.log | awk '{print $NF}' | sort -n
```

## 🧪 Тестирование

### Нагрузочное тестирование

Используйте Apache Bench для базового тестирования:

```bash
# Тест базового маршрута
ab -n 1000 -c 10 http://localhost:8080/demo/get

# Тест rate limiting
ab -n 100 -c 5 http://localhost:8080/rate-limited/get
```

Для более сложного тестирования используйте JMeter или Gatling.

### Тестирование circuit breaker

```bash
# Симуляция медленных ответов
curl "http://localhost:8080/circuit-breaker/delay/5"

# Симуляция ошибок
for i in {1..10}; do
  curl "http://localhost:8080/circuit-breaker/status/500"
done

# Проверка fallback
curl "http://localhost:8080/circuit-breaker/get"
```

## 📋 Checklist для production

### Перед развертыванием

- [ ] Настроен HTTPS
- [ ] Настроена аутентификация
- [ ] Настроен мониторинг
- [ ] Настроены алерты
- [ ] Проведено нагрузочное тестирование
- [ ] Настроен backup для Redis
- [ ] Настроен health check
- [ ] Проверены security заголовки
- [ ] Настроен rate limiting
- [ ] Проверен circuit breaker
- [ ] Настроено логирование
- [ ] Проверена конфигурация CORS

### После развертывания

- [ ] Проверить health endpoints
- [ ] Проверить метрики
- [ ] Проверить логи
- [ ] Провести smoke testing
- [ ] Настроить мониторинг
- [ ] Проверить алерты

## 🆘 Служба поддержки

### Сбор информации для диагностики

При возникновении проблем соберите следующую информацию:

```bash
# Версия Java
java -version

# Версия Maven
mvn -version

# Статус Redis
redis-cli ping

# Логи приложения
tail -100 logs/spring.log

# Информация о системе
curl http://localhost:8080/actuator/info

# Состояние здоровья
curl http://localhost:8080/actuator/health

# Активные маршруты
curl http://localhost:8080/actuator/gateway/routes
```

### Полезные команды

```bash
# Проверка портов
netstat -tulpn | grep :8080

# Проверка процессов
ps aux | grep java

# Проверка памяти
free -h

# Проверка диска
df -h

# Проверка сети
ping google.com
```

## 📚 Дополнительные материалы

### Рекомендуемое чтение

- [Spring Cloud Gateway Documentation](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/)
- [Reactor Core Documentation](https://projectreactor.io/docs/core/release/reference/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Resilience4j Documentation](https://resilience4j.readme.io/docs)

### Полезные ссылки

- [Spring Cloud Gateway Samples](https://github.com/spring-cloud/spring-cloud-gateway)
- [Reactive Programming Guide](https://github.com/reactor/reactor-core)
- [Micrometer Documentation](https://micrometer.io/docs)
- [Redis Documentation](https://redis.io/documentation)

---

**Важное напоминание**: Этот проект предназначен для образовательных целей. Для production использования обязательно адаптируйте конфигурацию под ваши требования безопасности и производительности.