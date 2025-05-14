package org.gualsh.demo.spdrest.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import jakarta.persistence.EntityNotFoundException;
import org.gualsh.demo.spdrest.model.Author;
import org.gualsh.demo.spdrest.model.Book;
import org.gualsh.demo.spdrest.model.Category;
import org.gualsh.demo.spdrest.projection.AuthorWithBooks;
import org.gualsh.demo.spdrest.repository.AuthorRepository;
import org.gualsh.demo.spdrest.repository.BookRepository;
import org.gualsh.demo.spdrest.validator.AuthorValidator;
import org.gualsh.demo.spdrest.validator.BookValidator;
import org.gualsh.demo.spdrest.validator.DeletionValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.core.mapping.ExposureConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Конфигурация Spring Data REST.
 *
 * Настраивает поведение REST API, включая:
 * - экспозицию ID в JSON-ответах
 * - базовый путь API
 * - разрешенные операции для сущностей
 * - CORS-настройки
 * - настройки сериализации/десериализации Jackson
 * - настройки конвертации типов
 * - настройки валидации сущностей
 */
@Configuration
public class SpringDataRestConfig {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;

    @Autowired
    public SpringDataRestConfig(BookRepository bookRepository, AuthorRepository authorRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
    }

    /**
     * Настраивает конфигуратор REST API.
     *
     * @return настроенный RepositoryRestConfigurer
     */
    @Bean
    public RepositoryRestConfigurer repositoryRestConfigurer() {
        return new RepositoryRestConfigurer() {
            /**
             * Настраивает базовую конфигурацию репозиториев REST API.
             * Определяет, как репозитории будут представлены в виде REST-ресурсов.
             *
             * @param config Конфигурация репозиториев
             * @param cors Реестр CORS-конфигураций
             */
            @Override
            public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
                // Включаем экспозицию ID в JSON-ответах
                // По умолчанию Spring Data REST не включает поля ID в ответы,
                // что может быть неудобно для клиентских приложений
                config.exposeIdsFor(Author.class, Book.class, Category.class);

                // Устанавливаем базовый путь API
                // Все эндпоинты Spring Data REST будут доступны по этому пути
                config.setBasePath("/api");

                // Регистрируем Проекции, которые находятся не в папке с сущностью
                config.getProjectionConfiguration()
                    .addProjection(AuthorWithBooks.class);

                // Настраиваем разрешенные операции для сущностей
                // Это позволяет ограничить доступные HTTP-методы для определенных сущностей
                ExposureConfiguration exposureConfig = config.getExposureConfiguration();

                // Для авторов запрещаем операции DELETE
                // Это предотвращает случайное удаление авторов через API
                exposureConfig.forDomainType(Author.class)
                    .withItemExposure((metadata, httpMethods) ->
                        httpMethods.disable(HttpMethod.DELETE));

                // Для категорий запрещаем операции DELETE
                // Это предотвращает случайное удаление категорий через API
                exposureConfig.forDomainType(Category.class)
                    .withItemExposure((metadata, httpMethods) ->
                        httpMethods.disable(HttpMethod.DELETE));

                // CORS конфигурация
                // Определяет, какие домены, методы и заголовки разрешены для доступа к API
                cors.addMapping("/api/**")
                    .allowedOrigins("*")
                    .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE")
                    .allowedHeaders("*")
                    .allowCredentials(false)
                    .maxAge(3600);
            }

            /**
             * Настраивает ObjectMapper из Jackson для сериализации/десериализации JSON.
             * Этот метод определяет, как объекты Java будут преобразовываться в JSON и обратно.
             *
             * @param objectMapper Маппер объектов Jackson
             */
            @Override
            public void configureJacksonObjectMapper(ObjectMapper objectMapper) {
                // Регистрируем модуль для корректной обработки Java 8 Date/Time API (JSR-310)
                JavaTimeModule javaTimeModule = new JavaTimeModule();

                // Настраиваем кастомные сериализаторы для Java 8 типов даты/времени
                // Это позволяет контролировать формат дат в JSON-ответах

                // Настройка форматов для дат и времени
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                // Регистрируем сериализаторы с указанными форматами
                javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter));
                javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));

                // Регистрируем модуль в ObjectMapper
                objectMapper.registerModule(javaTimeModule);

                // Отключаем сериализацию дат как временных меток
                // По умолчанию Jackson сериализует даты как числа (timestamp),
                // но мы хотим видеть их в удобочитаемом строковом формате
                objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

                // Настраиваем опции сериализации

                // Включаем отступы для красивого форматирования JSON
                // Это делает ответы более читаемыми при отладке, но увеличивает размер JSON
                objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

                // Отключаем сериализацию пустых бинов как null
                // Это позволяет сериализовать пустые объекты как пустые JSON-объекты {},
                // а не как null, что может быть полезно для клиентов
                objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            }

            /**
             * Настраивает службу конвертации типов.
             * Этот метод позволяет регистрировать пользовательские конвертеры
             * для преобразования между различными типами данных в API.
             *
             * @param conversionService Конфигурируемый сервис конвертации
             */
            @Override
            public void configureConversionService(ConfigurableConversionService conversionService) {
                // Регистрируем конвертер из строки в LocalDate с поддержкой разных форматов дат
                conversionService.addConverter(String.class, LocalDate.class, source -> {
                    // Поддерживаем несколько форматов входных дат
                    String trimmed = source.trim();
                    try {
                        // Сначала пробуем стандартный ISO формат (yyyy-MM-dd)
                        return LocalDate.parse(trimmed);
                    } catch (Exception e1) {
                        try {
                            // Затем пробуем формат с точками (dd.MM.yyyy)
                            return LocalDate.parse(trimmed, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                        } catch (Exception e2) {
                            try {
                                // И формат с косыми чертами (MM/dd/yyyy)
                                return LocalDate.parse(trimmed, DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                            } catch (Exception e3) {
                                // Если не удалось разобрать ни в одном формате, выбрасываем исключение
                                throw new IllegalArgumentException(
                                    "Не удалось преобразовать строку '" + trimmed +
                                        "' в дату. Поддерживаемые форматы: yyyy-MM-dd, dd.MM.yyyy, MM/dd/yyyy"
                                );
                            }
                        }
                    }
                });

                // Конвертер для строковых идентификаторов в формате "isbn:XXXXXXXXXX" в объект Book
                conversionService.addConverter(String.class, Book.class, source -> {
                    if (source.startsWith("isbn:")) {
                        String isbn = source.substring(5); // Удаляем префикс "isbn:"
                        return bookRepository.findByIsbn(isbn)
                            .orElseThrow(() -> new EntityNotFoundException(
                                "Книга с ISBN '" + isbn + "' не найдена"
                            ));
                    } else {
                        try {
                            // Пробуем интерпретировать как числовой ID
                            Long id = Long.parseLong(source);
                            return bookRepository.findById(id)
                                .orElseThrow(() -> new EntityNotFoundException(
                                    "Книга с ID " + id + " не найдена"
                                ));
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException(
                                "Неправильный формат идентификатора книги: " + source +
                                    ". Используйте числовой ID или префикс 'isbn:'"
                            );
                        }
                    }
                });

                // Конвертер для строк в формате "firstName lastName" в объект Author
                conversionService.addConverter(String.class, Author.class, source -> {
                    String[] parts = source.split("\\s+", 2);
                    if (parts.length == 2) {
                        String firstName = parts[0];
                        String lastName = parts[1];
                        return authorRepository.findByFirstNameAndLastName(firstName, lastName);
                    }
                    throw new IllegalArgumentException(
                        "Неправильный формат имени автора: " + source +
                            ". Используйте формат 'Имя Фамилия'"
                    );
                });
            }

            /**
             * Настраивает валидацию событий репозитория.
             * Этот метод регистрирует валидаторы, которые будут применяться
             * на разных этапах жизненного цикла сущностей.
             *
             * @param validatingListener слушатель событий валидации
             */
            @Override
            public void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener validatingListener) {

                // Регистрируем валидатор для книг перед созданием
                // Это обеспечивает валидацию всех новых книг перед сохранением в БД
                validatingListener.addValidator("beforeCreate", new BookValidator());

                // Регистрируем валидатор для книг перед обновлением
                // Это обеспечивает валидацию книг при их обновлении
                validatingListener.addValidator("beforeSave", new BookValidator());

                // Регистрируем валидатор для авторов перед созданием
                validatingListener.addValidator("beforeCreate", new AuthorValidator());

                // Регистрируем валидатор для авторов перед обновлением
                validatingListener.addValidator("beforeSave", new AuthorValidator());

                // Для событий, связанных с удалением, добавляем валидатор
                // DeletionValidator проверяет, можно ли безопасно удалить сущность
                // (например, запрещает удаление автора, у которого есть книги)
                validatingListener.addValidator("beforeDelete", new DeletionValidator());
            }

            /**
             * Настраивает HTTP-конвертеры сообщений.
             * Этот метод позволяет добавить пользовательские конвертеры или настроить существующие.
             *
             * @param messageConverters Список конвертеров HTTP-сообщений
             */
            @Override
            public void configureHttpMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
                // В этом методе можно добавить или настроить конвертеры для разных типов контента
                // Например, можно добавить конвертеры для XML, CSV и других форматов

                // В данном примере мы не добавляем пользовательские конвертеры,
                // используя стандартные конвертеры Spring
            }
        };
    }
}