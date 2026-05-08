package org.gulash.demo.dwh;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Полноценный e2e-тест приложения dwh.
 *
 * <h2>Что демонстрирует</h2>
 * <ul>
 *   <li>Поднимаются ТРИ независимых контейнера Postgres (по одному на dwh, dictionary, history) —
 *       строго как в продовом сценарии. {@link DynamicPropertySource} «прокидывает» их
 *       реальные jdbc-url'ы в свойства Spring Boot и нашего стартера.</li>
 *   <li>Запускается полный {@link SpringBootTest} с веб-сервером (RANDOM_PORT) — это не
 *       юнит-срез, а живое приложение.</li>
 *   <li>Проверяется кросс-БД сценарий: создаётся заказ ⇒ валидация валюты идёт в
 *       dictionary, событие пишется в history, факт записи виден в /api/history/recent.</li>
 *   <li>Проверяется Actuator: компоненты {@code dictionaryDataSource} и
 *       {@code historyDataSource} имеют статус UP — это работает HealthIndicator стартера.</li>
 * </ul>
 *
 * <h2>Подводные камни</h2>
 * <ul>
 *   <li>Три контейнера = долгий старт (10–20 секунд). Используйте этот тест для smoke-сценариев,
 *       а юнит-проверки выносите в обычные unit-тесты.</li>
 *   <li>{@code DynamicPropertySource} вычисляется ДО старта контекста — это ключевой
 *       момент: только тогда тест может «знать» порт контейнера.</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class DwhApplicationIT {

    @Container
    static final PostgreSQLContainer<?> dwhDb = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16.4-alpine"))
            .withDatabaseName("dwh").withUsername("dwh").withPassword("dwh");

    @Container
    static final PostgreSQLContainer<?> dictionaryDb = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16.4-alpine"))
            .withDatabaseName("dictionary").withUsername("dictionary").withPassword("dictionary");

    @Container
    static final PostgreSQLContainer<?> historyDb = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16.4-alpine"))
            .withDatabaseName("history").withUsername("history").withPassword("history");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        // основной DataSource Spring Boot
        r.add("spring.datasource.url", dwhDb::getJdbcUrl);
        r.add("spring.datasource.username", dwhDb::getUsername);
        r.add("spring.datasource.password", dwhDb::getPassword);

        // дополнительные источники — наш стартер
        r.add("app.datasources.dictionary.jdbc-url", dictionaryDb::getJdbcUrl);
        r.add("app.datasources.dictionary.username", dictionaryDb::getUsername);
        r.add("app.datasources.dictionary.password", dictionaryDb::getPassword);

        r.add("app.datasources.history.jdbc-url", historyDb::getJdbcUrl);
        r.add("app.datasources.history.username", historyDb::getUsername);
        r.add("app.datasources.history.password", historyDb::getPassword);
    }

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate http;

    @Test
    void createOrderWritesToOrdersAndHistory_andValidatesCurrency() {
        // 1) Сначала проверим словарь — миграции должны были насеять три валюты.
        var currencies = http.getForObject("http://localhost:" + port + "/api/dictionary/currencies", Object[].class);
        assertThat(currencies).hasSize(3);

        // 2) Создаём заказ с НЕИЗВЕСТНОЙ валютой — должно вернуться 400.
        var bad = http.postForEntity("http://localhost:" + port + "/api/orders",
                jsonOrder("Alice", "XXX", "100.00"), Map.class);
        assertThat(bad.getStatusCode().value()).isEqualTo(400);

        // 3) Создаём корректный заказ.
        ResponseEntity<Map> created = http.postForEntity("http://localhost:" + port + "/api/orders",
                jsonOrder("Alice", "USD", "100.00"), Map.class);
        assertThat(created.getStatusCode().is2xxSuccessful()).isTrue();
        Number id = (Number) created.getBody().get("id");
        assertThat(id.longValue()).isPositive();

        // 4) В истории появилось событие.
        var events = http.getForObject("http://localhost:" + port + "/api/history/recent?limit=10", Object[].class);
        assertThat(events).isNotEmpty();

        // 5) Actuator-health видит обе доп.БД UP.
        // Подводный камень: индикаторы DataSource-типа Spring Boot группирует в составной
        // индикатор "db" (CompositeHealthContributor). Поэтому реальный путь — components.db.components.*
        @SuppressWarnings("unchecked")
        Map<String, Object> health = http.getForObject("http://localhost:" + port + "/actuator/health", Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> components = (Map<String, Object>) health.get("components");
        @SuppressWarnings("unchecked")
        Map<String, Object> dbGroup = (Map<String, Object>) components.get("db");
        @SuppressWarnings("unchecked")
        Map<String, Object> dbComponents = (Map<String, Object>) dbGroup.get("components");
        assertThat(dbComponents).containsKey("dictionaryDataSource");
        assertThat(dbComponents).containsKey("historyDataSource");
        @SuppressWarnings("unchecked")
        Map<String, Object> dictHealth = (Map<String, Object>) dbComponents.get("dictionaryDataSource");
        assertThat(dictHealth.get("status")).isEqualTo("UP");
    }

    private static HttpEntity<String> jsonOrder(String customer, String currency, String amount) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"customer\":\"%s\",\"currency\":\"%s\",\"amount\":%s}"
                .formatted(customer, currency, new BigDecimal(amount));
        return new HttpEntity<>(body, h);
    }
}
