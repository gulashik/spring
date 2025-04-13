package ru.otus.hw.config.postgre;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;


@ConfigurationProperties(prefix = "spring.liquibase")
public record LiquibasePropertiesCustom(
    Boolean enabled,
    String changeLog,
    String contexts,
    String defaultSchema,
    String liquibaseSchema,
    String liquibaseTablespace,
    Boolean dropFirst,
    Boolean shouldRun,
    Boolean testRollbackOnUpdate,
    Map<String, String> parameters,
    String rollbackFile
) {
}
