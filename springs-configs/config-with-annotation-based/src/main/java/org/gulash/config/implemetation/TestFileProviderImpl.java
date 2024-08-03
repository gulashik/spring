package org.gulash.config.implemetation;

import org.gulash.config.TestConfig;
import org.gulash.config.TestFileNameProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// todo привязываем нужное из файла .properties
@Component
public record TestFileProviderImpl(
        @Value("${test.rightAnswersCountToPass}")
        int rightAnswersCountToPass,

        @Value("${test.file.name}")
        String testFileName,

        @Value("${test.file.question.tag}")
        String questionTag,

        @Value("${test.file.answer.tag}")
        String answerTag,

        @Value("${test.file.answer.splitter}")
        String answerSplitter,

        @Value("${test.file.skipLines}")
        int skipLines
) implements TestConfig, TestFileNameProvider{}
