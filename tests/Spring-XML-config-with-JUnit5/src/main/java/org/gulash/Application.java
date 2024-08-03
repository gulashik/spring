package org.gulash;

import org.springframework.context.ApplicationContext;
import org.gulash.service.TestRunnerService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Application {
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("spring-context.xml");

        var testRunnerService = context.getBean(TestRunnerService.class);
        testRunnerService.run();

    }
}