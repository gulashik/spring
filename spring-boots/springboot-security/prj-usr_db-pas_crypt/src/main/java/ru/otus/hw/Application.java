package ru.otus.hw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
		System.out.println(
			"""
				Чтобы перейти на страницу сайта открыть:
				-> http://localhost:8080
				
				Чтобы перейти в H2-condole открыть
				-> http://localhost:8080/h2-console
					url=jdbc:h2:mem:maindb
					user=sa password=<empty>
					роли-SELECT * FROM AUTHORITIES
					пользователи-SELECT * FROM USERS
				"""
		);
	}
}
