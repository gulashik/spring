package ru.otus.hw.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;

import javax.sql.DataSource;

/*todo конфиг */
@Configuration
public class SecurityConfiguration {

    // todo PasswordEncoder отвечает за шифрование (хеширование) паролей перед их сохранением в базе данных
    //      и за проверку совпадения пароля при аутентификации пользователя.
    // https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/storage.html
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    /*todo UserDetailsManager - удобная штатная имплементация UserDetailsService */
    @Bean
    public UserDetailsManager userDetailsManager(DataSource dataSource) {
        return new JdbcUserDetailsManager(dataSource);
    }
}
