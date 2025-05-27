# 🚀 Инструкция по демонстрации Spring WebClient проекта

## 📋 Быстрый старт

### 1. Запуск приложения

```bash
# Клонирование проекта
git clone <repository-url>
cd webclient-demo

# Установка Weather API ключа (опционально)
export WEATHER_API_KEY=your-openweathermap-api-key

# Запуск приложения
mvn spring-boot:run

# Проверка работоспособности
curl http://localhost:8080/actuator/health
```

### 2. Базовые проверки

**Health Check:**
```bash
curl http://localhost:8080/actuator/health
# Ожидаемый ответ: {"status":"UP"}
```

**Metrics:**
```bash
curl http://localhost:8080/actuator/metrics
# Показывает доступные метрики
```

## 🎯 Демонстрация ключевых возможностей

### 1. ParameterizedTypeReference для generic типов

**Запрос списка пользователей:**
```bash
curl -X GET "http://localhost:8080/api/v1/jsonplaceholder/users" \
  -H "Accept: application/json" | jq .
```

**Что демонстрирует:**
- ✅ Правильную десериализацию `List<UserDto>`
- ✅ Использование `ParameterizedTypeReference<List<UserDto>>()`
- ✅ Кэширование результата (повторный запрос будет быстрее)

### 2. Кэширование с @EnableCaching

**Первый запрос пользователя (медленный):**
```bash
time curl "http://localhost:8080/api/v1/jsonplaceholder/users/1"
```

**Второй запрос пользователя (быстрый из кэша):**
```bash
time curl "http://localhost:8080/api/v1/jsonplaceholder/users/1"
```

**Проверка кэша:**
```bash
curl http://localhost:8080/actuator/caches
```

### 3. Retry механизм с @EnableRetry

**Принудительная проверка retry** (используя несуществующий ID):
```bash
curl "http://localhost:8080/api/v1/jsonplaceholder/users/999"
# Будет несколько попыток, потом ошибка
```

**Логи покажут retry попытки:**
```
2024-01-01 12:00:01 DEBUG - Request: GET https://jsonplaceholder.typicode.com/users/999
2024-01-01 12:00:02 WARN  - Retrying request, attempt 1
2024-01-01 12:00:04 WARN  - Retrying request, attempt 2
2024-01-01 12:00:08 ERROR - Retry exhausted after 3 attempts
```

### 4. Reactive Streams с Server-Sent Events

**Получение постов пользователя как стрим:**
```bash
curl -N -H "Accept: text/event-stream" \
  "http://localhost:8080/api/v1/jsonplaceholder/users/1/posts"
```

**Что демонстрирует:**
- ✅ Flux вместо Mono
- ✅ Streaming данных в реальном времени
- ✅ Content-Type: text/event-stream

### 5. CRUD операции

**Создание поста (POST):**
```bash
curl -X POST "http://localhost:8080/api/v1/jsonplaceholder/posts" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "title": "Demo Post",
    "body": "This is a demonstration post created via WebClient"
  }' | jq .
```

**Обновление поста (PUT):**
```bash
curl -X PUT "http://localhost:8080/api/v1/jsonplaceholder/posts/1" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "userId": 1,
    "title": "Updated Demo Post",
    "body": "This post has been updated"
  }' | jq .
```

**Частичное обновление (PATCH):**
```bash
curl -X PATCH "http://localhost:8080/api/v1/jsonplaceholder/posts/1" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Partially Updated Title"
  }' | jq .
```

**Удаление поста (DELETE):**
```bash
curl -X DELETE "http://localhost:8080/api/v1/jsonplaceholder/posts/1"
# Ожидаемый ответ: HTTP 204 No Content
```

### 6. Пагинация

**Получение постов с пагинацией:**
```bash
curl "http://localhost:8080/api/v1/jsonplaceholder/posts?page=0&size=5" | jq .
```

**Проверка заголовков пагинации:**
```bash
curl -I "http://localhost:8080/api/v1/jsonplaceholder/posts?page=1&size=3"
# Ожидаемые заголовки: X-Page, X-Size, X-Total
```

### 7. Композиция запросов

**Получение профиля пользователя (пользователь + его посты):**
```bash
curl "http://localhost:8080/api/v1/jsonplaceholder/users/1/profile" | jq .
```

**Что демонстрирует:**
- ✅ Mono.zip() для композиции
- ✅ Параллельное выполнение запросов
- ✅ Объединение результатов

### 8. Batch операции

**Получение нескольких пользователей одновременно:**
```bash
curl -X POST "http://localhost:8080/api/v1/jsonplaceholder/users/batch" \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -d '[1, 2, 3, 4, 5]' | jq -c .
```

### 9. Weather API (с API ключами)

**Получение погоды:**
```bash
curl "http://localhost:8080/api/v1/weather/current?city=London" | jq .
```

**Погода по координатам:**
```bash
curl "http://localhost:8080/api/v1/weather/coordinates?lat=51.5&lon=-0.1" | jq .
```

**Проверка доступности weather сервиса:**
```bash
curl "http://localhost:8080/api/v1/weather/health" | jq .
```

### 10. Обработка ошибок

**404 ошибка:**
```bash
curl "http://localhost:8080/api/v1/jsonplaceholder/users/999"
# Ожидается структурированная ошибка
```

**Валидационные ошибки:**
```bash
curl -X POST "http://localhost:8080/api/v1/jsonplaceholder/posts" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "title": "",
    "body": "Valid body"
  }'
# Ожидается ошибка валидации для пустого title
```

## 📊 Мониторинг и метрики

### Actuator Endpoints

```bash
# Общая информация о приложении
curl http://localhost:8080/actuator/info

# Метрики WebClient
curl http://localhost:8080/actuator/metrics/http.client.requests

# Метрики кэша
curl http://localhost:8080/actuator/metrics/cache.gets

# Состояние кэшей
curl http://localhost:8080/actuator/caches

# HTTP trace
curl http://localhost:8080/actuator/httptrace
```

### Логирование

**Включение DEBUG логов для WebClient:**
```bash
# В application.yml уже настроено:
logging:
  level:
    org.springframework.web.reactive.function.client: DEBUG
```

**Просмотр логов в реальном времени:**
```bash
tail -f logs/application.log | grep -E "(WebClient|HTTP|Cache|Retry)"
```

## 🐳 Docker демонстрация

### Запуск с Docker Compose

```bash
# Запуск всей инфраструктуры
docker-compose up -d

# Проверка статуса сервисов
docker-compose ps

# Просмотр логов приложения
docker-compose logs -f webclient-demo
```

### Доступ к сервисам

- **Приложение**: http://localhost:8080
- **Grafana**: http://localhost:3000 (admin/admin123)
- **Prometheus**: http://localhost:9090
- **WireMock**: http://localhost:8089
- **Redis**: localhost:6379

### Grafana Dashboard

1. Откройте http://localhost:3000
2. Войдите как admin/admin123
3. Перейдите в Dashboards
4. Выберите "Spring Boot WebClient Dashboard"
5. Наблюдайте метрики в реальном времени

## 🧪 Тестирование

### Unit тесты

```bash
# Запуск unit тестов
mvn test -Dtest="*Test"

# Конкретный тест класс
mvn test -Dtest="JsonPlaceholderServiceTest"

# С подробным выводом
mvn test -Dtest="JsonPlaceholderServiceTest" -X
```

### Интеграционные тесты

```bash
# Запуск интеграционных тестов
mvn test -Dtest="*IntegrationTest"

# С Spring Boot Test контекстом
mvn test -Dtest="WebClientIntegrationTest"
```

### Тесты с покрытием

```bash
# Генерация отчета о покрытии
mvn clean test jacoco:report

# Просмотр отчета
open target/site/jacoco/index.html
```

## 📝 Демонстрационный сценарий

### Сценарий 1: Основные возможности (5 минут)

1. **Запуск приложения** и проверка health
2. **Получение пользователей** - демонстрация ParameterizedTypeReference
3. **Повторный запрос** - демонстрация кэширования
4. **Создание поста** - демонстрация POST запроса
5. **Server-Sent Events** - демонстрация streaming

### Сценарий 2: Обработка ошибок (3 минуты)

1. **404 ошибка** - несуществующий пользователь
2. **Валидация** - невалидные данные для поста
3. **Retry механизм** - имитация сетевой ошибки
4. **Graceful degradation** - fallback значения

### Сценарий 3: Производительность (3 минуты)

1. **Batch запросы** - получение нескольких пользователей
2. **Композиция** - пользователь с постами
3. **Кэширование** - сравнение времени ответа
4. **Метрики** - просмотр в Actuator

### Сценарий 4: Docker и мониторинг (4 минуты)

1. **Docker Compose** - запуск инфраструктуры
2. **Grafana** - просмотр дашбордов
3. **Prometheus** - метрики в реальном времени
4. **Логирование** - анализ логов приложения

## 🔧 Решение проблем

### Частые проблемы

**1. Порт 8080 занят:**
```bash
# Найти процесс
lsof -i :8080
# Или запустить на другом порту
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

**2. Weather API недоступен:**
```bash
# Проверить API ключ
curl "https://api.openweathermap.org/data/2.5/weather?q=London&appid=YOUR_API_KEY"
```

**3. Docker проблемы:**
```bash
# Пересборка образов
docker-compose build --no-cache

# Очистка и перезапуск
docker-compose down -v
docker-compose up -d
```

**4. Тесты падают:**
```bash
# Проверка сетевого доступа
curl https://jsonplaceholder.typicode.com/users

# Запуск без сетевых тестов
mvn test -Dtest="!*IntegrationTest"
```

## 📚 Дополнительные материалы

- **Swagger UI**: http://localhost:8080/swagger-ui.html (если добавлен)
- **API Docs**: http://localhost:8080/v3/api-docs
- **Spring Boot Admin**: Можно добавить для расширенного мониторинга
- **OpenAPI спецификация**: Для документирования API

---

**Время полной демонстрации**: ~15-20 минут  
**Минимальная демонстрация**: ~5 минут (сценарий 1)  
**Расширенная демонстрация**: ~30 минут (все сценарии + Q&A)