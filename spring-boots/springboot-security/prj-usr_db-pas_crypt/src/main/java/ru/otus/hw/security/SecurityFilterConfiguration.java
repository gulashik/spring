package ru.otus.hw.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AnonymousConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import ru.otus.hw.security.model.AnonymousUserDetails;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityFilterConfiguration {

    private final UserDetailsManager userDetailsManager;

    // todo Нужно реализовать SecurityFilterChain bean
    // todo SecurityFilterChain(часть механизма фильтров Servlet API) — это цепочка фильтров, которая управляет
    //  процессом обработки всех HTTP-запросов и их защитой (Аутентификация, Авторизация, Обработка запросов/ответов).
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // todo org.springframework.security.web.FilterChainProxy.doFilter - можно подебажить
        return http
            // todo Отключение CSRF-защиты. Для RestAPI с JWT не актуально
            .csrf(
                AbstractHttpConfigurer::disable
            )
            // todo sessionManagement - настройка сессий
            .sessionManagement(
                (SessionManagementConfigurer<HttpSecurity> session) ->
                    // todo сессия будет создаваться всегда независимо от того, требуется ли она для аутентификации.
                    session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
            )
            .authorizeHttpRequests(
                (authorize) -> authorize
                    // todo requestMatchers - набор шаблонов URL-путей с определённым правилам безопасности.
                    .requestMatchers("/h2-console/**").permitAll() // Доступно всем
                    // .requestMatchers(HttpMethod.XXX, "/xxx/**").hasRole("XXX")
                    // .requestMatchers("/authenticated", "/success").authenticated()

                    // todo anyRequest - является "заглушкой" для всех запросов, которые не были явно настроены ранее
                    .anyRequest().authenticated() // Все остальные запросы требуют аутентификации
                // .anyRequest().permitAll() // permitAll()- Все остальные запросы доступны всем
            )
            .headers(
                headers -> headers
                    // Разрешаем использование iframe для консоли H2
                    .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
            )
            // todo Настройка анонимного пользователя - включена по умолчанию
            //  В данном случае анонимному пользователю назначается объект AnonimusUD как principal и роль ROLE_ANONYMOUS.
            //  при отключении анонимного пользователя Authentication == null, т.е. securityContext.getAuthentication() == null
            //.anonymous(AbstractHttpConfigurer::disable) // отключение анонимного пользователя - но не надо
            .anonymous(
                (AnonymousConfigurer<HttpSecurity> anonConfig) ->
                    anonConfig
                        .principal(new AnonymousUserDetails())
                        .authorities("ROLE_ANONYMOUS")
                    /* .principal("guest") // Имя principal для анонимного пользователя
                    .authorities("ROLE_GUEST") // Роли для анонимного пользователя
                    */
            )
            // todo настройка формы логина
            // простое использование
            .formLogin(
                Customizer.withDefaults()
            )
            /*.formLogin(
                fm ->
                    fm
                        //.loginPage("/extlogin") // опционально URL страницы с логином, если нужно
                        //.usernameParameter("extuser") // опционально имя параметра в форме входа, который соответствует имени пользователя
                        //.passwordParameter("extpass") // опционально имя параметра в форме входа, который соответствует паролю
                        //.loginProcessingUrl("/extlogin_process") // опционально URL, на который отправляются данные формы входа для обработки Spring Security. Spring Security автоматически обрабатывает запросы на этот URL, проверяет учетные данные и выполняет аутентификацию.
                        .defaultSuccessUrl("/success", true) // при удачной аутентификации, true чтобы редирект был
                        .failureForwardUrl("/fail") // при не удачной аутентификации
            )*/
            // todo функцию "Запомнить меня".
            //  В данном случае задается ключ (AnyKey) и время жизни токена в секундах. ключ используется для создания токена в cookies
            .rememberMe(rm -> rm.key("AnyKey") // Секретный ключ для HMAC при создании подписи токена
                .tokenValiditySeconds(600)
            )
            .logout(
                (logout) ->
                    logout
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
            )
            // todo UserDetailsManager - реализация UserDetailsService с нужным источником(сейчас БД)
            .userDetailsService(
                userDetailsManager
            )
            .build();
    }
}
