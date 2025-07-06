package org.gualsh.demo.httpe.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import lombok.Builder;
import lombok.Data;

/**
 * Модель пользователя из ReqRes API.
 *
 * <h4>Образовательный момент</h4>
 * <p>
 * Показывает работу с разными API структурами
 * и использование Jackson аннотаций для mapping.
 * </p>
 */
@Data
@Builder
public class ReqResUser {
    private Long id;

    @Email
    private String email;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    private String avatar;

    /**
     * Получает полное имя пользователя.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * Computed properties в DTO помогают
     * инкапсулировать бизнес-логику.
     * </p>
     *
     * @return полное имя
     */
    public String getFullName() {
        return (firstName != null ? firstName : "") +
            " " +
            (lastName != null ? lastName : "");
    }
}
