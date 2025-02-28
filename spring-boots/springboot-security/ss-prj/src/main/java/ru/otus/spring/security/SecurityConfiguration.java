package ru.otus.spring.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import ru.otus.spring.security.filter.MyOwnFilter;

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
            // todo Отключение CSRF-защиты. Для RestAPI с JWT не актуально
            .csrf(AbstractHttpConfigurer::disable)
            // todo ???
            .sessionManagement(
                (session) ->
                    session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
            )
            // todo ???
            .authorizeHttpRequests(
                (authorize) -> authorize
                    // todo requestMatchers - набор шаблонов URL-путей с определённым правилам безопасности.
                    .requestMatchers("/").permitAll()
                    .requestMatchers(HttpMethod.GET, "/public").permitAll() // Доступно всем
                    // .requestMatchers("/admin/**").hasRole("ADMIN") // Только для администраторов
                    .requestMatchers("/authenticated", "/success").authenticated()

                    // todo anyRequest - является "заглушкой" для всех запросов, которые не были явно настроены ранее
                    .anyRequest().permitAll() // permitAll()- Все остальные запросы доступны всем
                   //.anyRequest().authenticated() // authenticated() - Все остальные запросы требуют аутентификации
            )
            .anonymous(a -> a.principal(new AnonimusUD()).authorities("ROLE_ANONYMOUS"))
            .addFilterAfter(new MyOwnFilter(), AuthorizationFilter.class)
            // todo HTTP Basic Authentication - выпадающее окошко с логином и паролем
            // .httpBasic(Customizer.withDefaults())

            // .formLogin(Customizer.withDefaults())
            .formLogin(
                fm ->
                    fm.defaultSuccessUrl("/success")
                        .failureForwardUrl("/fail")
            )
            .rememberMe(rm -> rm.key("AnyKey")
                .tokenValiditySeconds(600))
        ;
        return http.build();
    }

    // todo PasswordEncoder отвечает за шифрование (хеширование) паролей перед их сохранением в базе данных
    //      и за проверку совпадения пароля при аутентификации пользователя.
    // https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/storage.html
    @SuppressWarnings("deprecation")
    @Bean
    public PasswordEncoder passwordEncoder() {
        //return new BCryptPasswordEncoder(10);
        return NoOpPasswordEncoder.getInstance();

    }

    // todo InMemoryUserDetailsManager - откуда Spring Security берёт информацию о пользователях
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
