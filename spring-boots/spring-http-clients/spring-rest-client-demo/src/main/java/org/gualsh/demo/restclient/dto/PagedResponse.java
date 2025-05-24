package org.gualsh.demo.restclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * DTO для представления страничного ответа.
 * Демонстрирует работу с коллекциями в ParameterizedTypeReference.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PagedResponse<T> implements Serializable {

    /**
     * Список элементов на текущей странице.
     */
    private List<T> content;

    /**
     * Номер текущей страницы (начиная с 0).
     */
    private Integer page;

    /**
     * Размер страницы.
     */
    private Integer size;

    /**
     * Общее количество элементов.
     */
    @JsonProperty("total_elements")
    private Long totalElements;

    /**
     * Общее количество страниц.
     */
    @JsonProperty("total_pages")
    private Integer totalPages;

    /**
     * Является ли текущая страница первой.
     */
    private Boolean first;

    /**
     * Является ли текущая страница последней.
     */
    private Boolean last;
}
