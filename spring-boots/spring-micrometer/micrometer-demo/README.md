# Spring Boot Actuator и Micrometer Demo

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