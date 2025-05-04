
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

Эндпоинты для пользователей</br>

GET /api/users - Получить всех пользователей
```shell
curl --request GET -sL \
     --url 'http://localhost:8080/api/users'
```

GET /api/users/{id} - Получить пользователя по ID
```shell
curl --request GET -sL \
     --url 'http://localhost:8080/api/users/1'
```

POST /api/users - Создать нового пользователя
```shell
curl --request POST -L \
     --url 'http://localhost:8080/api/users' \
     --header 'Content-Type: application/json' \
     --data '{
       "username": "новый_пользователь", 
       "email": "user@example.com", 
       "password": "пароль123"
     }'
```

PUT /api/users/{id} - Обновить пользователя
```shell
curl --request PUT -sL \
     --url 'http://localhost:8080/api/users/1' \
     --header 'Content-Type: application/json' \
     --data '{
       "username": "обновленное_имя", 
       "email": "updated@example.com", 
       "active": true
     }'
```

DELETE /api/users/{id} - Удалить пользователя
```shell
curl --request DELETE -sL \
     --url 'http://localhost:8080/api/users/1'
```

GET /api/users/search?username={username} - Поиск пользователей по имени. Кирилица не канает.
```shell
curl --request GET -sL \
     --url 'http://localhost:8080/api/users/search?username=%D0%BD%D0%BE%D0%B2%D1%8B%D0%B9_%D0%BF%D0%BE%D0%BB%D1%8C%D0%B7%D0%BE%D0%B2%D0%B0%D1%82%D0%B5%D0%BB%D1%8C'
```
