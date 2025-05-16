
### Собрать и запустить всю инфраструктуру (приложение, Prometheus, Grafana)
```bash
podman compose up -d
```
### Доступ к UI После запуска с Docker Compose:

- **Приложение**: http://localhost:8080
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin123)


### Эндпоинты API

### Основные эндпоинты приложения

`GET /api/hello` - Базовый эндпоинт с использованием аннотации @Timed. Получаем приветствие
```bash
curl http://localhost:8080/api/hello
```

`GET /api/stats` - Эндпоинт с демонстрацией аннотации @Counted. Получаем статистику
```bash
# Получаем статистику
curl http://localhost:8080/api/stats
```

`GET /api/task/{seconds}` - Эндпоинт для демонстрации Observation API
```bash
# Выполняем задачу с задержкой 3 секунды
curl http://localhost:8080/api/task/3
```

`GET /api/test-error?shouldFail=true|false` - Эндпоинт для демонстрации метрик ошибок
```bash
# Тестируем обработку ошибок
curl http://localhost:8080/api/test-error?shouldFail=true
```

`POST /api/load?intensity=1-10` - Эндпоинт для генерации нагрузки
```bash
curl -X POST http://localhost:8080/api/load?intensity=5
```

### Actuator эндпоинты

`GET /actuator` - Список доступных эндпоинтов

`GET /actuator/health` - Состояние приложения
```bash
curl http://localhost:8080/actuator/health
```

`GET /actuator/info` - Информация о приложении
```bash
curl http://localhost:8080/actuator/info
```

`GET /actuator/metrics` - Список доступных метрик
```bash
curl http://localhost:8080/actuator/metrics
```

`GET /actuator/metrics/{name}` - Подробная информация о конкретной метрике
```bash
curl http://localhost:8080/actuator/metrics/http.server.requests
```

`GET /actuator/prometheus` - Метрики в формате Prometheus
```bash
curl http://localhost:8080/actuator/prometheus
```
