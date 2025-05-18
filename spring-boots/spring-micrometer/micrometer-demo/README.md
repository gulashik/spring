Демонстрационный проект Spring Boot, показывающий возможности Micrometer для мониторинга.

### Запуск с Docker Compose
```bash
# Сборка образа и запуск всех сервисов
podman compose up -d
```
```bash
# Статус
podman compose ps
```
После запуска сервисы будут доступны по следующим адресам:
- Spring Boot REST будеn доступен по: http://localhost:8080
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (логин: admin, пароль: admin)
- Zipkin: http://localhost:9411

```bash
# Для остановки сервисов
podman compose down
```


## Мониторинг

### Prometheus
Приложение экспортирует метрики в формате Prometheus через эндпоинт `/actuator/prometheus`. Prometheus настроен на сбор метрик каждые 15 секунд.

Основные запросы Prometheus:
```
# HTTP запросы в секунду
rate(http_server_requests_seconds_count{job="spring-boot-app"}[1m])

# 95 процентиль времени ответа
histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket{job="spring-boot-app"}[1m])) by (le, uri))

# Количество заказов в секунду
rate(business_orders_total{job="spring-boot-app"}[1m])

# Использование CPU
system_cpu_usage{job="spring-boot-app"}

# Использование памяти JVM
jvm_memory_used_bytes{job="spring-boot-app", area="heap"}
```

### Grafana
## Войдите в Grafana
После запуска перейдите по адресу [http://localhost:3000](http://localhost:3000) и войдите в Grafana, используя:
- Логин: `admin`
- Пароль: `admin`

## Проверьте источник данных Prometheus
В вашем проекте уже настроен источник данных Prometheus через файл конфигурации . Чтобы убедиться, что он работает: `grafana/provisioning/datasources/datasource.yml`
1. Нажмите значок шестеренки ⚙️ в левом боковом меню
2. Выберите "Data Sources"
3. Убедитесь, что источник данных "Prometheus" присутствует и настроен

## Создайте новый дашборд
1. Нажмите на "+" в левом боковом меню
2. Выберите "New Dashboard" (Новый дашборд)
3. Нажмите "Add visualization" (Добавить визуализацию) или "Add new panel" (Добавить новую панель)

## Создайте панели с готовыми запросами
Теперь вы можете создать панели с готовыми запросами Prometheus, которые упоминаются в вашем проекте. Для каждой панели:
1. Выберите "Prometheus" как источник данных (должен быть выбран по умолчанию)
2. В редакторе запросов вставьте нужный запрос из перечисленных ниже:

### Панель для HTTP запросов в секунду
``` 
rate(http_server_requests_seconds_count{job="spring-boot-app"}[1m])
```
### Панель для 95 процентиля времени ответа
``` 
histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket{job="spring-boot-app"}[1m])) by (le, uri))
```
### Панель для количества заказов в секунду
``` 
rate(business_orders_total{job="spring-boot-app"}[1m])
```
### Панель для использования CPU
``` 
system_cpu_usage{job="spring-boot-app"}
```
### Панель для использования памяти JVM
``` 
jvm_memory_used_bytes{job="spring-boot-app", area="heap"}
```
## Настройте визуализацию для каждой панели
Для каждой панели вы можете выбрать тип визуализации [[1]](https://grafana.com/docs/grafana/latest/getting-started/build-first-dashboard/):
1. В настройках панели перейдите на вкладку "Panel options" (Параметры панели)
2. В зависимости от типа данных выберите подходящую визуализацию:
    - Для метрик типа "rate" (HTTP запросы/сек, заказы/сек) подойдет график (Graph) или линейная диаграмма (Time series)
    - Для процентилей также подойдет график (Graph)
    - Для использования CPU и памяти можно использовать графики, счетчики (Gauge) или гистограммы

## Организуйте дашборд
1. Перетаскивайте и изменяйте размер панелей для удобного отображения
2. Добавьте название для дашборда и описание, если необходимо
3. Настройте временной интервал обновления данных (в правом верхнем углу)

## Сохраните дашборд
После настройки всех панелей нажмите на иконку дискеты (или "Save dashboard") в правом верхнем углу и дайте дашборду название, например, "Spring Boot Micrometer Metrics".
## Проверьте работу дашборда
Чтобы проверить, что дашборд правильно отображает данные, запустите нагрузочные тесты с помощью предоставленных в проекте команд:

``` bash
curl -s 'http://localhost:8080/api/load-test?seconds=30' | jq
```

-------------------

## Примеры запросов
### Создание заказа
```bash
curl -s \
  -X POST \
  -H "Content-Type: application/json" -d '{"amount": 350.5, "region": "eu"}' \
  http://localhost:8080/api/orders \
  | jq
```

### Запуск нагрузочного тестирования
```bash
clear
curl -s 'http://localhost:8080/api/load-test?seconds=30' | jq
```

### Тестирование метрик трассировки
```bash
clear
curl -s 'http://localhost:8080/api/tracing/demo' | jq
```

### Получение списка всех метрик
```bash
clear
curl 'http://localhost:8080/api/metrics/list' | jq
```

### Получение информации о конкретной метрике
```bash
clear
curl -s 'http://localhost:8080/api/metrics/info?name=api.resources.request' | jq
```

// оставить
## API Endpoints

### Основные эндпоинты
- **GET /api/status** - получение статуса сервера (демонстрация @Timed)
- **GET /api/resources** - получение информации о ресурсах (демонстрация ручного Timer)
- **POST /api/orders** - создание заказа (демонстрация бизнес-метрик)
- **GET /api/error/{type}** - генерация ошибок для тестирования
- **GET /api/load-test** - генерация нагрузки для тестирования метрик

### Метрики
- **GET /api/metrics/list** - список всех доступных метрик
- **GET /api/metrics/search?prefix={prefix}** - поиск метрик по префиксу
- **GET /api/metrics/info?name={name}** - информация о конкретной метрике
- **GET /api/metrics/summary** - сводная информация о метриках

### Трассировка
- **GET /api/tracing/demo** - демонстрация трассировки
- **GET /api/tracing/operation?operationName={name}** - пользовательская операция с трассировкой
- **GET /api/tracing/resource/{id}** - получение ресурса с трассировкой
- **GET /api/tracing/error** - генерация ошибки для трассировки

### Счетчики
- **POST /api/counters/operation?value={value}** - выполнение операции с счетчиками
- **GET /api/counters/count/{type}** - демонстрация @Counted
- **GET /api/counters/process?parameter={param}** - демонстрация комбинации @Timed и @Counted
- **POST /api/counters/dynamic/{category}** - создание динамического счетчика

### Actuator

Приложение имеет полный набор эндпоинтов Spring Boot Actuator:
- **GET /actuator/health** - информация о здоровье приложения
- **GET /actuator/info** - информация о приложении
- **GET /actuator/metrics** - список доступных метрик
- **GET /actuator/metrics/{name}** - детали конкретной метрики
- **GET /actuator/prometheus** - экспорт метрик в формате Prometheus