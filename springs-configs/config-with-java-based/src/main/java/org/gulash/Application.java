package org.gulash;

import org.gulash.config.AppConfig;
import org.springframework.context.ApplicationContext;
import org.gulash.service.TestRunnerService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Application {
    public static void main(String[] args) {
        // todo Используем Java-based конфиг для создания контекста
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        var testRunnerService = context.getBean(TestRunnerService.class);
        testRunnerService.run();

    }
}