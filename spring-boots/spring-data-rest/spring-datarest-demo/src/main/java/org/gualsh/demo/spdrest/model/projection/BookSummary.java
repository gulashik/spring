package org.gualsh.demo.spdrest.model.projection;

import org.gualsh.demo.spdrest.model.Author;
import org.gualsh.demo.spdrest.model.Book;
import org.gualsh.demo.spdrest.model.Category;
import org.springframework.data.rest.core.config.Projection;

import java.time.LocalDate;
import java.util.Set;

/**
 * Проекция для сущности Book.
 *
 * Проекции позволяют определить подмножество полей, которые будут возвращены в ответе.
 * Это помогает оптимизировать API и предоставлять только необходимые данные.
 */
@Projection(name = "bookSummary", types = { Book.class })
public interface BookSummary {
    Long getId();
    String getTitle();
    String getIsbn();
    LocalDate getPublicationDate();
    Integer getPageCount();
    String getLanguage();
    String getPublisher();

    /**
     * Проекция для категории внутри книги.
     *
     * @return сокращенное представление категории
     */
    CategorySummary getCategory();

    /**
     * Проекция для авторов внутри книги.
     *
     * @return сокращенное представление авторов
     */
    Set<AuthorSummary> getAuthors();

    /**
     * Внутренняя проекция для категории.
     */
    @Projection(types = { Category.class })
    interface CategorySummary {
        Long getId();
        String getName();
    }

    /**
     * Внутренняя проекция для автора.
     */
    @Projection(types = { Author.class })
    interface AuthorSummary {
        Long getId();
        String getFirstName();
        String getLastName();

        /**
         * Пользовательский метод, не связанный напрямую с полем сущности.
         * Возвращает полное имя автора.
         *
         * @return полное имя автора
         */
        default String getFullName() {
            return getFirstName() + " " + getLastName();
        }
    }
}