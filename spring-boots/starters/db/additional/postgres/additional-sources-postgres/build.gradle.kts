/*
 * build.gradle.kts — сборка стартера additional-sources-postgres.
 *
 * Что такое "Spring Boot Starter" и почему здесь НЕТ плагина org.springframework.boot:
 *   Стартер — это БИБЛИОТЕКА, а не приложение. Плагин `org.springframework.boot`
 *   собирает исполняемый fat-jar и поэтому здесь НЕ нужен (и даже вреден). Используем
 *   обычный `java-library` — это создаёт корректный публикуемый jar с Class-Path,
 *   совместимый с любым потребителем (Spring Boot 3.x приложение, обычное Spring-приложение).
 *
 * Что делает плагин `io.spring.dependency-management`:
 *   Импортирует BOM Spring Boot, чтобы версии всех Spring-артефактов брались из
 *   единого источника. Это гарантирует совместимость и избавляет от ручного
 *   указания версий для spring-context, spring-jdbc, HikariCP и т.п.
 *
 * Зачем `maven-publish`:
 *   Чтобы пользователь мог выполнить `./gradlew publishToMavenLocal` и сразу
 *   подтянуть стартер в проект `dwh` через mavenLocal().
 */

plugins {
    `java-library`                                                  // публикуем библиотеку, а не приложение
    `maven-publish`                                                 // публикация в локальный/удалённый Maven repo
    id("io.spring.dependency-management") version "1.1.6"           // BOM Spring Boot для согласованных версий
}

group = "org.gulash.demo"
version = "0.0.1-SNAPSHOT"
description = "Spring Boot Starter, добавляющий несколько дополнительных PostgreSQL-источников по конфигурации"

java {
    // Toolchain — рекомендуемый способ зафиксировать целевую JDK независимо от системной.
    // Gradle сам найдёт/скачает JDK. На машине разработчика она уже есть в ~/.gradle/jdks.
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()                                                // публикуем исходники — полезно для пользователей starter'а
    withJavadocJar()                                                // и javadoc-jar
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.3.4")
    }
}

dependencies {
    // === ОБЯЗАТЕЛЬНЫЕ для стартера ===
    // spring-boot-autoconfigure: содержит @AutoConfiguration, @ConditionalOn*, базы для FailureAnalyzer и т.п.
    api("org.springframework.boot:spring-boot-autoconfigure")
    // spring-jdbc: нужен JdbcTemplate, который мы динамически регистрируем для каждого доп.источника.
    api("org.springframework:spring-jdbc")
    // HikariCP: де-факто стандарт пула соединений в Spring Boot. Версия приходит из BOM.
    api("com.zaxxer:HikariCP")
    // PostgreSQL JDBC-драйвер. api — потому что наши DataSource-бины напрямую от него зависят.
    api("org.postgresql:postgresql")

    // === ОПЦИОНАЛЬНЫЕ ===
    // Actuator — стартер умеет регистрировать HealthIndicator для каждой доп.БД, но
    // только если в classpath приложения есть actuator. Иначе авто-конфиг "выключится"
    // через @ConditionalOnClass. compileOnly — чтобы потребитель сам решал, нужен ли actuator.
    compileOnly("org.springframework.boot:spring-boot-actuator")

    // === ИНСТРУМЕНТЫ РАЗРАБОТКИ ===
    // configuration-processor: генерирует spring-configuration-metadata.json из @ConfigurationProperties.
    // Это даёт автодополнение свойств app.datasources.* в IDE у пользователей стартера.
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // === ТЕСТЫ ===
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        // exclude vintage-engine — оставим только JUnit 5 Jupiter.
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("org.springframework.boot:spring-boot-starter-actuator") // для теста health indicator
    testImplementation("org.assertj:assertj-core")
    // Testcontainers для проверок с реальной Postgres
    testImplementation("org.testcontainers:junit-jupiter:1.20.2")
    testImplementation("org.testcontainers:postgresql:1.20.2")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")                         // нужно для @ConfigurationProperties (rec. конструкторное связывание)
}

tasks.withType<Javadoc>().configureEach {
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
    options.encoding = "UTF-8"
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    // Подсказываем Testcontainers, где сокет (Podman / Docker Desktop). Локально пользователь
    // выставит переменные окружения сам; Gradle их прокинет в JVM теста.
    systemProperty("file.encoding", "UTF-8")
    testLogging {
        events("passed", "failed", "skipped")
        showStandardStreams = false
    }
}

// Отключаем публикацию Gradle Module Metadata (.module-файла).
// Причина: io.spring.dependency-management поставляет версии зависимостей через BOM, поэтому
// в Gradle Metadata они оказываются «без версии», и Gradle 8+ помечает это как ошибку
// валидации публикации. Для совместимости с Maven-консьюмерами нам достаточно pom.xml.
tasks.withType<GenerateModuleMetadata>().configureEach {
    enabled = false
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name.set("additional-sources-postgres")
                description.set(project.description)
            }
        }
    }
}
