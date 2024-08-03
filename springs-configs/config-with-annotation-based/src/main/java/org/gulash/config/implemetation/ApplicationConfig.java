package org.gulash.config.implemetation;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

// todo загружаем файл .properties
@PropertySource("classpath:application.properties")
@Configuration
public class ApplicationConfig {
}