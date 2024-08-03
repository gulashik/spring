package com.sprboot.annotation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AnnotationExampleApplication {
    public static void main(String[] args) {
        var context = SpringApplication.run(AnnotationExampleApplication.class, args);

        EventPublisher eventPublisher = context.getBean("eventPublisher", EventPublisher.class);

        eventPublisher.publishEvent();
    }
}
