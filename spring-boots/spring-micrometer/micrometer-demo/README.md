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