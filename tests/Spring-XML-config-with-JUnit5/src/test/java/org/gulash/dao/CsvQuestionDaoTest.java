package org.gulash.dao;

import org.gulash.config.AppProperties;
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

@ExtendWith({SpringExtension.class, MockitoExtension.class}) // todo используем @ExtendWith({SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(locations = {"classpath:spring-context.xml"}) // todo путь до XML конфига
class CsvQuestionDaoTest {
    @Autowired
    @Spy
    private AppProperties appProperties;

    @Autowired
    @Spy
    private LineMapper lineMapper;

    @InjectMocks // todo inject-им всё что @Spy
    private CsvQuestionDao csvQuestionDao;

    @Test
    public void testExampleBean() {
        // arrange
        List<Answer> answers = List.of(
                new Answer("Correct answer Q1", true),
                new Answer("Incorrect answer1 Q1", false),
                new Answer("Incorrect answer2 Q1", false)
        );
        Question question = new Question("Question1?", answers);

        // todo изменяем поведение
        Mockito.when(appProperties.getTestFileName()).thenReturn("test.csv");

        // act
        List<Question> all = csvQuestionDao.findAll();

        // assert
        assertEquals(all.size(), 1);
        assertEquals(all.get(0), question);
    }
}