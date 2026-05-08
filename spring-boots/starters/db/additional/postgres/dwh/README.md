# dwh — демо-приложение

Spring Boot 3.3.4 приложение, использующее:
* стандартный Spring Boot DataSource для основной БД `dwh`;
* стартер `additional-sources-postgres` для двух дополнительных БД `dictionary` и `history`;
* Flyway (отдельные locations на каждую БД);
* Actuator для health-проверок всех трёх источников.

## Структура

```
src/main/java/org/gulash/demo/dwh/
  DwhApplication.java          # точка входа
  AdditionalFlywayConfig.java  # Flyway-бины для каждой БД
  OrderDao.java                # JdbcTemplate (основная БД)
  DictionaryDao.java           # JdbcTemplate + @Qualifier (dictionary)
  HistoryDao.java              # NamedParameterJdbcTemplate (history)
  OrdersController.java        # REST + кросс-БД сценарий

src/main/resources/
  application.yml
  db/migration/dwh/V1__init.sql
  db/migration/dictionary/V1__init.sql
  db/migration/history/V1__init.sql
```

## Перед запуском

1. Поднимите `compose.yml` из корня репозитория (`docker compose up -d`).
2. Соберите и опубликуйте стартер:
   ```
   cd ../additional-sources-postgres && ./gradlew publishToMavenLocal
   ```

## Запуск

```bash
./gradlew bootRun
```

Проверка:

```bash
curl http://localhost:8080/api/dictionary/currencies
curl -X POST http://localhost:8080/api/orders \
     -H 'Content-Type: application/json' \
     -d '{"customer":"Alice","currency":"USD","amount":99.95}'
curl 'http://localhost:8080/api/history/recent?limit=10'
curl http://localhost:8080/actuator/health
```

## Тесты

```bash
./gradlew test
```

Использует Testcontainers (Docker/Podman должен быть доступен).
Поднимаются три отдельных контейнера PostgreSQL — это **намеренно**, чтобы
в тестовом окружении была та же топология, что и в проде.
