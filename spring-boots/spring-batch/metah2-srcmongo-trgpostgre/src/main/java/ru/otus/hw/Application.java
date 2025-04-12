package ru.otus.hw;

import com.github.cloudyrock.spring.v5.EnableMongock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableMongock
@EnableMongoRepositories
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        /*
        Смотрим в H2 накатилась схема по SpringBatch,  oc: openConsoleH2
        shell: oc

        Смотрим в Embedded Mongo
        localhost 8099
        database: mdb
        user: root
        password: root

        Запускаем миграцию Mongo -> Postgres, sm: startMigration
        shell: sm

        Смотрим в docker Postgres
        sma ShowMigrationAuthor
        shell:sma: ShowMigrationAuthor

        smc ShowMigrationComment
        shell:smc: ShowMigrationComment

        smb ShowMigrationBook
        shell:smb: ShowMigrationBook

        smg ShowMigrationGenre
        shell:smg: ShowMigrationGenre
       */
        SpringApplication.run(Application.class, args);
    }
}
