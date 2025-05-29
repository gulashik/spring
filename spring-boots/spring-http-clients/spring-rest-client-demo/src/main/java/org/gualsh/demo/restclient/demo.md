
```bash
# Проверка запуска
curl http://localhost:8080/actuator/health
```

#### Получение всех пользователей
**Что демонстрируется:**
- Базовый GET запрос через RestClient
- Использование ParameterizedTypeReference для List<User>
- Кеширование результата (повторный запрос будет быстрее)
```bash
clear 
curl -s -X GET http://localhost:8080/api/demo/users \
  -H "Accept: application/json" | jq
```

#### Получение пользователя по ID
**Что демонстрируется:**
- Обработка различных HTTP статусов
- Кеширование по ID
- Graceful handling 404 ошибок
```bash
# Существующий пользователь
clear
curl -s -X GET http://localhost:8080/api/demo/users/1 \
  -H "Accept: application/json" | jq
```
```bash
# Несуществующий пользователь (404)
clear
curl -s -X GET http://localhost:8080/api/demo/users/999 \
  -H "Accept: application/json" -w "%{http_code}\n" | jq
```

#### Создание нового пользователя
**Что демонстрируется:**
- POST запрос с JSON телом
- Валидация входных данных
- Возврат ResponseEntity с заголовками
```bash
clear
curl -X POST http://localhost:8080/api/demo/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Демо Пользователь",
    "username": "demouser",
    "email": "demo@example.com",
    "phone": "+7-900-123-45-67",
    "website": "demo.example.com"
  }' 
```

#### Обновление пользователя
**Что демонстрируется:**
- Разница между PUT и PATCH
- Обновление кеша
- Обработка частичных данных
```bash
# Полное обновление (PUT)
clear
curl -s -X PUT http://localhost:8080/api/demo/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Обновленное Имя",
    "username": "updateduser",
    "email": "updated@example.com"
  }' | jq
```
```bash
# Частичное обновление (PATCH)
clear
curl -s -X PATCH http://localhost:8080/api/demo/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Частично Обновленное Имя"
  }' | jq
```

#### Удаление пользователя
**Что демонстрируется:**
- DELETE операция
- Возврат статуса операции
- Очистка кеша
```bash
clear
curl -s -X DELETE http://localhost:8080/api/demo/users/1 \
  -H "Accept: application/json" | jq
```

### Асинхронные операции
**Что демонстрируется:**
- Асинхронное выполнение через @Async
- Параллельная обработка множественных запросов
- CompletableFuture в REST контроллерах
#### Асинхронное получение пользователя
```bash
clear
curl -s -X GET http://localhost:8080/api/demo/users/1/async \
  -H "Accept: application/json" | jq
```

#### Массовое асинхронное получение
```bash
clear
curl -s -X POST http://localhost:8080/api/demo/users/batch-async \
  -H "Content-Type: application/json" \
  -d '[1, 2, 3, 4, 5]' | jq
```

#### Работа с заголовками
**Что демонстрируется:**
- Передача пользовательских заголовков
- Добавление стандартных заголовков (User-Agent, X-Request-ID)
- Получение информации о заголовках от HTTPBin
```bash
clear
curl -s -X POST http://localhost:8080/api/demo/headers \
  -H "Content-Type: application/json; charset=utf-8" \
  -d '{
    "X-Custom-Header": "My header",
    "X-Client-Version": "1.0.0",
    "X-Request-Source": "Demo"
  }' | jq
```

#### Работа с параметрами запроса
**Что демонстрируется:**
- Построение URL с параметрами через UriBuilder
- Передача множественных параметров
- Обработка query parameters
```bash
clear
curl -s -X GET "http://localhost:8080/api/demo/query-params?param1=value1&param2=value2&search=demo&limit=10" \
  -H "Accept: application/json" | jq
```

#### Демонстрация различных HTTP ошибок
**Что демонстрируется:**
- Обработка различных HTTP статусов
- Логирование ошибок
- Graceful degradation
```bash
# 400 Bad Request
curl -X GET http://localhost:8080/api/demo/error/400 | jq

# 404 Not Found  
curl -X GET http://localhost:8080/api/demo/error/404 | jq

# 500 Internal Server Error
curl -X GET http://localhost:8080/api/demo/error/500 | jq

# 503 Service Unavailable
curl -X GET http://localhost:8080/api/demo/error/503 | jq
```

#### Валидация данных
**Что демонстрируется:**
- Bean Validation с подробными сообщениями
- Глобальный обработчик исключений
- Структурированные ответы об ошибках
```bash
# Невалидные данные (пустое имя, неправильный email)
curl -X POST http://localhost:8080/api/demo/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "",
    "username": "test",
    "email": "invalid-email"
  }' | jq
```

#### Проверка состояния приложения
```bash
# Health check
clear
curl -s http://localhost:8080/actuator/health | jq
```
```bash
# Информация о приложении
clear
curl -s http://localhost:8080/actuator/info | jq
```
```bash
# Метрики
clear
curl -s http://localhost:8080/actuator/metrics | jq
```
```bash
# Конкретная метрика
clear
curl http://localhost:8080/actuator/metrics/http.server.requests | jq
```

#### Состояние кешей
```bash
cear
curl -s http://localhost:8080/actuator/caches | jq
```

#### Prometheus метрики
```bash
clear
curl -s http://localhost:8080/actuator/prometheus | jq
```

### Тестирование кеширования

#### Демонстрация работы кеша
```bash
# Первый запрос (попадание в базу)
clear
time curl -X GET http://localhost:8080/api/demo/users/1

# Второй запрос (из кеша, должен быть быстрее)
time curl -X GET http://localhost:8080/api/demo/users/1

# Проверка состояния кеша
curl http://localhost:8080/actuator/caches | jq
```

#### Инвалидация кеша
Обновим пользователя (очистит кеш)
```bash
clear
curl -s -X PUT http://localhost:8080/api/demo/users/1 \
  -H "Content-Type: application/json" \
  -d '{"name": "Updated Name"}' | jq
```
```bash
# Следующий запрос снова попадет в базу
clear
time curl -X GET http://localhost:8080/api/demo/users/1 
```