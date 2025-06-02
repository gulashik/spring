# Multi-stage build для оптимизации размера образа
FROM maven:3.9.4-eclipse-temurin-17 AS build

# Установка рабочей директории
WORKDIR /app

# Копирование только pom.xml для кеширования зависимостей
COPY pom.xml .
# Предварительная загрузка зависимостей (улучшает кеширование слоев)
RUN mvn dependency:go-offline

# Копирование исходного кода
COPY src ./src

# Сборка приложения (пропуск тестов для ускорения сборки)
RUN mvn clean package -DskipTests

# Production stage - использование легковесного образа для запуска
FROM eclipse-temurin:17-jre

# Метаданные образа
LABEL maintainer="developer@example.com"
LABEL description="Spring Boot application with PostgreSQL"
LABEL version="1.0"

# Создание непривилегированного пользователя для безопасности
RUN groupadd -g 1001 appgroup && \
    useradd -u 1001 -g appgroup -s /bin/sh -m appuser

# Установка рабочей директории
WORKDIR /app

# Копирование JAR файла из build stage
COPY --from=build /app/target/*.jar app.jar

# Изменение владельца файлов
RUN chown -R appuser:appgroup /app

# Переключение на непривилегированного пользователя
USER appuser

# Настройка JVM для контейнерной среды (будет переопределено из docker-compose)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+UseStringDeduplication"

# Декларация порта приложения
EXPOSE 8080

# Health check для проверки работоспособности приложения
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Точка входа с исполнением JAR-файла
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]