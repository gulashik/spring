package com.sprboot.testcontext.jdbcpostgre.service;

import com.sprboot.testcontext.jdbcpostgre.domain.Author;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@Repository
public class AuthorDaoJdbc {
    // Arrays.stream(applicationContext.getBeanDefinitionNames()).forEach(System.out::println);
    private final ApplicationContext applicationContext;

    /*============Primary=================*/
    // todo будет привязка основного data source с @Primary
    private final JdbcTemplate jdbc;

    // todo будет привязка data source с @Primary
    private final NamedParameterJdbcTemplate namedJdbc;

    // todo будет привязка вторичного data source через конструктор по имени
    private final JdbcTemplate primaryJdbcTemplate;

    /*============Secondary=================*/
    // todo будет привязка вторичного data source через конструктор по имени
    private final JdbcTemplate secondaryJdbcTemplate;

    @Autowired
    public AuthorDaoJdbc(
        NamedParameterJdbcTemplate namedJdbc,
        JdbcTemplate jdbc,
        @Qualifier("primaryJdbcTemplate") JdbcTemplate primaryJdbcTemplate,
        @Qualifier("secondaryJdbcTemplate") JdbcTemplate secondaryJdbcTemplate,
        ApplicationContext applicationContext
    )
    {
        this.secondaryJdbcTemplate = secondaryJdbcTemplate;
        this.primaryJdbcTemplate = primaryJdbcTemplate;
        this.namedJdbc = namedJdbc;
        this.jdbc = jdbc;
        this.applicationContext = applicationContext;
    }

    public List<Author> findAll() {
        // todo будет primary data source то что с @Primary
        List<Author> primaryJdbc = jdbc.query("select * from author", new AuthorMapper());
        List<Author> primaryNamedJdbc = namedJdbc.query("select * from author", new AuthorMapper());
        List<Author> primaryByName = primaryJdbcTemplate.query("select * from author", new AuthorMapper());
        // todo выводим настройки DataSource
        DataSource dataSourcePrimary = primaryJdbcTemplate.getDataSource();
        if (dataSourcePrimary instanceof com.zaxxer.hikari.HikariDataSource) {
            com.zaxxer.hikari.HikariDataSource hikariDataSource = (com.zaxxer.hikari.HikariDataSource) dataSourcePrimary;
            System.out.println("Использовался Primary DataSource");
            System.out.println("Primary URL: " + hikariDataSource.getJdbcUrl());
            System.out.println("Primary Username: " + hikariDataSource.getUsername());
            System.out.println("Primary Max Pool Size: " + hikariDataSource.getMaximumPoolSize());
        }

        // todo будет второй data source
        List<Author> secondaryByName = secondaryJdbcTemplate.query("select * from author", new AuthorMapper());
        // todo выводим настройки DataSource
        DataSource dataSourceSecondary = secondaryJdbcTemplate.getDataSource();
        if (dataSourceSecondary instanceof com.zaxxer.hikari.HikariDataSource) {
            System.out.println("Использовался Secondary DataSource");
            com.zaxxer.hikari.HikariDataSource hikariDataSource = (com.zaxxer.hikari.HikariDataSource) dataSourceSecondary;
            System.out.println("Secondary URL: " + hikariDataSource.getJdbcUrl());
            System.out.println("Secondary Username: " + hikariDataSource.getUsername());
            System.out.println("Secondary Max Pool Size: " + hikariDataSource.getMaximumPoolSize());
        }

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
