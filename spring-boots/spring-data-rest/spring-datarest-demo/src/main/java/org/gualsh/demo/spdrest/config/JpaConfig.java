package org.gualsh.demo.spdrest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Конфигурация JPA-аудита для автоматического заполнения полей createdAt и updatedAt.
 *
 * Аннотация @EnableJpaAuditing включает функциональность аудита JPA.
 * Аннотация @EnableJpaRepositories включает поддержку репозиториев Spring Data JPA.
 * Аннотация @EnableTransactionManagement включает управление транзакциями на основе аннотаций.
 */
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "org.gualsh.demo.spdrest.repository")
@EnableTransactionManagement
public class JpaConfig {
    // Конфигурационные бины можно добавить здесь при необходимости
}