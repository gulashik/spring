package ru.gulash.spring.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.ArrayList;
import java.util.List;

// Может чего доописать?
@Configuration // todo нужна
@EnableWebSecurity // todo аннотация для SecurityFilterChain bean
@EnableMethodSecurity // todo Включает поддержку всех аннотаций (@PreAuthorize, @PostAuthorize, @Secured, @RolesAllowed)
public class SecurityConfigurationUrlRoles {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests((authorize) -> authorize
                .requestMatchers("/public", "/").permitAll()
                //.requestMatchers( "/authenticated", "/success" ).authenticated()

                // todo наличие ролей для доступа по URL
                .requestMatchers("/manager").hasAnyRole("MANAGER")
                .requestMatchers("/user").hasAnyRole("USER") // todo без префикса ROLE_ = Role
                //.requestMatchers("/user").hasAnyAuthority("ROLE_USER") // todo с префиксом ROLE_ = Authority
                .requestMatchers( "/admin" ).access(new AuthorizationService().hasAuthorizationGrant("ADMIN")) // todo можно сделать кастомную проверку
                //.requestMatchers( "/admin" ).hasAnyRole( "ADMIN" )

                .anyRequest().authenticated()
            )
            .formLogin(Customizer.withDefaults())
        ;
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        var users = new ArrayList<UserDetails>();
        users.add(User
            .withUsername("admin").password("admin").roles("ADMIN")
            .build());
        users.add(User
            .withUsername("user").password("user").roles("USER")
            .build());
        return new InMemoryUserDetailsManager(users);

    }
}
