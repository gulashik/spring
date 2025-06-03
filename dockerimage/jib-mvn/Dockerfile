# Multi-stage build для оптимизации размера образа
FROM maven:3.9.4-eclipse-temurin-17 AS build

# Установка рабочей директории
WORKDIR /app

# Копирование файлов для сборки
COPY pom.xml .
COPY src ./src

# Сборка приложения (пропуск тестов для ускорения сборки)
RUN mvn clean package -DskipTests

# Production stage(чистая сборка без мусора)
FROM arm64v8/eclipse-temurin:17-jre

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

# Настройка JVM для контейнерной среды
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+UseStringDeduplication"

# Декларация на Порт приложения
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Точка входа
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]