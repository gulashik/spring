package org.gualsh.demo.webclient.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO для ответа API с пагинацией.
 *
 * @param <T> тип данных в списке
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponseDto<T> implements Serializable {

    /**
     * Список данных на текущей странице.
     */
    private List<T> data;

    /**
     * Общее количество элементов.
     */
    private Long total;

    /**
     * Номер текущей страницы (начиная с 0).
     */
    private Integer page;

    /**
     * Размер страницы.
     */
    private Integer size;

    /**
     * Общее количество страниц.
     */
    private Integer totalPages;

    /**
     * Есть ли следующая страница.
     */
    private Boolean hasNext;

    /**
     * Есть ли предыдущая страница.
     */
    private Boolean hasPrevious;

    /**
     * Время создания ответа.
     */
    private LocalDateTime timestamp;
}
