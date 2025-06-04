
```shell
# Останавливеам если запущено приложение
clear
podman compose down -v
```

-----Docker-------------
Создание
./gradlew jibDockerBuild
Запуск
docker-compose up -d
-----------------------

-----Podman-------------
```shell
# Создание tar архива если
clear
./gradlew clean jibBuildTar
```

```shell
# Загрузка образа в Podman
clear
podman load < build/jib-image.tar
```

```shell
# Запуск docker-compose.yml через podman
clear
podman compose up -d
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