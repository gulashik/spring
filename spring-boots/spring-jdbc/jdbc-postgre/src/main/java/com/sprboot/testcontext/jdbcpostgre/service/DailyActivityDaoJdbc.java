package com.sprboot.testcontext.jdbcpostgre.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class DailyActivityDaoJdbc {

    private final JdbcOperations jdbc;

    private final NamedParameterJdbcTemplate namedJdbc;

    public int newRecGenerate(String name) {
        // хранилка аргументов
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", name);

        // хранилка сгенерированного ключа
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        int update = namedJdbc.update(
                """
                    INSERT INTO users (name) VALUES (:name);
                    """,
                params, // параметры
                keyHolder, // куда кладём
                new String[]{"id"} // имя столбца, где ключ лежит
        );
        return keyHolder.getKey().intValue(); // возвращаем
    }
}
