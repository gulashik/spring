package org.gualsh.demo.spdrest.projection;

import org.gualsh.demo.spdrest.model.Author;
import org.gualsh.demo.spdrest.model.Book;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import java.time.LocalDate;
import java.util.Set;

/**
 * Проекция для сущности Author.
 *
 * Проекции позволяют определить подмножество полей, которые будут возвращены в ответе.
 * Это помогает оптимизировать API и предоставлять только необходимые данные.
 */
// todo Проекция либо находится в пакете/подпакете с сущностью либо регистриется через конфиг см. SpringDataRestConfig.java
@Projection(
    /*
        Имеет имя, которое используется для её выбора в запросах
        например GET http://localhost:8080/api/authors/1?projection=authorWithBooks
    */
    name = "authorWithBooks",
    /*
        Привязывается к конкретным типам сущностей один или больше
    */
    types = { Author.class }
)
public interface AuthorWithBooks {
    Long getId();
    String getFirstName();
    String getLastName();
    LocalDate getBirthDate();
    String getBiography();

    /**
     * Пользовательский метод, не связанный напрямую с полем сущности.<p>
     * Возвращает полное имя автора.
     *
     * @return полное имя автора
     */
    default String getFullName() {
        return getFirstName() + " " + getLastName();
    }

    // Вычисляемое свойство через SpEL-выражения
    @Value("#{target.firstName + ' ' + target.lastName}")
    String getFullNameSpEl();
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