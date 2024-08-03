package com.sprboot.mapstractexample.springmapstractgradle;

import com.sprboot.mapstractexample.springmapstractgradle.dto.UserDto;
import com.sprboot.mapstractexample.springmapstractgradle.entity.UserEntity;
import com.sprboot.mapstractexample.springmapstractgradle.info.Country;
import com.sprboot.mapstractexample.springmapstractgradle.mapstruct.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDate;
import java.time.LocalDateTime;

@SpringBootApplication
public class SpringMapstractGradleApplication implements ApplicationRunner {

    public static void main(String[] args) {
        SpringApplication.run(SpringMapstractGradleApplication.class, args);
    }

    // todo привязываем mapper по интерфейсу
    @Autowired
    UserMapper userMapper;

    @Override
    public void run(ApplicationArguments args) {
        UserEntity manualEntity = new UserEntity(
            1L,
            "fname",
            "lname",
            "email@m.ru",
            "RU",
            "desc",
            LocalDateTime.now(),
            LocalDate.of(2010, 1, 1),
            null,
            null
        );
        UserDto manualDto = new UserDto(
            1L,
            "fname",
            "lname",
            "email@m.ru",
            new Country("RU","Russia","Евразия"),
            "desc",
            LocalDateTime.now(),
            150,
            null,
            null
            );

        // todo маппим
        UserEntity resultEntity = userMapper.toEntity(manualDto);
        System.out.println(resultEntity);

        UserDto resultDto = userMapper.toDto(manualEntity);
        System.out.println(resultDto);
    }
}
