
Эндпоинты Actuator будут доступны по адресу http://localhost:8080/actuator

Эндпоинты Spring Boot Actuator</br>

GET /actuator — Корневой эндпоинт, список всех доступных эндпоинтов — JSON со ссылками на все активные эндпоинты
```shell
curl --request GET -sL \
     --url 'http://localhost:8080/actuator' \
     | jq -C
```

GET GET /actuator/health - Состояние здоровья приложения 
```shell
curl --request GET -sL \
     --url 'http://localhost:8080/actuator/health' | yq -P
```
