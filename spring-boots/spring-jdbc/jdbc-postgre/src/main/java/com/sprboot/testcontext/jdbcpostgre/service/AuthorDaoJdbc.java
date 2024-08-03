package com.sprboot.testcontext.jdbcpostgre.service;

import com.sprboot.testcontext.jdbcpostgre.domain.Author;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Repository
public class AuthorDaoJdbc {
    @NonNull
    private final JdbcOperations jdbc;
    @NonNull
    private final NamedParameterJdbcTemplate namedJdbc;

    public List<Author> findAll() {
        return jdbc.query("select * from author", new AuthorMapper());
    }

    private static class AuthorMapper implements RowMapper<Author> {
        @Override
        public Author mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Author(
                    rs.getLong("id"), /*Long id*/
                    rs.getObject("first_name", String.class), /*String firstName*/
                    rs.getObject("last_name", String.class), /*String lastName*/
                    getDate( rs.getObject("date_of_birth", Date.class)), /*LocalDate dateOfBirth*/
                    rs.getInt("year_of_birth"), /*int yearOfBirth*/
                    rs.getInt("distinguished")/*int distinguished*/
            );
        }

        private LocalDate getDate(Date date) {
            if (date == null) {
                return null;
            }
            return date.toLocalDate();
        }
    }
}
