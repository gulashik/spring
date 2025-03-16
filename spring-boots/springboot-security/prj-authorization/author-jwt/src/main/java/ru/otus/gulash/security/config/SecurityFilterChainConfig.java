package ru.otus.gulash.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@EnableMethodSecurity
@Configuration
public class SecurityFilterChainConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(AbstractHttpConfigurer::disable)
            // todo basic авторизация для получения jwt
            .httpBasic(Customizer.withDefaults())

            // todo фильтр по URL-ам
            .authorizeHttpRequests((authorize) ->
                authorize
                    .requestMatchers(HttpMethod.GET, "/token").hasRole("APP")
                    .requestMatchers("/userinfo").hasAnyAuthority("ROLE_JWT_ADMIN", "SCOPE_ROLE_APP")
                    .anyRequest().authenticated()
            )

            // todo OAuth2 Resource Server для работы с JWT. В данном случае, используется JWT для аутентификации.
            //.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults())) // todo можно использовать default потом парсить jwt
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt ->
                    jwt.jwtAuthenticationConverter(
                        /*todo кастомный конвертер*/new SecurityJwtConfig().jwtAuthenticationConverter()
                    )
                )
            )
            .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // todo используются кастомные обработчики
            //  для ошибок аутентификации (BearerTokenAuthenticationEntryPoint) и
            //  доступа (BearerTokenAccessDeniedHandler).
            .exceptionHandling((exceptions) -> exceptions
                .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                .accessDeniedHandler(new BearerTokenAccessDeniedHandler())
            );

        return http.build();
    }
}