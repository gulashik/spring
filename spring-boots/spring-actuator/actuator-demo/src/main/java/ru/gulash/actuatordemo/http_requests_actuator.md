
Эндпоинты Actuator будут доступны по адресу http://localhost:8080/actuator

Эндпоинты Spring Boot Actuator</br>

GET /actuator — Корневой эндпоинт, список всех доступных эндпоинтов — JSON со ссылками на все активные эндпоинты
```shell
curl --request GET -sL \
     --url 'http://localhost:8080/actuator' \
     | jq -C
```

POST /actuator/shutdown - Выключение приложения
```shell
curl -X POST http://localhost:8080/actuator/shutdown
```

GET /actuator/custom
```shell
curl --request GET -sL \
     --url 'http://localhost:8080/actuator/custom' | jq -C
```

GET /actuator/health — Состояние здоровья приложения 
```shell
curl --request GET -sL \
     --url 'http://localhost:8080/actuator/health' | jq -C
```

GET /actuator/info - Информация о приложении
```shell
curl --request GET -sL \
     --url 'http://localhost:8080/actuator/info' | jq -C
```

GET /actuator/metrics - Список доступных метрик
```shell
curl --request GET -sL \
     --url 'http://localhost:8080/actuator/metrics' | jq -C
```

GET /actuator/prometheus - Метрики в формате Prometheus
```shell
curl --request GET -sL \
     --url 'http://localhost:8080/actuator/prometheus' 
```

GET /actuator/env - Информация о переменных окружения
```shell
curl --request GET -sL \
     --url 'http://localhost:8080/actuator/env'  | jq -C
```

GET /actuator/scheduledtasks - Информация о запланированных задачах
```shell
curl --request GET -sL \
     --url 'http://localhost:8080/actuator/scheduledtasks'  | jq -C
```

GET /actuator/loggers — Информация о логгерах и их уровнях
```shell
curl --request GET -sL \
     --url 'http://localhost:8080/actuator/loggers'  | jq -C
```

GET /actuator/threaddump — Информация о всех потоках приложения
```shell
curl --request GET -sL \
     --url 'http://localhost:8080/actuator/threaddump'  | jq -C
```

GET /actuator/mappings - Информация о всех маппингах контроллеров
```shell
curl --request GET -sL \
     --url 'http://localhost:8080/actuator/mappings'  | jq -C
```

GET /actuator/beans — Список всех бинов и их зависимостей
```shell
curl --request GET -sL \
     --url 'http://localhost:8080/actuator/beans'  | jq -C
```

GET /actuator/conditions - Информация о примененных условиях автоконфигурации
```shell
curl --request GET -sL \
     --url 'http://localhost:8080/actuator/conditions'  | jq -C
```

GET /actuator/configprops - Конфигурационные свойства приложения
```shell
curl --request GET -sL \
     --url 'http://localhost:8080/actuator/configprops'  | jq -C
```
