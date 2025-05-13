package org.gualsh.demo.spdrest.projection;

import org.gualsh.demo.spdrest.model.Author;
import org.gualsh.demo.spdrest.model.Book;
import org.springframework.data.rest.core.config.Projection;

import java.time.LocalDate;
import java.util.Set;

/**
 * Проекция для сущности Author.
 *
 * Проекции позволяют определить подмножество полей, которые будут возвращены в ответе.
 * Это помогает оптимизировать API и предоставлять только необходимые данные.
 */
@Projection(name = "authorWithBooks", types = { Author.class })
public interface AuthorWithBooks {
    Long getId();
    String getFirstName();
    String getLastName();
    LocalDate getBirthDate();
    String getBiography();

    /**
     * Пользовательский метод, не связанный напрямую с полем сущности.
     * Возвращает полное имя автора.
     *
     * @return полное имя автора
     */
    default String getFullName() {
        return getFirstName() + " " + getLastName();
    }

    /**
     * Пользовательский метод, не связанный напрямую с полем сущности.
     * Вычисляет возраст автора.
     *
     * @return возраст автора или null, если дата рождения не указана
     */
    default Integer getAge() {
        if (getBirthDate() == null) {
            return null;
        }
        return LocalDate.now().getYear() - getBirthDate().getYear();
    }

    /**
     * Проекция для книг внутри автора.
     *
     * @return сокращенное представление книг
     */
    Set<BookSummary> getBooks();

    /**
     * Внутренняя проекция для книги.
     */
    @Projection(types = { Book.class })
    interface BookSummary {
        Long getId();
        String getTitle();
        String getIsbn();
        LocalDate getPublicationDate();
    }
}