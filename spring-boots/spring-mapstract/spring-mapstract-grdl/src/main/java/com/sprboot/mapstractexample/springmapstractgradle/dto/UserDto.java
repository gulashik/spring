package com.sprboot.mapstractexample.springmapstractgradle.dto;

import com.sprboot.mapstractexample.springmapstractgradle.info.Country;
import lombok.*;

import java.time.LocalDateTime;

// todo сущности для маппинга
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String fName;
    private String lastName;
    private String email;
    private Country country;
    private String description;
    private LocalDateTime createdAt;
    private Integer age;
    private String code;
    private String phoneNumber;

}