
### Требования
- Интернет соединение (для доступа к внешним API)

# Запуск
```maven 
mvn spring-boot:run
```
или
```maven 
mvn clean install
```
```bash
java -jar target/spring-openfeign-demo-1.0.0-SNAPSHOT.jar
```

# Состояние приложения
```bash
curl -s http://localhost:8080/api/demo/health | jq
```