package ru.otus.gulash;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JwtStarter {
    public static void main(String[] args){
        SpringApplication.run( JwtStarter.class, args );

        System.out.println("""
            Используем файл http_requests.md для демонстрации работы
            """);
    }
}
