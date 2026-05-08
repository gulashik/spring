package org.gulash.demo.dwh;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * DAO для основной БД dwh.
 *
 * <h2>Внедрение «основного» {@link JdbcTemplate}</h2>
 * Здесь НЕТ {@code @Qualifier}: Spring Boot создаёт «дефолтный» JdbcTemplate,
 * настроенный на {@code spring.datasource.*}. Он primary, поэтому подбирается
 * по типу автоматически. Это контрастирует с DAO для dictionary/history, где
 * нужны квалификаторы — это и есть наглядная демонстрация различий.
 */
@Repository
public class OrderDao {

    private final JdbcTemplate jdbc;

    public OrderDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public record Order(long id, String customer, String currency, BigDecimal amount, OffsetDateTime createdAt) {}

    public List<Order> findAll() {
        return jdbc.query(
                "SELECT id, customer, currency, amount, created_at FROM orders ORDER BY id",
                (rs, n) -> new Order(
                        rs.getLong("id"),
                        rs.getString("customer"),
                        rs.getString("currency"),
                        rs.getBigDecimal("amount"),
                        rs.getObject("created_at", OffsetDateTime.class)
                ));
    }

    public long create(String customer, String currency, BigDecimal amount) {
        Long id = jdbc.queryForObject(
                "INSERT INTO orders (customer, currency, amount) VALUES (?, ?, ?) RETURNING id",
                Long.class, customer, currency, amount);
        if (id == null) {
            throw new IllegalStateException("RETURNING id вернул null — проверьте схему таблицы orders");
        }
        return id;
    }
}
