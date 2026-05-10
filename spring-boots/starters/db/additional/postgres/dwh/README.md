# dwh — демо-приложение

Spring Boot приложение, использующее:
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