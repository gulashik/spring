package ru.gulash.spring;

import org.h2.tools.Console;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import ru.gulash.spring.dao.PersonDao;

@SpringBootApplication
public class Main {

    public static void main(String[] args) throws Exception {

        ApplicationContext context = SpringApplication.run(Main.class);

        PersonDao dao = context.getBean(PersonDao.class);

        // todo будет запуск браузера если нужно зачем-то
        Console.main(args);
    }
}


