# Spring WebClient Demo

### Примеры запросов

**Проверка работоспособности**
```bash
clear
curl -s http://localhost:8080/actuator/health | jq
```

**Запрос списка пользователей:**
Что демонстрирует:
- Правильную десериализацию `List<UserDto>`
- Использование `ParameterizedTypeReference<List<UserDto>>()`
- Кэширование результата (повторный запрос будет быстрее)
```bash
curl -s -X GET "http://localhost:8080/api/v1/jsonplaceholder/users" \
  -H "Accept: application/json" | jq
```

**Создание поста:**
```bash
clear
curl -s -X POST http://localhost:8080/api/v1/jsonplaceholder/posts \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "title": "My New Post",
    "body": "This is the content of my new post"
  }' | jq
```
**Обновление поста (PUT):**
```bash
clear
curl -s -X PUT "http://localhost:8080/api/v1/jsonplaceholder/posts/1" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "userId": 1,
    "title": "Updated Demo Post",
    "body": "This post has been updated"
  }' | jq .
```

**Частичное обновление поста:**
```bash
clear
curl -s -X PATCH http://localhost:8080/api/v1/jsonplaceholder/posts/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Updated Title"
  }' | jq
```

**Удаление поста (DELETE):**
```bash
clear
curl -X DELETE "http://localhost:8080/api/v1/jsonplaceholder/posts/1"
```

**Получение постов пользователя как стрим:**
```bash
curl -N -H "Accept: text/event-stream" \
  "http://localhost:8080/api/v1/jsonplaceholder/users/1/posts"
```

**Получение нескольких пользователей одновременно:**
```bash
curl -X POST http://localhost:8080/api/v1/jsonplaceholder/users/batch \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -d '[1, 2, 3, 4, 5]'
```

**Получение постов с пагинацией:**
```bash
clear
curl -s "http://localhost:8080/api/v1/jsonplaceholder/posts?page=0&size=5" | jq .
```

**Проверка заголовков пагинации:**
```bash
clear
curl -I "http://localhost:8080/api/v1/jsonplaceholder/posts?page=1&size=3"
```

**Получение профиля пользователя (пользователь + его посты):**
```bash
clear 
curl "http://localhost:8080/api/v1/jsonplaceholder/users/1/profile" | jq .
```

**Кэширование с @EnableCaching**

**Первый запрос пользователя (медленный):**
```bash
time curl "http://localhost:8080/api/v1/jsonplaceholder/users/1" | jq
```
**Второй запрос пользователя (быстрый из кэша):**
```bash
time curl "http://localhost:8080/api/v1/jsonplaceholder/users/1"
```

**Проверка кэша:**
```bash
clear
curl -s http://localhost:8080/actuator/caches | jq
```

**Метрики WebClient**

```bash
# HTTP клиентские метрики
clear
curl http://localhost:8080/actuator/metrics/http.client.requests
```
```bash
# Метрики кэша
clear
curl -s http://localhost:8080/actuator/metrics/cache.gets | jq
```
```bash
# JVM метрики
clear
curl -s http://localhost:8080/actuator/metrics/jvm.memory.used | jq
```