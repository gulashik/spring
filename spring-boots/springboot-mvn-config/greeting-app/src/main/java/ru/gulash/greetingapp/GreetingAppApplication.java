package ru.gulash.greetingapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan // todo сканируем пропы
@SpringBootApplication // todo над главным классом
public class GreetingAppApplication {
	public static void main(String[] args) {
		SpringApplication.run(GreetingAppApplication.class, args);
	}
}
