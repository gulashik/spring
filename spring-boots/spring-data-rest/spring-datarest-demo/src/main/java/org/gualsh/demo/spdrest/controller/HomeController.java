package org.gualsh.demo.spdrest.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * Основной контроллер приложения.
 *
 * Обрабатывает основные URL-пути, не связанные с REST API.
 */
@Controller
public class HomeController {

    @Value("${spring.data.rest.base-path}")
    private String apiBasePath;

    /**
     * Главная страница приложения.
     * Доступно по URL: /
     *
     * @return JSON с информацией о приложении и ссылками на API
     */
    @GetMapping("/")
    @ResponseBody
    public Map<String, Object> home() {
        Map<String, Object> response = new HashMap<>();

        response.put("application", "Spring Data REST Demo");
        response.put("description", "Демонстрационное приложение для Spring Data REST");

        // Ссылки на основные ресурсы API
        Map<String, String> apiLinks = new HashMap<>();
        apiLinks.put("books", apiBasePath + "/books");
        apiLinks.put("authors", apiBasePath + "/authors");
        apiLinks.put("categories", apiBasePath + "/categories");
        apiLinks.put("bookSummaryProjection", apiBasePath + "/books?projection=bookSummary");
        apiLinks.put("authorWithBooksProjection", apiBasePath + "/authors?projection=authorWithBooks");
        apiLinks.put("booksByAuthorId", apiBasePath + "/books/search/byAuthorId?authorId=1");
        apiLinks.put("booksByCategoryName", apiBasePath + "/books/search/byCategoryName?categoryName=Programming");
        apiLinks.put("bookStatsByLanguage", apiBasePath + "/books/stats/by-language");

        response.put("apiLinks", apiLinks);

        // Дополнительная информация
        Map<String, String> additionalInfo = new HashMap<>();
        additionalInfo.put("swagger", "/swagger-ui.html");
        additionalInfo.put("h2Console", "/h2-console");

        response.put("additionalInfo", additionalInfo);

        return response;
    }
}