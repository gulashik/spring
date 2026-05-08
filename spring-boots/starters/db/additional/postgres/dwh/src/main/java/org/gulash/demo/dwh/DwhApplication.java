package org.gulash.demo.dwh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Точка входа Spring Boot приложения «DWH».
 *
 * <h2>Что демонстрирует приложение</h2>
 * <ol>
 *   <li>Использование «основной» БД через стандартный Spring Boot
 *       (свойства {@code spring.datasource.*}).</li>
 *   <li>Подключение «вспомогательных» БД (dictionary, history) через стартер
 *       {@code additional-sources-postgres} (свойства {@code app.datasources.*}).</li>
 *   <li>Миграции Flyway отдельно для каждой БД.</li>
 *   <li>Actuator-health на трёх источниках сразу.</li>
 * </ol>
 *
 * <h2>Запуск</h2>
 * <pre>{@code
 *   # 1. Поднять три Postgres
 *   docker compose up -d
 *
 *   # 2. Опубликовать стартер в локальный maven (один раз)
 *   cd additional-sources-postgres && ./gradlew publishToMavenLocal
 *
 *   # 3. Запустить приложение
 *   cd ../dwh && ./gradlew bootRun
 *
 *   # 4. Проверить
 *   curl http://localhost:8080/api/orders
 *   curl http://localhost:8080/api/dictionary/currencies
 *   curl http://localhost:8080/api/history/recent
 *   curl http://localhost:8080/actuator/health
 * }</pre>
 */
@SpringBootApplication
public class DwhApplication {

    public static void main(String[] args) {
        SpringApplication.run(DwhApplication.class, args);
    }
}
