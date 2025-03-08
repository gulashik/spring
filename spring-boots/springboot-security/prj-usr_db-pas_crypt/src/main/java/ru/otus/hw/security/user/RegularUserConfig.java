package ru.otus.hw.security.user;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import ru.otus.hw.security.service.UserDetailsServiceImpl;

@RequiredArgsConstructor
@Configuration
public class RegularUserConfig implements ApplicationRunner {

    private final UserDetailsServiceImpl userDetailsService;

    @Override
    public void run(ApplicationArguments args) {
        userDetailsService.createUser("user", "user", "USER");
        userDetailsService.createUser("admin", "admin", "ADMIN");
    }
}
