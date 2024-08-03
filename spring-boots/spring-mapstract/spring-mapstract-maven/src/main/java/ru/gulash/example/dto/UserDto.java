package ru.gulash.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.gulash.example.info.Country;

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