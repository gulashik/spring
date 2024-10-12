package ru.otus.multipledbdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MultipleDbDemoApplication {

	public static void main(String[] args) {
		// todo Переходим в http://localhost:8080/
		// видим у нас 2 БД
		SpringApplication.run(MultipleDbDemoApplication.class, args);
	}

}
