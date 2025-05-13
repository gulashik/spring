package org.gualsh.demo.spdrest.validator;

import org.gualsh.demo.spdrest.model.Book;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Валидатор для сущности Book.
 *
 * Реализует интерфейс Validator из Spring Framework для проверки
 * корректности данных книги перед сохранением в базу данных.
 */
public class BookValidator implements Validator {

    /**
     * Определяет, поддерживает ли валидатор указанный класс.
     *
     * @param clazz класс для проверки
     * @return true, если валидатор поддерживает класс Book
     */
    @Override
    public boolean supports(Class<?> clazz) {
        return Book.class.isAssignableFrom(clazz);
    }

    /**
     * Выполняет валидацию объекта Book.
     * Применяет бизнес-правила валидации, которые не могут быть выражены через аннотации.
     *
     * @param target объект для валидации
     * @param errors контейнер для ошибок валидации
     */
    @Override
    public void validate(Object target, Errors errors) {
        Book book = (Book) target;

        // Проверка ISBN (если указан)
        if (book.getIsbn() != null && !book.getIsbn().isEmpty()) {
            if (!isValidIsbn(book.getIsbn())) {
                errors.rejectValue("isbn", "invalid.isbn",
                    "ISBN должен быть в формате ISBN-10 или ISBN-13");
            }
        }

        // Проверка количества страниц
        if (book.getPageCount() != null) {
            if (book.getPageCount() < 1) {
                errors.rejectValue("pageCount", "negative.pageCount",
                    "Количество страниц должно быть положительным числом");
            }

            // Дополнительная валидация для разных категорий книг
            if (book.getCategory() != null) {
                String categoryName = book.getCategory().getName();

                // Романы должны иметь минимум 50 страниц
                if ("Fiction".equalsIgnoreCase(categoryName) && book.getPageCount() < 50) {
                    errors.rejectValue("pageCount", "too.small.for.fiction",
                        "Художественная книга должна содержать минимум 50 страниц");
                }

                // Технические книги должны иметь минимум 100 страниц
                if ("Programming".equalsIgnoreCase(categoryName) && book.getPageCount() < 100) {
                    errors.rejectValue("pageCount", "too.small.for.programming",
                        "Книга по программированию должна содержать минимум 100 страниц");
                }
            }
        }

        // Проверка наличия авторов
        if (book.getAuthors() == null || book.getAuthors().isEmpty()) {
            errors.rejectValue("authors", "empty.authors",
                "Книга должна иметь хотя бы одного автора");
        }

        // Проверка даты публикации
        if (book.getPublicationDate() != null) {
            if (book.getPublicationDate().isAfter(java.time.LocalDate.now())) {
                errors.rejectValue("publicationDate", "future.publicationDate",
                    "Дата публикации не может быть в будущем");
            }
        }
    }

    /**
     * Проверяет, является ли строка корректным ISBN.
     * Упрощенная проверка для демонстрации.
     *
     * @param isbn строка ISBN для проверки
     * @return true, если ISBN корректен
     */
    private boolean isValidIsbn(String isbn) {
        // Удаляем все символы, кроме цифр и 'X'
        String cleanIsbn = isbn.replaceAll("[^0-9X]", "");

        // ISBN-10 или ISBN-13
        return (cleanIsbn.length() == 10 || cleanIsbn.length() == 13);
    }
}