package com.sprboot.testcontext.jdbcpostgre;

import com.sprboot.testcontext.jdbcpostgre.domain.Author;
import com.sprboot.testcontext.jdbcpostgre.service.AuthorDaoJdbc;
import com.sprboot.testcontext.jdbcpostgre.service.DailyActivityDaoJdbc;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class AppRunner implements ApplicationRunner {

    private final AuthorDaoJdbc authorDao;
    private final DailyActivityDaoJdbc activityDaoJdbc;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<Author> authors = authorDao.findAll();
        System.out.println(authors);

        long generated = activityDaoJdbc.newRecGenerate("App");
        System.out.println("Получили ключ " + generated);
    }
}
