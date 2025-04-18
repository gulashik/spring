package ru.otus.spring;

import com.github.cloudyrock.spring.v5.EnableMongock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;
import ru.otus.spring.domain.Person;
import ru.otus.spring.repostory.PersonRepository;

@EnableMongock // todo включить зависимость Mongock(миграция для mongodb)
@EnableMongoRepositories // todo включить
@SpringBootApplication
public class Main {

    public static void main(String[] args) throws InterruptedException {
        ApplicationContext context = SpringApplication.run(Main.class);

        PersonRepository repository = context.getBean(PersonRepository.class);

        repository.save(new Person("Dostoevsky"));

        Thread.sleep(3000);

        System.out.println("\n\n\n----------------------------------------------\n\n");
        System.out.println("Авторы в БД:");
        repository.findAll().forEach(p -> System.out.println(p.getName()));
        System.out.println("\n\n----------------------------------------------\n\n\n");
        /*
        * Порт по mongo можно увидеть в логах MongoClient with metadata ... clusterSettings={hosts=[localhost:63877] ...
        */
    }
}




