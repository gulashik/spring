package ru.gulash.example.shelldemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.shell.command.annotation.CommandScan;

@SpringBootApplication
public class ShellDemoApplication {
	public static void main(String[] args) {
		SpringApplication.run(ShellDemoApplication.class, args);
	}
}
