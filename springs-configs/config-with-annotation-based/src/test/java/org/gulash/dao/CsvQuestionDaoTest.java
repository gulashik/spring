package org.gulash.dao;


import org.gulash.Application;
import org.gulash.dao.implemetation.CsvQuestionDao;
import org.gulash.domain.Answer;
import org.gulash.domain.Question;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith({SpringExtension.class})
@ContextConfiguration(classes = {Application.class})
@TestPropertySource("classpath:application-test.properties")
class CsvQuestionDaoTest {

    @Autowired
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

        // act
        List<Question> all = questionDao.findAll();

        // assert
        assertEquals(all.size(), 1);
        assertEquals(all.get(0), question);
    }
}