# Используем многоэтапную сборку для оптимизации размера образа

# Этап 1: Сборка приложения
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Копируем только файл pom.xml для загрузки зависимостей
COPY pom.xml .
# Загружаем зависимости
RUN mvn dependency:go-offline -B

# Копируем исходный код
COPY src ./src
# Собираем приложение и пропускаем тесты
RUN mvn package -DskipTests

# Этап 2: Создание финального образа
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Копируем только jar-файл из предыдущего этапа
COPY --from=build /app/target/*.jar app.jar

# Порт, на котором будет работать приложение
EXPOSE 8080

# Точка входа для запуска приложения
ENTRYPOINT ["java", "-jar", "app.jar"]

# Возможность передавать дополнительные параметры при запуске контейнера
CMD ["--spring.profiles.active=dev"]