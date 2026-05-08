# Демонстрация: Spring Boot Starter для дополнительных Postgres-источников

Образовательный проект на Spring Boot 3.3.4 + Java 21 + Gradle (Kotlin DSL),
показывающий, **как правильно создать и использовать Spring Boot Starter**,
который добавляет в приложение произвольное число дополнительных PostgreSQL-источников.

Структура — «от простого к сложному»:

```
.
├── compose.yml                    # три независимых Postgres (dwh, dictionary, history)
├── additional-sources-postgres/   # сам стартер (BIBLIOTEKA, не приложение)
└── dwh/                           # Spring Boot ПРИЛОЖЕНИЕ, использующее стартер
```

---

## 1. Что демонстрируется

| Возможность Spring Boot Starter API                                              | Где смотреть                                                            |
| -------------------------------------------------------------------------------- | ----------------------------------------------------------------------- |
| `@AutoConfiguration` + `META-INF/spring/...AutoConfiguration.imports`            | `additional-sources-postgres/.../AdditionalPostgresAutoConfiguration.java` |
| `@ConfigurationProperties` (record, конструкторное связывание, Map<String, X>)   | `AdditionalSourcesProperties.java`, `DataSourceProperties.java`         |
| Динамическая регистрация бинов через `BeanDefinitionRegistryPostProcessor`       | `AdditionalDataSourceRegistrar.java`                                    |
| Условия активации: `@ConditionalOnClass`, `@AutoConfigureBefore`, FilteredClassLoader | `AdditionalPostgresAutoConfiguration.java`, тесты                  |
| Опциональная Actuator-интеграция (HealthIndicator)                               | `AdditionalDataSourceHealthIndicator.java`                              |
| Метаданные конфигурации (`additional-spring-configuration-metadata.json`)        | `META-INF/additional-spring-configuration-metadata.json`                |
| `publishToMavenLocal` для распространения стартера                               | `additional-sources-postgres/build.gradle.kts`                          |
| Корректное использование стартера: `@Qualifier`, миграции Flyway, REST           | `dwh/.../*Dao.java`, `AdditionalFlywayConfig.java`                      |
| Тесты: `ApplicationContextRunner`, `FilteredClassLoader`, Testcontainers, e2e    | `additional-sources-postgres/src/test`, `dwh/src/test`                  |

---

## 2. Технические выборы и обоснование

| Выбор                                | Почему                                                                                |
| ------------------------------------ | ------------------------------------------------------------------------------------- |
| **Java 21**                          | LTS; `record`, `pattern matching`, `switch expressions` упрощают код стартера         |
| **Spring Boot 3.3.4**                | стабильный релиз; новый механизм авто-конфигурации (`AutoConfiguration.imports`)      |
| **HikariCP**                         | де-факто стандарт пула; самая быстрая и предсказуемая реализация                      |
| **Gradle Kotlin DSL**                | строгая типизация, IDE-подсказки, отсутствие XML                                      |
| **`record` для properties**          | иммутабельность, конструкторное связывание (`-parameters`), отсутствие сеттеров       |
| **`Map<String, DataSourceProperties>`** | количество дополнительных БД заранее неизвестно — карта самый прямой способ         |
| **`BeanDefinitionRegistryPostProcessor`** | единственный надёжный способ программной регистрации произвольного числа бинов  |
| **mavenLocal для дистрибуции стартера** | реалистично имитирует «настоящий» жизненный цикл: build → publish → consume       |
| **Flyway 10 + flyway-database-postgresql** | актуальный модульный Flyway; неактуальные `flyway-core` без диалекта здесь не подойдут |
| **Testcontainers**                   | реальная Postgres вместо H2 ⇒ ловятся настоящие ошибки SQL/драйвера                   |

---

## 3. Требования

* JDK **21** (Gradle toolchain автоматически найдёт; либо поставьте `JAVA_HOME`)
* Docker / Podman (`compose.yml`, Testcontainers)
* Свободные порты: `5433`, `5434`, `5435`, `8080`

### Подсказка для Podman (macOS)

Чтобы Testcontainers увидели Podman:

```bash
export DOCKER_HOST=unix://$(podman machine inspect podman-machine-default --format '{{.ConnectionInfo.PodmanSocket.Path}}')
export TESTCONTAINERS_RYUK_DISABLED=true
```

---

## 4. Запуск

### 4.1. Поднять три Postgres

```bash
docker compose up -d   # или: podman compose up -d
docker compose ps
```

### 4.2. Опубликовать стартер в локальный Maven-репозиторий

```bash
cd additional-sources-postgres
./gradlew publishToMavenLocal
```

### 4.3. Запустить приложение dwh

```bash
cd ../dwh
./gradlew bootRun
```

### 4.4. Проверить эндпоинты

```bash
curl http://localhost:8080/api/dictionary/currencies
curl -X POST http://localhost:8080/api/orders \
     -H 'Content-Type: application/json' \
     -d '{"customer":"Alice","currency":"USD","amount":99.95}'
curl http://localhost:8080/api/orders
curl http://localhost:8080/api/history/recent
curl http://localhost:8080/actuator/health | jq
```

В `/actuator/health` под `components.db.components` будут три источника:
`dataSource`, `dictionaryDataSource`, `historyDataSource`.

---

## 5. Запуск тестов

```bash
# тесты стартера (быстрые ApplicationContextRunner + 1 Testcontainers)
cd additional-sources-postgres && ./gradlew test

# полный e2e тест dwh — 3 Postgres контейнера
cd ../dwh && ./gradlew test
```

---

## 6. Подводные камни (выжимка)

| Грабли                                                         | Решение, использованное в коде                                          |
| -------------------------------------------------------------- | ----------------------------------------------------------------------- |
| `spring.datasource.*` — это уже занятый Spring Boot префикс    | Использовать СВОЙ префикс: `app.datasources.*`                          |
| `record` не свяжется без `-parameters`                         | `options.compilerArgs.add("-parameters")` в Gradle                      |
| Несколько `DataSource` ⇒ `NoUniqueBeanDefinitionException`     | `@Qualifier("dataSource")` на основной + `primary = false` в стартере   |
| Flyway автоконфиг **отключается**, как только появляется свой бин `Flyway` | Создаём «основной» `Flyway` сами (`dwhFlyway`)                |
| HealthIndicator стандартный покрывает только основной DataSource | Свой `AdditionalDataSourceHealthIndicator` + регистрация в `ContextRefreshedEvent` |
| `ConditionalOnProperty` не умеет различать `Map`-маркер        | «Софт-выключение» в Registrar при пустой карте                          |
| Hikari «утечка» при остановке контекста                        | `setDestroyMethodName("close")` в BeanDefinition                        |


---

## 7. Лицензия

Учебный проект, без явной лицензии. Используйте свободно для самообучения.
