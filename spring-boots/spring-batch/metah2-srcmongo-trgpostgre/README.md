
Пример на основе Spring Batch процедура миграции данных из Mongo в Postgres. 
Хранилка метаданных по Spring Batch в H2

---
##### H2
Накатываем схему H2 БД по SpringBatch</br>
[H2BatchSchemaInitializer.java](src/main/java/ru/otus/hw/config/h2/H2BatchSchemaInitializer.java)

H2 DataSource</br> 
[H2DataSourceConfig.java](src/main/java/ru/otus/hw/config/h2/H2DataSourceConfig.java)</br>
application.yml - spring.datasource

---
##### Postgre
Образ</br>
[compose.md](src/main/java/ru/otus/hw/compose.md)

Postgre DataSource</br>
[PostgreDataSourceConfig.java](src/main/java/ru/otus/hw/config/postgre/PostgreDataSourceConfig.java)
application.yml - spring.target-datasource

Liquibase Postgre DataSource </br>
[LiquibaseDataSourceConfig.java](src/main/java/ru/otus/hw/config/postgre/LiquibaseDataSourceConfig.java)

entity, dto</br>
[targetdb](src/main/java/ru/otus/hw/model/targetdb)

repository</br>
[jpa](src/main/java/ru/otus/hw/repositories/jpa)

---
##### Mongo
накатываем данные</br>
[DatabaseChangelog.java](src/main/java/ru/otus/hw/mongock/changelog/DatabaseChangelog.java)

application.yml
 - spring.data.mongodb - host, port, etc
 - de.flapdoodle.mongodb.embedded - Embedded Mongo server
 - mongock

entity, dto</br>
[sourcedb](src/main/java/ru/otus/hw/model/sourcedb)

repository</br>
[mongo](src/main/java/ru/otus/hw/repositories/mongo)

---
##### Liquibase
application.yml - spring.liquibase</br>
[changelog](src/main/resources/db/changelog)


---
##### SpringBatch
Использование JobLauncher, получаем JobExecution</br>
[Command.java](src/main/java/ru/otus/hw/command/Command.java)

