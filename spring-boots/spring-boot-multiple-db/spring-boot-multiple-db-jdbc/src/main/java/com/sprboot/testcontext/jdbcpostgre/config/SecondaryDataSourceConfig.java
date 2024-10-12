package com.sprboot.testcontext.jdbcpostgre.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class SecondaryDataSourceConfig {
    @Value("${additional-datasource.db-secondary.flyway-location}")
    private String flywayLocations;

    @Bean
    @ConfigurationProperties("additional-datasource.db-secondary")
    public DataSourceProperties secondaryDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource secondaryDataSource() {
        return secondaryDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean
    public JdbcTemplate secondaryJdbcTemplate() {
        return new JdbcTemplate(secondaryDataSource());
    }

    @Bean
    public Flyway flywaySecondaryDataSource(){
        return Flyway.configure()
            .dataSource(secondaryDataSource())
            .locations(flywayLocations)
            .load();
    }

    @Bean
    public FlywayMigrationInitializer flywayMigrationInitializerSecondary() {
        return new FlywayMigrationInitializer(flywaySecondaryDataSource());
    }
}
