package ru.gulash.spring.security;

import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AnonymousConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import ru.gulash.spring.security.roleplay.AuthorizationService;

import java.util.ArrayList;

@Configuration // todo нужна т.к. @Configuration не во всех версиях @EnableWebSecurity и @EnableMethodSecurity есть
@EnableWebSecurity // todo аннотация для SecurityFilterChain bean
// todo Включает поддержку аннотаций
@EnableMethodSecurity(
     prePostEnabled = true/* todo default Включает поддержку @PreAuthorize, @PostAuthorize, @PreFilter и @PostFilter */
    ,securedEnabled = true /* todo Включает поддержку @Secured */
    ,mode = AdviceMode.PROXY /* todo default Стандартные Spring-прокси (например, для Spring-бинов) */
)
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
                //.requestMatchers( "/admin" ).access(new AuthorizationService().hasAuthorizationGrant("ADMIN")) // todo можно сделать кастомную проверку
                .requestMatchers( "/admin" ).hasAnyRole( "ADMIN" )

                .anyRequest().authenticated()
                //.anyRequest().denyAll()
            )
            .anonymous( // перенастройка анонимного пользователя если нужно
                (AnonymousConfigurer<HttpSecurity> anonConfig) ->
                    anonConfig
                     .principal("guest") // Имя principal для анонимного пользователя
                    .authorities("ROLE_GUEST") // Роли для анонимного пользователя
            )
            .formLogin(Customizer.withDefaults())
        ;
        return http.build();
    }

    // todo Указываем нужную иерархию
    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy("ROLE_ADMIN > ROLE_MANAGER > ROLE_USER");
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
        users.add(User
            .withUsername("manager").password("manager").roles("MANAGER")
            .build());
        return new InMemoryUserDetailsManager(users);
    }
}
