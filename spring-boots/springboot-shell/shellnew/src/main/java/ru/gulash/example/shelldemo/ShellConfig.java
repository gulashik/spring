package ru.gulash.example.shelldemo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.Availability;
import org.springframework.shell.AvailabilityProvider;

// todo bean-ы для вычисления доступности команды
@Configuration
public class ShellConfig {
    // todo нужно что-то одно вернуть Availability.available() или Availability.unavailable("причина")
    @Bean
    public AvailabilityProvider publishEventCommandAvailabilityProvider(InMemoryLoginContext loginContext) {
        return () -> loginContext.isUserLoggedIn()
                ? Availability.available()
                : Availability.unavailable("Сначала залогиньтесь");
    }
}
