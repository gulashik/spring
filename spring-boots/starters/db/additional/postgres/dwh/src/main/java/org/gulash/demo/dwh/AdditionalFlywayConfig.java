package org.gulash.demo.dwh;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Запускает Flyway-миграции для дополнительных источников {@code dictionary} и {@code history}.
 *
 * <h2>Почему это нужно явно</h2>
 * Spring Boot из коробки автоматически мигрирует ТОЛЬКО «основной» DataSource
 * (через {@link org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration}).
 * Дополнительные DataSource'ы стартер регистрирует, но Flyway о них «не знает» —
 * нужно создать {@link Flyway}-бины и {@link FlywayMigrationInitializer}-ы вручную.
 *
 * <h2>Структура миграций</h2>
 * <pre>
 *   src/main/resources/db/migration/
 *     dwh/         (V1__init.sql, V2__...) — основная БД
 *     dictionary/  (V1__init.sql)         — справочник
 *     history/     (V1__init.sql)         — история
 * </pre>
 * Каждый bean'у Flyway передаётся свой {@code locations} — это ключ изоляции миграций.
 *
 * <h2>Подводные камни</h2>
 * <ul>
 *   <li>Бин с именем {@code flyway} (без квалификатора) — это «основной», его создаёт Spring Boot.
 *       Если объявить ещё один без имени — будет конфликт. Поэтому здесь — explicit names.</li>
 *   <li>Порядок выполнения: {@link FlywayMigrationInitializer} должен отработать ДО
 *       первого использования {@link javax.sql.DataSource}, иначе DAO упадут на «no such table».
 *       Для этого мы делаем initializer'ы зависимыми от соответствующего DataSource.</li>
 *   <li>В Postgres имя схемы по умолчанию — {@code public}. Если в свойствах источника
 *       задана своя схема, передайте её и в {@link Flyway} (см. {@code .schemas(...)}).</li>
 * </ul>
 */
@Configuration(proxyBeanMethods = false)
public class AdditionalFlywayConfig {

    /**
     * Бин Flyway для ОСНОВНОЙ dwh-БД.
     *
     * <p>Почему явно, а не автоматически:
     * как только в контексте появляется ЛЮБОЙ {@link Flyway}-бин (наши {@code dictionaryFlyway} /
     * {@code historyFlyway}), стандартный {@code FlywayAutoConfiguration} выключается из-за
     * {@code @ConditionalOnMissingBean(Flyway.class)}. Поэтому если уже создаём свои Flyway —
     * обязательно создаём и «основной» руками. Иначе схема dwh не мигрируется,
     * и при первом запросе будет {@code relation "orders" does not exist}.</p>
     */
    @Bean
    public Flyway dwhFlyway(@Qualifier("dataSource") DataSource primaryDataSource) {
        // @Qualifier("dataSource") явно указывает на «основной» DataSource Spring Boot
        // (имя бина — "dataSource"). Без квалификатора Spring увидит ТРИ бина DataSource
        // (dataSource, dictionaryDataSource, historyDataSource) и упадёт с
        // NoUniqueBeanDefinitionException — это типичный подводный камень при
        // нескольких DataSource в одном контексте.
        return Flyway.configure()
                .dataSource(primaryDataSource)
                .locations("classpath:db/migration/dwh")
                .baselineOnMigrate(true)
                .load();
    }

    @Bean
    public FlywayMigrationInitializer dwhFlywayInitializer(@Qualifier("dwhFlyway") Flyway dwhFlyway) {
        return new FlywayMigrationInitializer(dwhFlyway, null);
    }

    @Bean
    public Flyway dictionaryFlyway(@Qualifier("dictionaryDataSource") DataSource dictionaryDataSource) {
        return Flyway.configure()
                .dataSource(dictionaryDataSource)
                .locations("classpath:db/migration/dictionary")
                .baselineOnMigrate(true)   // если в БД уже есть таблицы — Flyway создаст baseline-запись
                .load();
    }

    @Bean
    public FlywayMigrationInitializer dictionaryFlywayInitializer(
            @Qualifier("dictionaryFlyway") Flyway dictionaryFlyway) {
        return new FlywayMigrationInitializer(dictionaryFlyway, null);
    }

    @Bean
    public Flyway historyFlyway(@Qualifier("historyDataSource") DataSource historyDataSource) {
        return Flyway.configure()
                .dataSource(historyDataSource)
                .locations("classpath:db/migration/history")
                .baselineOnMigrate(true)
                .load();
    }

    @Bean
    public FlywayMigrationInitializer historyFlywayInitializer(
            @Qualifier("historyFlyway") Flyway historyFlyway) {
        return new FlywayMigrationInitializer(historyFlyway, null);
    }
}
