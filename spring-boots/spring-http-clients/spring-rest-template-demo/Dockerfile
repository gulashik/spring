# Используем официальный OpenJDK 17 образ как базовый
FROM openjdk:17-jdk-slim

# Информация о мейнтейнере
LABEL maintainer="gualsh@example.com"
LABEL description="RestTemplate Demo Application"
LABEL version="1.0.0"

# Устанавливаем рабочую директорию
WORKDIR /app

# Создаем пользователя для безопасности (не root)
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

# Копируем Maven wrapper и зависимости для кэширования слоев
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Скачиваем зависимости (кэшируется если pom.xml не изменился)
RUN ./mvnw dependency:go-offline -B

# Копируем исходный код
COPY src ./src

# Собираем приложение
RUN ./mvnw clean package -DskipTests -B

# Переименовываем JAR файл для удобства
RUN mv target/resttmplt-demo-*.jar target/app.jar

# Изменяем владельца файлов на созданного пользователя
RUN chown -R appuser:appgroup /app

# Переключаемся на не-root пользователя
USER appuser

# Открываем порт приложения
EXPOSE 8080

# Настройки JVM для контейнерной среды
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseContainerSupport"

# Проверка здоровья приложения
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Команда запуска приложения
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar target/app.jar"]