package ru.gulash.spring.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

// todo другие аннотации
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@Configuration
public class SecurityConfiguration {

    // todo bean другой
    @Bean
    public SecurityWebFilterChain springWebFilterChain( ServerHttpSecurity http ) {
        // todo не много другое название методов
        return http
                .authorizeExchange((exchanges)->exchanges
                        .pathMatchers( HttpMethod.GET, "/authenticated.html" ).authenticated()
                        .pathMatchers( "/person" ).hasAnyRole( "USER" )
                        .anyExchange().authenticated()
                )
                .formLogin( Customizer.withDefaults())
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    // todo другой bean
    @Bean
    public ReactiveUserDetailsService userDetailsService() {
        UserDetails user = User
                .withUsername( "user" )
                .password( "user" )
                .roles( "USER" )
                .build();
        return new MapReactiveUserDetailsService( user );
    }
}
