/*
 * Build script для Spring Boot приложения с поддержкой контейнеризации через Jib
 */

// ========================================
// ПЛАГИНЫ
// ========================================
plugins {
    java
    id("org.springframework.boot") version "3.3.2"
    id("io.spring.dependency-management") version "1.1.6"
    id("com.google.cloud.tools.jib") version "3.4.3" // Контейнеризация без Docker daemon
}

group = "ru.otus.hw"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21) // Использование Java 21 LTS
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())  // Lombok annotations доступны во время компиляции
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    
    runtimeOnly("org.postgresql:postgresql")
    
    // ИНСТРУМЕНТЫ РАЗРАБОТКИ
    compileOnly("org.projectlombok:lombok")  // Lombok для генерации boilerplate кода (@Data, @Builder и т.д.)
    annotationProcessor("org.projectlombok:lombok")  // Обработчик аннотаций Lombok
    
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// ========================================
// КОНФИГУРАЦИЯ JIB ДЛЯ КОНТЕЙНЕРИЗАЦИИ
// ========================================
jib {
    // БАЗОВЫЙ ОБРАЗ
    from {
        image = "eclipse-temurin:21-jre-alpine" // Легковесный Alpine Linux с OpenJDK 21 JRE
        platforms {
            // Поддержка ARM64 архитектуры (например, для Apple M1/M2, AWS Graviton)
            platform {
                architecture = "arm64"
                os = "linux"
            }
            // Раскомментировать для AMD64/x86_64 архитектуры
            /*platform {
                architecture = "amd64"
                os = "linux"
            }*/
        }
    }
    
    // ЦЕЛЕВОЙ ОБРАЗ
    to {
        image = "jib-gradle-app"  // Имя создаваемого образа
        tags = setOf("latest", version.toString()) // Теги образа: latest и версия проекта
    }
    
    // КОНФИГУРАЦИЯ КОНТЕЙНЕРА
    container {
        // JVM ПАРАМЕТРЫ для оптимальной работы в контейнере
        jvmFlags = listOf(
            "-server",                                             // Серверный режим JVM
            "-Xms512m",                                           // Начальный размер heap
            "-Xmx1024m",                                          // Максимальный размер heap
            "-XX:+UseG1GC",                                       // G1 сборщик мусора (подходит для контейнеров)
            "-XX:+UseContainerSupport"                            // Поддержка container-aware JVM
        )
        ports = listOf("8080")                                    // Экспортируемый порт Spring Boot приложения
        
        // ПЕРЕМЕННЫЕ ОКРУЖЕНИЯ
        environment = mapOf(
            "SPRING_PROFILES_ACTIVE" to "prod"                   // Активация production профиля Spring
        )
        
        // МЕТАДАННЫЕ ОБРАЗА
        labels = mapOf(
            "maintainer" to "otus-hw",                           // Maintainer образа
            "version" to version.toString(),                     // Версия приложения
            "description" to "Spring Boot application built with Jib" // Описание образа
        )
        
        creationTime = "USE_CURRENT_TIMESTAMP"                   // Время создания = текущее время
        user = "1000:1000"                                       // Запуск под непривилегированным пользователем (безопасность)
    }
    
    // НАСТРОЙКА ДЛЯ PODMAN вместо Docker
    dockerClient {
        executable = "podman"                                     // Использование Podman как альтернативы Docker
    }
    
    // ДОПОЛНИТЕЛЬНЫЕ ДИРЕКТОРИИ для копирования в образ
    extraDirectories {
        paths {
            path {
                setFrom("src/main/jib")                          // Копирование файлов из src/main/jib
                into = "/app"                                    // в директорию /app контейнера
            }
        }
    }
}