package org.gulash.service.implementation;

import lombok.RequiredArgsConstructor;
import org.gulash.dao.QuestionDao;
import org.gulash.service.IOService;
import org.gulash.service.TestService;

@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final IOService ioService;

    private final QuestionDao questionDao;

    @Override
    public void executeTest() {
        ioService.printLine("");
        ioService.printFormattedLine("Please answer the questions below%n");
        questionDao.findAll().forEach(System.out::println);
    }
}
