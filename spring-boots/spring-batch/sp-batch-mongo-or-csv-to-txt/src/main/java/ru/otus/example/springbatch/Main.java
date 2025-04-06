package ru.otus.example.springbatch;

import com.github.cloudyrock.spring.v5.EnableMongock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableMongock
@SpringBootApplication
public class Main {
    // Опция запуска если нужно авто запуск через Program Parameters
    // --spring.shell.interactive.enabled=false --spring.batch.job.enabled=true inputFileName=entries.csv outputFileName=output_new.dat

    // Cозданы два конфига запуска со SpringShell и Автозапуск Job-ов
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}


