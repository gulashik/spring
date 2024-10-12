package com.sprboot.testcontext.jdbcpostgre.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class PrimaryDataSourceConfig {
//    @Value("${additional-datasource.db-primary.flyway-location}")
//    private String flywayLocations;

    @Primary // todo указываем для основного бина
    @Bean
    @ConfigurationProperties("additional-datasource.db-primary")
    public DataSourceProperties primaryDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean
    public DataSource primaryDataSource() {
        return primaryDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Primary
    @Bean
    public JdbcTemplate primaryJdbcTemplate() {
        return new JdbcTemplate(primaryDataSource());
    }
    // todo Flyway настройки возьмуться из biuld.gradle
//    @Primary
//    @Bean
//    public Flyway flyway(){
//        return Flyway.configure()
//            .dataSource(primaryDataSource())
//            .locations(flywayLocations)
//            .load();
//    }
//
//    @Primary
//    @Bean
//    public FlywayMigrationInitializer flywayMigrationInitializer() {
//        return new FlywayMigrationInitializer(flyway());
//    }
}
