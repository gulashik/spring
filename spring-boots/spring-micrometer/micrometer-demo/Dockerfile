FROM eclipse-temurin:21-jdk as builder
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# Экспортируем порт для Spring Boot приложения
EXPOSE 8080

# Экспортируем порт для Prometheus (если он будет использоваться)
EXPOSE 9090

ENTRYPOINT ["java", "-jar", "app.jar"]