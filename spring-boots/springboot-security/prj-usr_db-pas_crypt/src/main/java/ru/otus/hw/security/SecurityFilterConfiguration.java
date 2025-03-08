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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(
                AbstractHttpConfigurer::disable
            )
            .sessionManagement(
                (SessionManagementConfigurer<HttpSecurity> session) ->
                    session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
            )
            .authorizeHttpRequests(
                (authorize) -> authorize
                    .requestMatchers("/h2-console/**").permitAll()
                    // .requestMatchers(HttpMethod.XXX, "/xxx/**").hasRole("XXX")
                    .anyRequest().authenticated()
            )
            .headers(
                headers -> headers
                    // Разрешаем использование iframe для консоли H2
                    .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
            )
            .anonymous(
                (AnonymousConfigurer<HttpSecurity> anonConfig) ->
                    anonConfig
                        .principal(new AnonymousUserDetails())
                        .authorities("ROLE_ANONYMOUS")
            )
            .formLogin(
                Customizer.withDefaults()
            )
            .logout(
                (logout) ->
                    logout
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
            )
            .userDetailsService(
                userDetailsManager
            )
            .build();
    }
}
