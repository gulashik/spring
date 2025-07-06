package org.gualsh.demo.httpe.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 * Ответ от ReqRes API со списком пользователей.
 *
 * <h4>Образовательный момент</h4>
 * <p>
 * Wrapper класс для paginated ответов.
 * Такой подход типичен для REST API.
 * </p>
 */
@Data
@Builder
public class ReqResListResponse<T> {
    private int page;

    @JsonProperty("per_page")
    private int perPage;

    private int total;

    @JsonProperty("total_pages")
    private int totalPages;

    private java.util.List<T> data;

    private Support support;

    @Data
    @Builder
    public static class Support {
        private String url;
        private String text;
    }
}

