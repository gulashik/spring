package ru.otus.spring.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration

// todo указывает Spring, что класс содержит конфигурацию безопасности для веб-приложения.
//  при использовании @EnableWebSecurity стандартная конфигурация отключается
@EnableWebSecurity
public class SecurityConfiguration {

    // todo Нужно реализовать SecurityFilterChain bean
    // todo SecurityFilterChain(часть механизма фильтров Servlet API) — это цепочка фильтров, которая управляет
    //  процессом обработки всех HTTP-запросов и их защитой (Аутентификация, Авторизация, Обработка запросов/ответов).
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(
                (authorize) -> authorize
                    .requestMatchers("/public").permitAll() // Доступно всем
                    // .requestMatchers("/admin/**").hasRole("ADMIN") // Только для администраторов
                    .requestMatchers("/authenticated").authenticated()

                    // todo anyRequest - является "заглушкой" для всех запросов, которые не были явно настроены ранее
                    .anyRequest().permitAll() // permitAll()- Все остальные запросы доступны всем
                    //.anyRequest().authenticated() // authenticated() - Все остальные запросы требуют аутентификации
            )
            .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder(10);
        return NoOpPasswordEncoder.getInstance();

    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails user = User
            .builder()
            .username("user")
            .password("password")
            .roles("USER")
            .build();
        return new InMemoryUserDetailsManager(user);
    }
}
