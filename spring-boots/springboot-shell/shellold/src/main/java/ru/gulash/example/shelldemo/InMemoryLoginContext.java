package ru.gulash.example.shelldemo;

import org.springframework.stereotype.Component;

import static java.util.Objects.nonNull;

@Component
public class InMemoryLoginContext {
    private String userName;

    public void login(String userName) {
        this.userName = userName;
    }

    public boolean isUserLoggedIn() {
        return nonNull(userName);
    }
}
