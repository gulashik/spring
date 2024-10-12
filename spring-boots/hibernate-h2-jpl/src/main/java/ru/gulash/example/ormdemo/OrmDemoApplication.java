package ru.gulash.example.ormdemo;

import org.h2.tools.Console;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.sql.SQLException;

@SpringBootApplication
public class OrmDemoApplication {

	public static void main(String[] args) throws SQLException {
		ConfigurableApplicationContext context = SpringApplication.run(OrmDemoApplication.class, args);
		// todo указываем url как в application.yml jdbc:h2:mem:testdb
		// Console.main(args); // запустит браузер и там можно будет увидеть наши таблицы
		//Console.main();
	}
}
