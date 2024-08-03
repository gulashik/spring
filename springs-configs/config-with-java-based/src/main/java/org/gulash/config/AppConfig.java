package org.gulash.config;

import org.gulash.config.implemetation.TestFileProviderImpl;
import org.gulash.dao.QuestionDao;
import org.gulash.dao.implementation.CsvQuestionDao;
import org.gulash.mapper.LineMapper;
import org.gulash.service.IOService;
import org.gulash.service.TestRunnerService;
import org.gulash.service.TestService;
import org.gulash.service.implementation.IOServiceStreams;
import org.gulash.service.implementation.TestRunnerServiceImpl;
import org.gulash.service.implementation.TestServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// todo Собираем Java-based контекст
@Configuration
public class AppConfig {
    @Bean
    TestFileProvider appProperties() {
        return new TestFileProviderImpl(
                "questions.csv",
                ";",
                "\\|",
                "%",
                1
        );
    }

    @Bean
    IOService ioService() {
        return new IOServiceStreams();
    }

    @Bean
    LineMapper lineMapper() {
        return new LineMapper();
    }

    @Bean
    QuestionDao questionDao(TestFileProvider appProperties, LineMapper lineMapper) {
        return new CsvQuestionDao(appProperties, lineMapper);
    }

    @Bean
    TestService testService(IOService ioService, QuestionDao questionDao) {
        return new TestServiceImpl(ioService, questionDao);
    }

    @Bean
    TestRunnerService testRunnerService(TestService testService) {
        return new TestRunnerServiceImpl(testService);
    }
}