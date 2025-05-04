
Эндпоинты Actuator будут доступны по адресу http://localhost:8080/actuator

Демонстрационные эндпоинты приложения</br>

GET /api/hello - Приветственное сообщение с увеличением счетчика
```shell
curl --request GET -sL \
     --url 'http://localhost:8080/api/hello'
```

GET /api/delay/{seconds} - Ответ с задержкой для демонстрации таймеров
```shell
curl --request GET -sL \
     --url 'http://localhost:8080/api/delay/2'
```

GET /api/error - Генерация исключения для демонстрации метрик ошибок
```shell
curl --request GET -sL \
     --url 'http://localhost:8080/api/error'
```

Эндпоинты для метрик</br>

GET /api/metrics/timed/{input} - Демонстрация метода с аннотацией @Timed
```shell
curl --request GET -sL \
     --url 'http://localhost:8080/api/metrics/timed/some_input'
```

GET /api/metrics/manual/{input} - Демонстрация метода с ручным измерением через Timer
```shell
curl --request GET -sL \
     --url 'http://localhost:8080/api/metrics/manual/some_input'
```

