package ru.otus.hw.config.h2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

@Configuration
public class H2BatchSchemaInitializer {

    private final DataSource dataSource;

    // todo DataSource по H2. application.yml - spring.datasource
    public H2BatchSchemaInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Bean
    public DataSourceInitializer dataSourceInitializer() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        // todo где лежит скрипты наката схемы БД
        populator.addScript(new ClassPathResource("org/springframework/batch/core/schema-h2.sql"));
        populator.setContinueOnError(true);

        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        initializer.setDatabasePopulator(populator);
        return initializer;
    }
}
