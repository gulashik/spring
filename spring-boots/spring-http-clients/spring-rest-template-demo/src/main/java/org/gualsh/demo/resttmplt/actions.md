# RestTemplate Demo Project

```bash
# Сборка и запуск одной командой
docker-compose up --build

# Запуск в фоне
docker-compose up -d

# Просмотр логов
docker-compose logs -f resttmplt-demo

# Остановка
docker-compose down


# Сборка проекта
./mvnw clean compile

# Запуск тестов
# Все тесты
./mvnw test

# Только интеграционные тесты
./mvnw test -Dtest="*Test"

# С покрытием кода
./mvnw test jacoco:report

# Запуск приложения
./mvnw spring-boot:run

# Проверка работоспособности
curl http://localhost:8080/actuator/health
```



## 📡 API Endpoints

### **Пользователи:**

```http
GET /api/users
# Получение всех пользователей (с кэшированием)

GET /api/users/{id}
# Получение пользователя по ID

GET /api/users/{id}/details  
# Получение пользователя с заголовками ответа

GET /api/users/{id}/posts
# Получение постов пользователя
```

### **Посты:**

```http
POST /api/posts
Content-Type: application/json

{
    "userId": 1,
    "title": "Новый пост",
    "body": "Содержимое поста"
}

PUT /api/posts/{id}
Content-Type: application/json

{
    "userId": 1,
    "title": "Обновленный пост", 
    "body": "Новое содержимое"
}

DELETE /api/posts/{id}
# Удаление поста
```

### **Демонстрационные:**

```http
GET /api/demo/headers
# Демонстрация работы с заголовками

GET /api/cache/info
# Информация о кэше

DELETE /api/cache/clear
# Очистка кэша
```

### **Мониторинг:**

```http
GET /actuator/health
# Проверка здоровья приложения

GET /actuator/metrics
# Метрики приложения

GET /actuator/prometheus
# Метрики в формате Prometheus

GET /actuator/health
# Статус здоровья: UP/DOWN с деталями

GET /actuator/metrics/http.client.requests
# Метрики HTTP клиента

GET /actuator/caches
# Информация о кэшах

GET /actuator/prometheus
# Все метрики в формате Prometheus
```
