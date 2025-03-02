package ru.otus.spring.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
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
        // todo org.springframework.security.web.FilterChainProxy.doFilter - можно подебажить
        http
            // todo Отключение CSRF-защиты. Для RestAPI с JWT не актуально
            .csrf(AbstractHttpConfigurer::disable)

            // todo sessionManagement - настройка сессий
            .sessionManagement(
                (session) ->
                    // todo сессия будет создаваться всегда независимо от того, требуется ли она для аутентификации.
                    session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
            )
            // todo ???
            .authorizeHttpRequests(
                (authorize) -> authorize
                    // todo requestMatchers - набор шаблонов URL-путей с определённым правилам безопасности.
                    .requestMatchers(HttpMethod.GET, "/","/public").permitAll() // Доступно всем
                    // .requestMatchers("/admin/**").hasRole("ADMIN") // Только для администраторов
                    .requestMatchers("/authenticated", "/success").authenticated()

                    // todo anyRequest - является "заглушкой" для всех запросов, которые не были явно настроены ранее
                    .anyRequest().permitAll() // permitAll()- Все остальные запросы доступны всем
                   //.anyRequest().authenticated() // authenticated() - Все остальные запросы требуют аутентификации
            )
            // todo Настройка анонимного пользователя.
            //  В данном случае анонимному пользователю назначается объект AnonimusUD как principal и роль ROLE_ANONYMOUS.
            .anonymous(a -> a.principal(new AnonimusUD()).authorities("ROLE_ANONYMOUS"))

            // todo добавления своего фильтра после AuthorizationFilter
            .addFilterAfter(new MyOwnFilter(), AuthorizationFilter.class)

            // todo HTTP Basic Authentication - выпадающее окошко с логином и паролем
            // .httpBasic(Customizer.withDefaults())

            // todo настройка формы логина
             //.formLogin(Customizer.withDefaults()) // простое использование
            .formLogin(
                fm ->
                    fm
                        //.loginPage("/extlogin") // опционально URL страницы с логином, если нужно
                        //.usernameParameter("extuser") // опционально имя параметра в форме входа, который соответствует имени пользователя
                        //.passwordParameter("extpass") // опционально имя параметра в форме входа, который соответствует паролю
                        //.loginProcessingUrl("/extlogin_process") // опционально URL, на который отправляются данные формы входа для обработки Spring Security. Spring Security автоматически обрабатывает запросы на этот URL, проверяет учетные данные и выполняет аутентификацию.
                        .defaultSuccessUrl("/success", true) // при удачной аутентификации, true чтобы редирект был
                        .failureForwardUrl("/fail") // при не удачной аутентификации
            )
            // todo функцию "Запомнить меня".
            //  В данном случае задается ключ (AnyKey) и время жизни токена в секундах (600 секунд, т.е. 10 минут).
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
