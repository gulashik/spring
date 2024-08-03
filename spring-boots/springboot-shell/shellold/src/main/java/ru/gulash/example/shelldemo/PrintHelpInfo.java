package ru.gulash.example.shelldemo;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class PrintHelpInfo {
    @PostConstruct
    private void print() {
        var info = """
                ***********************************************************
                
                Надо вначале команду "l", "l имя" или "login", "login имя"
                    по умолчанию имя будет AnyUser
                Потом ввести "a", "access", "get-access"
                Для выхода нужно "exit"
                Для просмотра доступных команда "help"
                
                ***********************************************************
                """;
        System.out.println(info);
    }
}