package ru.gulash.example.shelldemo;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class AppServiceAction {
    public String executeAction() {
        return "Доступ получен";
    }
}
