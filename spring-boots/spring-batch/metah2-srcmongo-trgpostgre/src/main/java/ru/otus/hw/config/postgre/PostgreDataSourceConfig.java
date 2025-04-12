package ru.otus.hw.config.postgre;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
//@EnableBatchProcessing // можно не указывать
@EnableJpaRepositories(
    basePackages = {
        "ru.otus.hw.model.targetdb.entity", // todo где лежат JPA сущности
        "ru.otus.hw.repositories" // todo где лежат JPA репозитории
    },
    entityManagerFactoryRef = "postgresEntityManagerFactory", // todo создали сами
    transactionManagerRef = "postgresTransactionManager" // todo создали сами
)
public class PostgreDataSourceConfig {

    // PostgreSQL для целевых данных
    // todo DataSourceProperties -> DataSource
    @Bean
    @ConfigurationProperties(prefix = "spring.target-datasource") // todo подтягиваем из application.yml
    public DataSourceProperties postgresDataSourceProperties() {
        return new DataSourceProperties();
    }

    // todo DataSource -> EntityManagerFactory
    @Bean(name = "postgresDataSource")
    @Qualifier("postgresDataSource")
    public DataSource postgresDataSource() {
        return postgresDataSourceProperties().initializeDataSourceBuilder().build();
    }

    // todo Явная настройка EntityManagerFactoryBuilder -> EntityManagerFactory
    @Bean
    public EntityManagerFactoryBuilder entityManagerFactoryBuilder() {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(true);
        vendorAdapter.setShowSql(true);

        return new EntityManagerFactoryBuilder(vendorAdapter, new HashMap<>(), null);
    }

    // todo EntityManagerFactory = DataSource+EntityManagerFactoryBuilder
    @Bean(name = "postgresEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean postgresEntityManagerFactory(
        EntityManagerFactoryBuilder builder,
        @Qualifier("postgresDataSource") DataSource dataSource // явно нужный postgresDataSource
    ) {
        return builder
            .dataSource(dataSource)
            .packages("ru.otus.hw.model.targetdb.entity") // todo Указываем пакеты для сканирования сущностей
            .persistenceUnit("postgres")
            // todo Можно добавить дополнительные свойства, если нужно. Свойства указаны в application.yml - spring.jpa
            //.properties(additionalJpaProperties())
            .build();
    }

/*    private Map<String, String> additionalJpaProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "none"); // или другое значение в зависимости от ваших потребностей
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        // Можно добавить и другие свойства
        return properties;
    }*/

    // todo TransactionManager
    @Bean(name = "postgresTransactionManager")
    public PlatformTransactionManager postgresTransactionManager(
        @Qualifier("postgresEntityManagerFactory") EntityManagerFactory entityManagerFactory // todo явно указываем
    ) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    // todo EntityManager
    @Bean(name = "postgresEntityManager")
    @PersistenceContext(unitName = "postgresEntityManagerFactory")
    public EntityManager postgresEntityManager(
        @Qualifier("postgresEntityManagerFactory") EntityManagerFactory entityManagerFactory // todo явно указываем
    ) {
        return entityManagerFactory.createEntityManager();
    }
}
