package ru.otus.hw.config.postgre;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class LiquibaseDataSourceConfig {

    // todo Postgres + Liquibase
    @Bean
    public SpringLiquibase liquibase(
        @Qualifier("postgresDataSource") DataSource dataSource
    ) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:db/changelog/db.changelog-master.yaml");

        return liquibase;
    }
}
