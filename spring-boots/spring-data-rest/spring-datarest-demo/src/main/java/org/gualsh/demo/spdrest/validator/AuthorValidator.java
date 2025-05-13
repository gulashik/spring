package org.gualsh.demo.spdrest.validator;

import org.gualsh.demo.spdrest.model.Author;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalDate;

/**
 * Валидатор для сущности Author.
 *
 * Реализует интерфейс Validator из Spring Framework для проверки
 * корректности данных автора перед сохранением в базу данных.
 */
public class AuthorValidator implements Validator {

    /**
     * Определяет, поддерживает ли валидатор указанный класс.
     *
     * @param clazz класс для проверки
     * @return true, если валидатор поддерживает класс Author
     */
    @Override
    public boolean supports(Class<?> clazz) {
        return Author.class.isAssignableFrom(clazz);
    }

    /**
     * Выполняет валидацию объекта Author.
     * Применяет бизнес-правила валидации, которые не могут быть выражены через аннотации.
     *
     * @param target объект для валидации
     * @param errors контейнер для ошибок валидации
     */
    @Override
    public void validate(Object target, Errors errors) {
        Author author = (Author) target;

        // Проверка имени автора
        if (author.getFirstName() != null) {
            if (author.getFirstName().length() < 2) {
                errors.rejectValue("firstName", "too.short.firstName",
                    "Имя автора должно содержать минимум 2 символа");
            }

            if (!author.getFirstName().matches("[\\p{L}\\s\\-']+")) {
                errors.rejectValue("firstName", "invalid.firstName",
                    "Имя автора должно содержать только буквы, пробелы, дефисы или апострофы");
            }
        }

        // Проверка фамилии автора
        if (author.getLastName() != null) {
            if (author.getLastName().length() < 2) {
                errors.rejectValue("lastName", "too.short.lastName",
                    "Фамилия автора должна содержать минимум 2 символа");
            }

            if (!author.getLastName().matches("[\\p{L}\\s\\-']+")) {
                errors.rejectValue("lastName", "invalid.lastName",
                    "Фамилия автора должна содержать только буквы, пробелы, дефисы или апострофы");
            }
        }

        // Проверка даты рождения
        if (author.getBirthDate() != null) {
            // Дата рождения не может быть в будущем
            if (author.getBirthDate().isAfter(LocalDate.now())) {
                errors.rejectValue("birthDate", "future.birthDate",
                    "Дата рождения не может быть в будущем");
            }

            // Проверка на разумный диапазон дат (не ранее 1700 года)
            if (author.getBirthDate().isBefore(LocalDate.of(1700, 1, 1))) {
                errors.rejectValue("birthDate", "too.old.birthDate",
                    "Дата рождения не может быть ранее 1700 года");
            }
        }

        // Проверка биографии (если указана)
        if (author.getBiography() != null && !author.getBiography().isEmpty()) {
            // Биография должна содержать минимум 10 символов
            if (author.getBiography().length() < 10) {
                errors.rejectValue("biography", "too.short.biography",
                    "Биография должна содержать минимум 10 символов");
            }
        }
    }
}