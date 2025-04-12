package ru.otus.hw.migration.item.processor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BookItemProcessor {

    @Bean
    public BookItemProcessorImpl bookProcessorProcessor() {
        return new BookItemProcessorImpl();
    }
}
