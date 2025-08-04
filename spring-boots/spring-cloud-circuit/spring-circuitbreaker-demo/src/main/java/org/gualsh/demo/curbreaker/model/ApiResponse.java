package org.gualsh.demo.curbreaker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Модель ответа от внешнего API.
 *
 * <h3>Образовательный момент:</h3>
 * <p>
 * Данная модель демонстрирует правильное создание DTO для работы с внешними API.
 * Использование @JsonIgnoreProperties(ignoreUnknown = true)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
 public class ApiResponse {

 /**
 * Уникальный идентификатор записи.
 *
 * <p><strong>Образовательный момент:</strong></p>
 * <p>
 * Использование Long вместо long позволяет обрабатывать null значения,
 * что важно при работе с внешними API, которые могут возвращать неполные данные.
 * </p>
 */
private Long id;

/**
 * Заголовок записи.
 */
private String title;

/**
 * Содержимое записи.
 */
private String body;

/**
 * Идентификатор пользователя, создавшего запись.
 */
private Long userId;
}
