package org.gulash.demo.dwh;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * DAO для БД {@code history}.
 *
 * <h2>Демонстрация {@link NamedParameterJdbcTemplate}</h2>
 * Стартер регистрирует и обычный, и «именованный» JdbcTemplate. NamedParam-вариант
 * безопаснее в чтении: вместо {@code ?} — {@code :name}, что:
 * <ul>
 *   <li>устраняет «магические числа» аргументов;</li>
 *   <li>предотвращает ошибки порядка параметров;</li>
 *   <li>лучше читается в SQL ревью.</li>
 * </ul>
 *
 * <h2>Имя бина</h2>
 * {@code historyNamedJdbcTemplate} — снова контракт со стартером
 * (см. {@code BeanNames#namedJdbcTemplate}).
 */
@Repository
public class HistoryDao {

    private final NamedParameterJdbcTemplate jdbc;

    public HistoryDao(@Qualifier("historyNamedJdbcTemplate") NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public record Event(long id, long orderId, String action, OffsetDateTime at) {}

    public void record(long orderId, String action) {
        jdbc.update(
                "INSERT INTO order_events (order_id, action) VALUES (:orderId, :action)",
                new MapSqlParameterSource(Map.of("orderId", orderId, "action", action)));
    }

    public List<Event> recent(int limit) {
        return jdbc.query(
                "SELECT id, order_id, action, at FROM order_events ORDER BY id DESC LIMIT :limit",
                new MapSqlParameterSource("limit", limit),
                (rs, n) -> new Event(
                        rs.getLong("id"),
                        rs.getLong("order_id"),
                        rs.getString("action"),
                        rs.getObject("at", OffsetDateTime.class)
                ));
    }
}
