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
