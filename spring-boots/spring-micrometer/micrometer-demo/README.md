# Spring Boot Actuator и Micrometer Demo

## Запуск приложения

### Локальный запуск

### Запуск с Docker Compose

```bash
# Собрать и запустить всю инфраструктуру (приложение, Prometheus, Grafana)
docker-compose up -d
```

## Эндпоинты API

### Основные эндпоинты приложения

- `GET /api/hello` - Базовый эндпоинт с использованием аннотации @Timed
- `GET /api/stats` - Эндпоинт с демонстрацией аннотации @Counted
- `GET /api/task/{seconds}` - Эндпоинт для демонстрации Observation API
- `GET /api/test-error?shouldFail=true|false` - Эндпоинт для демонстрации метрик ошибок
- `POST /api/load?intensity=1-10` - Эндпоинт для генерации нагрузки

### Actuator эндпоинты

- `GET /actuator` - Список доступных эндпоинтов
- `GET /actuator/health` - Состояние приложения
- `GET /actuator/info` - Информация о приложении
- `GET /actuator/metrics` - Список доступных метрик
- `GET /actuator/metrics/{name}` - Подробная информация о конкретной метрике
- `GET /actuator/prometheus` - Метрики в формате Prometheus

## Примеры использования

### Получение состояния приложения

```bash
curl http://localhost:8080/actuator/health
```

Ответ:
```json
{
  "status": "UP",
  "components": {
    "custom": {
      "status": "UP",
      "details": {
        "externalSystem": "available",
        "responseTime": "75ms",
        "errorCount": 2,
        "lastChecked": 1652345678901
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 1000000000,
        "free": 500000000,
        "threshold": 10000000
      }
    }
  }
}
```

### Получение информации о приложении

```bash
curl http://localhost:8080/actuator/info
```

Ответ:
```json
{
  "app": {
    "name": "Micrometer and Actuator Demo",
    "description": "Demo application for Spring Boot Actuator and Micrometer",
    "version": "1.0.0",
    "java": {
      "version": "21"
    }
  },
  "java": {
    "version": "21.0.1",
    "vendor": {
      "name": "Eclipse Adoptium"
    }
  }
}
```

### Получение списка метрик

```bash
curl http://localhost:8080/actuator/metrics
```

Ответ:
```json
{
  "names": [
    "jvm.memory.used",
    "jvm.memory.max",
    "http.server.requests",
    "process.cpu.usage",
    "custom.counter",
    "api.calls.total",
    "task.count",
    "task.duration",
    "exceptions.runtime"
  ]
}
```

### Получение конкретной метрики

```bash
curl http://localhost:8080/actuator/metrics/http.server.requests
```

Ответ:
```json
{
  "name": "http.server.requests",
  "description": null,
  "baseUnit": "seconds",
  "measurements": [
    {
      "statistic": "COUNT",
      "value": 42
    },
    {
      "statistic": "TOTAL_TIME",
      "value": 5.3
    },
    {
      "statistic": "MAX",
      "value": 0.23
    }
  ],
  "availableTags": [
    {
      "tag": "uri",
      "values": [
        "/api/hello",
        "/api/stats",
        "/actuator/metrics"
      ]
    },
    {
      "tag": "status",
      "values": [
        "200",
        "404",
        "500"
      ]
    }
  ]
}
```

### Получение метрик в формате Prometheus

```bash
curl http://localhost:8080/actuator/prometheus
```

Ответ (часть вывода):
```
# HELP http_server_requests_seconds  
# TYPE http_server_requests_seconds histogram
http_server_requests_seconds_bucket{application="micromet-demo",uri="/api/hello",method="GET",status="200",le="0.05"} 1.0
http_server_requests_seconds_bucket{application="micromet-demo",uri="/api/hello",method="GET",status="200",le="0.1"} 2.0
...
# HELP jvm_memory_used_bytes The amount of used memory
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{application="micromet-demo",area="heap"} 2.06045184E8
...
```

### Генерация нагрузки

```bash
curl -X POST http://localhost:8080/api/load?intensity=5
```

### Проверка демонстрационных API

```bash
# Получаем приветствие
curl http://localhost:8080/api/hello

# Получаем статистику
curl http://localhost:8080/api/stats

# Выполняем задачу с задержкой 3 секунды
curl http://localhost:8080/api/task/3

# Тестируем обработку ошибок
curl http://localhost:8080/api/test-error?shouldFail=true
```

## Доступ к UI

После запуска с Docker Compose:

- **Приложение**: http://localhost:8080
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin123)

### Настройка Grafana

1. Откройте Grafana по адресу http://localhost:3000
2. Войдите с использованием admin/admin123
3. Добавьте источник данных Prometheus:
    - Перейдите в Configuration > Data Sources
    - Нажмите "Add data source"
    - Выберите "Prometheus"
    - URL: http://prometheus:9090
    - Нажмите "Save & Test"
4. Импортируйте готовые дашборды для Spring Boot:
    - Перейдите в Create > Import
    - Введите ID дашборда 10280 (JVM Micrometer) или 4701 (Spring Boot Statistics)
    - Выберите ваш источник данных Prometheus
    - Нажмите "Import"

## Архитектура проекта

```
org.gualsh.demo.micromet
├── config                  # Конфигурационные классы
│   └── MetricsConfig.java  # Настройка Micrometer
├── controller              # REST контроллеры
│   └── DemoController.java # Демонстрационный контроллер с метриками
├── service                 # Сервисный слой
│   └── DemoService.java    # Демонстрационный сервис с метриками
├── exception               # Обработка исключений
│   └── CustomExceptionHandler.java # Обработчик с метриками
├── health                  # Индикаторы здоровья для Actuator
│   └── CustomHealthIndicator.java  # Пользовательский индикатор
├── metric                  # Пользовательские метрики
│   └── CustomMetrics.java  # Примеры различных типов метрик
└── MicrometApplication.java # Главный класс приложения
```

## Объяснение важных компонентов

### Spring Boot Actuator

Spring Boot Actuator - это модуль, который добавляет в приложение production-ready возможности:

- **Эндпоинты мониторинга**: Предоставляет готовые HTTP эндпоинты с информацией о приложении
- **Метрики**: Собирает метрики производительности и бизнес-метрики
- **Аудит**: Отслеживает события аудита
- **Здоровье**: Предоставляет информацию о работоспособности приложения

Основные эндпоинты:
- `/actuator/health`: Показывает состояние приложения
- `/actuator/info`: Отображает информацию о приложении
- `/actuator/metrics`: Предоставляет метрики
- `/actuator/env`: Показывает переменные окружения
- `/actuator/loggers`: Позволяет просматривать и менять уровень логирования
- `/actuator/prometheus`: Экспортирует метрики в формате Prometheus

### Micrometer

Micrometer - это библиотека, предоставляющая фасад для инструментирования приложений. Основные особенности:

- **Абстракция над системами мониторинга**: Позволяет настроить приложение один раз, а затем переключаться между различными системами мониторинга
- **Различные типы метрик**: Поддерживает счетчики, таймеры, индикаторы и др.
- **Тегирование**: Мощная система тегов для группировки и фильтрации метрик
- **Интеграция с Spring**: Автоматически настраивает метрики JVM, системные метрики, пулы потоков и т.д.

### Типы метрик

1. **Counter (Счетчик)** - кумулятивное значение, которое может только увеличиваться:
   ```java
   Counter counter = Counter.builder("my.counter")
                     .tag("tag1", "value1")
                     .register(meterRegistry);
   counter.increment();
   ```

2. **Gauge (Индикатор)** - значение, которое может увеличиваться и уменьшаться:
   ```java
   AtomicInteger value = new AtomicInteger(0);
   Gauge.builder("my.gauge", value, AtomicInteger::get)
        .register(meterRegistry);
   value.set(42);
   ```

3. **Timer (Таймер)** - измеряет продолжительность событий:
   ```java
   Timer timer = Timer.builder("my.timer")
               .publishPercentiles(0.5, 0.95)
               .register(meterRegistry);
   
   timer.record(() -> {
       // Код для измерения
   });
   ```

4. **Distribution Summary (Распределение)** - похоже на таймер, но для произвольных значений, не времени:
   ```java
   DistributionSummary summary = DistributionSummary.builder("my.summary")
                                .baseUnit("bytes")
                                .register(meterRegistry);
   summary.record(2048);
   ```

### Аннотации для метрик

1. **@Timed** - измеряет время выполнения метода:
   ```java
   @Timed(value = "my.timed.method", percentiles = {0.5, 0.95})
   public void myMethod() {
       // ...
   }
   ```

2. **@Counted** - подсчитывает вызовы метода:
   ```java
   @Counted("my.counted.method")
   public void myMethod() {
       // ...
   }
   ```

3. **@Observed** - позволяет использовать Observation API (новое в Spring Boot 3.x):
   ```java
   @Observed(name = "my.observation", 
             contextualName = "my-method",
             lowCardinalityKeyValues = {"role", "user"})
   public void myMethod() {
       // ...
   }
   ```

## Best Practices

1. **Тегирование метрик**:
    - Используйте теги для группировки метрик (например, по окружению, региону, компоненту)
    - Ограничивайте кардинальность тегов (слишком много уникальных значений создаст проблемы)
    - Используйте общие теги для всех метрик через MeterRegistryCustomizer

2. **Именование метрик**:
    - Используйте вложенные имена через точку (например, "http.server.requests")
    - Следуйте общим конвенциям для своей команды/организации
    - Используйте единицы измерения в названии или через baseUnit

3. **Безопасность**:
    - В production, ограничивайте доступ к эндпоинтам Actuator через Spring Security
    - Экспонируйте только необходимые эндпоинты, а не все (*) в production

4. **Производительность**:
    - Избегайте чрезмерного создания метрик
    - Используйте метрики только там, где это действительно нужно
    - Фильтруйте метрики с помощью MeterFilter для уменьшения объема данных

5. **Работа с Prometheus**:
    - Настройте scrape_interval в соответствии с потребностями (обычно 15-30 секунд)
    - Используйте метки для фильтрации и группировки
    - Создавайте информативные дашборды в Grafana

## Заключение

Этот проект демонстрирует мощные возможности Spring Boot Actuator и Micrometer для мониторинга приложений. Используя эти инструменты, вы можете:

- Получать данные о производительности и здоровье приложения
- Создавать пользовательские метрики для бизнес-процессов
- Интегрироваться с системами мониторинга, такими как Prometheus и Grafana
- Быстро выявлять и решать проблемы в production-окружении

Помните, что хороший мониторинг - это ключевой аспект надежной и масштабируемой системы в production.