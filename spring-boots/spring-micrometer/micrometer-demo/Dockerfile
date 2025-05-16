FROM eclipse-temurin:21-jdk-alpine as builder
WORKDIR /app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

# Устанавливаем права на запуск mvnw и собираем приложение
RUN chmod +x ./mvnw
RUN ./mvnw package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Копируем собранный JAR
COPY --from=builder /app/target/*.jar app.jar

# Устанавливаем переменные окружения для JVM
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Xms256m -Xmx512m"

# Устанавливаем переменные окружения для приложения
ENV SPRING_PROFILES_ACTIVE=prod

# Устанавливаем порты
EXPOSE 8080

# Запускаем приложение
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]