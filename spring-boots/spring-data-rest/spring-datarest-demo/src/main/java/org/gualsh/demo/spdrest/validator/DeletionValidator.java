package org.gualsh.demo.spdrest.validator;

import org.gualsh.demo.spdrest.model.Author;
import org.gualsh.demo.spdrest.model.Category;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Валидатор для проверки возможности удаления сущностей.
 *
 * Проверяет, можно ли безопасно удалить сущность, не нарушая ссылочную целостность.
 * Например, запрещает удаление авторов с книгами или категорий, содержащих книги.
 */
public class DeletionValidator implements Validator {

    /**
     * Определяет, поддерживает ли валидатор указанный класс.
     * Поддерживает валидацию авторов и категорий.
     *
     * @param clazz класс для проверки
     * @return true, если валидатор поддерживает класс
     */
    @Override
    public boolean supports(Class<?> clazz) {
        return Author.class.isAssignableFrom(clazz) ||
            Category.class.isAssignableFrom(clazz);
    }

    /**
     * Выполняет валидацию перед удалением объекта.
     * Проверяет, не нарушит ли удаление ссылочную целостность.
     *
     * @param target объект для валидации
     * @param errors контейнер для ошибок валидации
     */
    @Override
    public void validate(Object target, Errors errors) {
        if (target instanceof Author) {
            Author author = (Author) target;
            if (author.getBooks() != null && !author.getBooks().isEmpty()) {
                errors.reject("author.has.books",
                    "Невозможно удалить автора, у которого есть связанные книги");
            }
        } else if (target instanceof Category) {
            Category category = (Category) target;
            if (category.getBooks() != null && !category.getBooks().isEmpty()) {
                errors.reject("category.has.books",
                    "Невозможно удалить категорию, в которой есть книги");
            }
        }
    }
}