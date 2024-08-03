package org.gulash.dao;

import org.gulash.config.AppConfig;
import org.gulash.config.TestFileProvider;
import org.gulash.dao.implementation.CsvQuestionDao;
import org.gulash.domain.Answer;
import org.gulash.domain.Question;
import org.gulash.mapper.LineMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
// todo Используем Java-based конфиг для создания контекста в тестах
@ContextConfiguration(classes = {AppConfig.class})

class CsvQuestionDaoTest {
    @Autowired
    @Spy
    private TestFileProvider appProperties;

    @Autowired
    @Spy
    private LineMapper lineMapper;

    @InjectMocks
    private CsvQuestionDao questionDao;

    @Test
    public void testExampleBean() {
        // arrange
        List<Answer> answers = List.of(
                new Answer("1) Correct answer Q1", true),
                new Answer("2) Incorrect answer1 Q1", false),
                new Answer("3) Incorrect answer2 Q1", false)
        );
        Question question = new Question("Question1?", answers);

        Mockito.when(appProperties.getTestFileName()).thenReturn("test.csv");

        // act
        List<Question> all = questionDao.findAll();

        // assert
        assertEquals(all.size(), 1);
        assertEquals(all.get(0), question);
    }
}