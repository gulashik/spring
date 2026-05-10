/*
 * build.gradle.kts приложения dwh.
 *
 * Это уже не библиотека, а ПРИЛОЖЕНИЕ Spring Boot, поэтому подключаем плагин
 * `org.springframework.boot` — он:
 *   - собирает исполняемый «жирный» jar (BOOT-INF/lib + Class-Path);
 *   - предоставляет задачу `bootRun` для локального запуска;
 *   - автоматически применяет дефолтные манифесты, BuildInfo и т.д.
 *
 * Параллельно нужен `io.spring.dependency-management` — он импортирует BOM
 * Spring Boot и держит версии всех Spring-артефактов согласованными.
 *
 * Зависимости — почему именно такие:
 *   - spring-boot-starter-jdbc: даёт JdbcTemplate, DataSourceAutoConfiguration,
 *     HikariCP. Это «основа» — JPA сюда не тащим намеренно (учебный проект).
 *   - spring-boot-starter-web: HTTP REST-эндпоинты для демо.
 *   - spring-boot-starter-actuator: /actuator/health покажет состояние ВСЕХ
 *     трёх БД, включая dictionary/history (благодаря HealthIndicator-ам стартера).
 *   - flyway-core + flyway-database-postgresql: миграции схемы для всех 3 БД.
 *   - postgresql: JDBC-драйвер.
 *   - additional-sources-postgres: НАШ стартер (берётся из mavenLocal).
 *
 * Тесты:
 *   - spring-boot-starter-test: JUnit 5, AssertJ, Mockito и пр.
 *   - testcontainers + postgresql: реальная БД в тестах (Docker/Podman нужен).
 */

plugins {
    java
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "org.gulash.demo"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    // === ОСНОВНОЙ DataSource приложения (dwh) ===
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // === Flyway: миграции для всех БД ===
    // С версии 10 Flyway вынес поддержку Postgres в отдельный модуль.
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")

    // === JDBC-драйвер ===
    runtimeOnly("org.postgresql:postgresql")

    // === НАШ СТАРТЕР ===
    // Подтягивается из https://jitpack.io/
    // в поиске пишем "gulashik/spring-starter-additional-sources-postgres" и выбираем нужную версию
    //implementation("com.github.gulashik:spring-starter-additional-sources-postgres:main-SNAPSHOT")

    // Подтягивается из mavenLocal() (см. settings.gradle.kts).
    implementation("org.gulash.demo:additional-sources-postgres:1.0.0")

    // === Тесты ===
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("org.testcontainers:junit-jupiter:1.20.2")
    testImplementation("org.testcontainers:postgresql:1.20.2")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    testLogging {
        events("passed", "failed", "skipped")
        showStandardStreams = false
    }
}
