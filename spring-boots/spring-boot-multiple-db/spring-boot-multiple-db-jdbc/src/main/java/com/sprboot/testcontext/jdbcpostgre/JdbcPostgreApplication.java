package com.sprboot.testcontext.jdbcpostgre;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class JdbcPostgreApplication {

	public static void main(String[] args) {
		SpringApplication.run(JdbcPostgreApplication.class, args);
	}
}
