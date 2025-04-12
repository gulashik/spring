package ru.otus.hw.migration.item.processor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GenreItemProcessor {

    @Bean
    public GenreItemProcessorImpl genreProcessorProcessor() {
        return new GenreItemProcessorImpl();
    }
}
