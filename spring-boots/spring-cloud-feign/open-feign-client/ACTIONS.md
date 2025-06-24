
### Требования
- Интернет соединение (для доступа к внешним API)

# Запуск
``` 
mvn spring-boot:run
```
или
``` 
mvn clean install
```
```bash
java -jar target/spring-openfeign-demo-1.0.0-SNAPSHOT.jar
```

# Состояние приложения
```bash
curl -s http://localhost:8080/api/demo/health | jq
```

# Примеры использования:

```bash
# Получить все посты
curl http://localhost:8080/api/demo/posts | jq
```
```bash
# Получить пост по ID
curl -s http://localhost:8080/api/demo/posts/1  | jq
```
```bash
# Создать новый пост
curl -s -X POST http://localhost:8080/api/demo/posts \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"title":"Test Post","body":"Test content"}' \
  | jq
```

```bash
# Запрос с задержкой HttpBin
clear
curl -s http://localhost:8080/api/demo/httpbin/demo | jq
```
```bash
# Тестирование обработки ошибок
clear
curl -s http://localhost:8080/api/demo/httpbin/status/404 | jq
```
```bash
# Тестирование таймаутов
curl http://localhost:8080/api/demo/httpbin/delay/2
```


```bash
# удалить логи
bash -c "rm -f ./logs/*"
```

