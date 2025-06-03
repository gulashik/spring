
```shell
# Запускаем приложение
podman compose down -v
clear
podman compose up -d
podman compose ps
```

```shell
# Дожидаемся корректного состояния приложения
clear
curl http://localhost:8080/actuator/health | jq
```

```shell
# Создание пользователя
clear
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com"
  }' | jq
```

```shell
# Получение всех пользователей
clear
curl http://localhost:8080/api/users | jq
```

```shell
# Обновление пользователя
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Smith",
    "email": "john.smith@example.com"
  }' | jq
```

```shell
clear
podman compose down -v
```