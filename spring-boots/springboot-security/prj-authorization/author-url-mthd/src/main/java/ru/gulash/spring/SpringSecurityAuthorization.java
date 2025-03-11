package ru.gulash.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringSecurityAuthorization {

    public static void main(String[] args) {
        SpringApplication.run(SpringSecurityAuthorization.class);
        System.out.println(
            """
				Чтобы перейти на страницу сайта открыть:
				-> http://localhost:9010/
				
				Пароли смотрим в ru/gulash/spring/security/SecurityConfigurationUrlRoles.java
				"""
        );
    }
}
