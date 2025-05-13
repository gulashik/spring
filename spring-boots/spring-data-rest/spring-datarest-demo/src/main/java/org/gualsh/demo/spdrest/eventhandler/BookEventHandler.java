package org.gualsh.demo.spdrest.eventhandler;

import org.gualsh.demo.spdrest.model.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

/**
 * Обработчик событий для сущностей Spring Data REST.
 *
 * RepositoryEventHandler позволяет перехватывать события, связанные с репозиториями,
 * такие как создание, обновление и удаление сущностей.
 */
@Component
@RepositoryEventHandler
public class BookEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(BookEventHandler.class);

    /**
     * Обрабатывает событие перед созданием книги.
     * Можно добавить логику валидации или предварительной обработки данных.
     *
     * @param book сущность книги, которая будет создана
     */
    @HandleBeforeCreate
    public void handleBookBeforeCreate(Book book) {
        logger.info("Создание новой книги: {}", book.getTitle());

        // Пример предварительной обработки: приведение ISBN к стандартному формату
        if (book.getIsbn() != null) {
            book.setIsbn(standardizeIsbn(book.getIsbn()));
        }
    }

    /**
     * Обрабатывает событие перед сохранением (обновлением) книги.
     * Можно добавить логику валидации или предварительной обработки данных.
     *
     * @param book сущность книги, которая будет обновлена
     */
    @HandleBeforeSave
    public void handleBookBeforeSave(Book book) {
        logger.info("Обновление книги: {}", book.getTitle());

        // Пример предварительной обработки: приведение ISBN к стандартному формату
        if (book.getIsbn() != null) {
            book.setIsbn(standardizeIsbn(book.getIsbn()));
        }
    }

    /**
     * Приводит ISBN к стандартному формату, удаляя все символы, кроме цифр и дефисов.
     *
     * @param isbn исходный ISBN
     * @return стандартизированный ISBN
     */
    private String standardizeIsbn(String isbn) {
        // Удаляем все символы, кроме цифр, дефисов и букв X/x (используются в ISBN)
        return isbn.replaceAll("[^0-9X\\-x]", "").toUpperCase();
    }
}