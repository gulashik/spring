
## Использование
Использует Testcontainers (Docker/Podman должен быть доступен). Поднимаются три отдельных контейнера PostgreSQL.

### Поднимаем контейнеры
```bash
podman compose -f ../compose.yml down
podman compose -f ../compose.yml up -d 
podman ps
```

### Пересобираем и публикуем стартер
```bash
cd ../additional-sources-postgres && ./gradlew publishToMavenLocal
```

### Пересобираем проект
```
./gradlew clean build 
```

### Тесты
```bash
./gradlew test
```

### Запускаем проект
```bash
./gradlew clean build bootRun 
```

### Проверка
```bash
curl http://localhost:8080/api/dictionary/currencies | jq
```
```bash
curl -X POST http://localhost:8080/api/orders \
     -H 'Content-Type: application/json' \
     -d '{"customer":"Alice","currency":"USD","amount":99.95}'  | jq
```
```bash
curl 'http://localhost:8080/api/history/recent?limit=10' | jq
```
```bash
curl http://localhost:8080/actuator/health | jq
```

### Остановка
```bash
pkill -f 'org.gulash.demo.dwh.DwhApplication'
```