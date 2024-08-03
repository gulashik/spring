package org.gulash;

import org.springframework.context.ApplicationContext;
import org.gulash.service.TestRunnerService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
// todo указываем область сканирования
@ComponentScan
public class Application {
    public static void main(String[] args) {
        // todo указываем класс с @ComponentScan можно себя же
        ApplicationContext context = new AnnotationConfigApplicationContext(Application.class/*себя же указали*/);
        var testRunnerService = context.getBean(TestRunnerService.class);
        testRunnerService.run();
    }
}