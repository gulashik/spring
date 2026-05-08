# additional-sources-postgres

Spring Boot Starter, добавляющий приложению произвольное количество дополнительных
PostgreSQL-источников через простую YAML-конфигурацию.

```yaml
app:
  datasources:
    dictionary:
      jdbc-url: jdbc:postgresql://localhost:5434/dictionary
      username: dictionary
      password: dictionary
    history:
      jdbc-url: jdbc:postgresql://localhost:5435/history
      username: history
      password: history
```

## Что регистрируется

Для каждой записи карты `app.datasources.<name>` стартер создаёт три бина:

| Имя бина                      | Тип                                |
| ----------------------------- | ---------------------------------- |
| `<name>DataSource`            | `HikariDataSource`                 |
| `<name>JdbcTemplate`          | `JdbcTemplate`                     |
| `<name>NamedJdbcTemplate`     | `NamedParameterJdbcTemplate`       |

При наличии Spring Boot Actuator также регистрируется
`<name>DataSourceHealthIndicator` и появляется в `/actuator/health` под
`components.db.components.<name>DataSource`.

## Использование

```java
@Repository
class DictionaryDao {
    private final JdbcTemplate jdbc;
    DictionaryDao(@Qualifier("dictionaryJdbcTemplate") JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }
}
```

## Сборка и публикация

```bash
./gradlew build               # тесты + сборка
./gradlew publishToMavenLocal # положить в ~/.m2/repository
```

## Подключение в потребителе (Gradle)

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
    }
}

// build.gradle.kts
dependencies {
    implementation("org.gulash.demo:additional-sources-postgres:0.0.1-SNAPSHOT")
}
```

## Опциональные поля конфигурации

| Свойство               | Значение по умолчанию | Назначение                                |
| ---------------------- | --------------------- | ----------------------------------------- |
| `maximum-pool-size`    | 5                     | максимум соединений Hikari                |
| `minimum-idle`         | = `maximum-pool-size` | рекомендация HikariCP — фиксированный пул |
| `connection-timeout`   | 5s                    | таймаут ожидания соединения из пула       |
| `read-only`            | false                 | помечает соединения как read-only         |
| `schema`               | (без изменений)       | дефолтная схема (search_path)             |
| `health-query`         | `SELECT 1`            | SQL для health-indicator                  |

Подробнее см. javadoc классов и корневой README репозитория.
