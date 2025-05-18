package org.gualsh.demo.micromet;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // Включаем планирование для демонстрационных задач
public class MicrometerDemoApplication {

    // todo Смотрим README.md
    public static void main(String[] args) {
        SpringApplication.run(MicrometerDemoApplication.class, args);
    }
}