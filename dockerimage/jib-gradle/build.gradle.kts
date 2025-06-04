plugins {
    java
    id("org.springframework.boot") version "3.3.2"
    id("io.spring.dependency-management") version "1.1.6"
    id("com.google.cloud.tools.jib") version "3.4.3"
}

group = "ru.otus.hw"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
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
    
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Конфигурация Jib
jib {
    from {
        image = "eclipse-temurin:21-jre-alpine"
        platforms {
            platform {
                architecture = "amd64"
                os = "linux"
            }
            platform {
                architecture = "arm64"
                os = "linux"
            }
        }
    }
    to {
        image = "jib-gradle-app"
        tags = setOf("latest", version.toString())
    }
    container {
        jvmFlags = listOf(
            "-server",
            "-Xms512m",
            "-Xmx1024m",
            "-XX:+UseG1GC",
            "-XX:+UseContainerSupport"
        )
        ports = listOf("8080")
        environment = mapOf(
            "SPRING_PROFILES_ACTIVE" to "prod"
        )
        labels = mapOf(
            "maintainer" to "otus-hw",
            "version" to version.toString(),
            "description" to "Spring Boot application built with Jib"
        )
        creationTime = "USE_CURRENT_TIMESTAMP"
    }
    extraDirectories {
        paths {
            path {
                setFrom("src/main/jib")
                into = "/app"
            }
        }
    }
}