package com.sprboot.mapstractexample.springmapstractgradle.entity;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

// todo сущности для маппинга
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String countryCode;
    private String description;
    private LocalDateTime createdAt;
    private LocalDate birthday;
    private String code;
    private String phoneNumber;

}