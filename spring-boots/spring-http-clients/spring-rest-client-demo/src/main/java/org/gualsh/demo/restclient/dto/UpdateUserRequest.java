package org.gualsh.demo.restclient.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO для обновления существующего пользователя.
 * Поля могут быть null для частичного обновления (PATCH).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest implements Serializable {

    @Size(max = 100, message = "Имя не может превышать 100 символов")
    private String name;

    @Size(max = 50, message = "Username не может превышать 50 символов")
    private String username;

    @Email(message = "Некорректный формат email")
    private String email;

    private String phone;
    private String website;
}