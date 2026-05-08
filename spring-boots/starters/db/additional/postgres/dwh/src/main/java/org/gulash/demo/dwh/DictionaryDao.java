package org.gulash.demo.dwh;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * DAO для справочной БД {@code dictionary}, подключённой через стартер.
 *
 * <h2>Ключевая идея</h2>
 * Здесь обязателен {@link Qualifier} — иначе Spring не поймёт, какой
 * {@link JdbcTemplate} нужен, и упадёт с {@code NoUniqueBeanDefinitionException}
 * (или, если есть primary, выберет основной — что было бы тихим багом).
 *
 * <p>Имя квалификатора {@code dictionaryJdbcTemplate} формируется стартером
 * по правилу {@code <name> + "JdbcTemplate"}, где {@code name} — ключ карты
 * {@code app.datasources.<name>}.
 */
@Repository
public class DictionaryDao {

    private final JdbcTemplate jdbc;

    public DictionaryDao(@Qualifier("dictionaryJdbcTemplate") JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public record Currency(String code, String name) {}

    public List<Currency> findCurrencies() {
        return jdbc.query(
                "SELECT code, name FROM currencies ORDER BY code",
                (rs, n) -> new Currency(rs.getString("code"), rs.getString("name")));
    }
}
