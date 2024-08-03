package org.gulash.service.implemetation;

import lombok.RequiredArgsConstructor;
import org.gulash.dao.QuestionDao;
import org.gulash.domain.Student;
import org.gulash.domain.TestResult;
import org.gulash.service.IOService;
import org.gulash.service.QuestionService;
import org.gulash.service.TestService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final IOService ioService;

    private final QuestionDao questionDao;

    private final QuestionService questionService;

    @Override
    public TestResult executeTestFor(Student student) {
        ioService.printLine("");
        ioService.printFormattedLine("Please answer the questions below%n");
        var questions = questionDao.findAll();
        var testResult = new TestResult(student);

        for (var question: questions) {
            var isAnswerValid = questionService.askQuestion(question);
            testResult.applyAnswer(question, isAnswerValid);
        }
        return testResult;
    }
}
