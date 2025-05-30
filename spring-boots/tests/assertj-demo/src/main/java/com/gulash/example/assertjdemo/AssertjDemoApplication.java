package com.gulash.example.assertjdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AssertjDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AssertjDemoApplication.class, args);

        System.out.println("""
            Н2 смотрим в http://localhost:8080/h2-console
            Driver Class=org.h2.Driver
            JDBC URL= jdbc:h2:mem:testdb
            User name SA
            DB=testdb
            """);
    }
}
