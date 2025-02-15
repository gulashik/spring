package ru.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoR2dbc {

   public static void main(String[] args) {
       // todo запустить из runDb.md для создания/пересоздания контейнера
       // todo HttpRequests.http запускалка запросов
        SpringApplication.run(DemoR2dbc.class, args);
    }
}