package ru.otus.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {

    public static void main( String[] args ) {
        SpringApplication.run( Main.class );
        // http://localhost:8080/
        System.out.println("""
            Переходим
             -> http://localhost:8080/
            """);
    }
}
