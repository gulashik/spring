package org.gualsh.demo.spdrest.repository;

import org.gualsh.demo.spdrest.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для сущности Book.
 *
 * Аннотация @RepositoryRestResource конфигурирует REST-ресурс:
 * - path: определяет путь для доступа к ресурсу (/books)
 * - collectionResourceRel: определяет имя элемента в коллекции ресурсов (books)
 */
@RepositoryRestResource(
    path = "books",
    collectionResourceRel = "books"
)
public interface BookRepository extends JpaRepository<Book, Long> {

    /**
     * Находит книгу по ISBN.
     * Доступно по URL: /books/search/findByIsbn?isbn={isbn}
     *
     * @param isbn ISBN книги
     * @return книга с указанным ISBN
     */
    @RestResource(path = "byIsbn")
    Optional<Book> findByIsbn(@Param("isbn") String isbn);

    /**
     * Находит книги по названию (без учета регистра).
     * Доступно по URL: /books/search/findByTitleContainingIgnoreCase?title={title}
     *
     * @param title название или часть названия книги
     * @return список книг с подходящим названием
     */
    @RestResource(path = "byTitle")
    Page<Book> findByTitleContainingIgnoreCase(@Param("title") String title, Pageable pageable);

    /**
     * Находит книги по идентификатору категории.
     * Доступно по URL: /books/search/findByCategoryId?categoryId={categoryId}
     *
     * @param categoryId идентификатор категории
     * @return список книг в указанной категории
     */
    @RestResource(path = "byCategoryId")
    Page<Book> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    /**
     * Находит книги по имени категории.
     * Доступно по URL: /books/search/findByCategoryName?categoryName={categoryName}
     *
     * @param categoryName название категории
     * @return список книг в указанной категории
     */
    @RestResource(path = "byCategoryName")
    Page<Book> findByCategoryNameIgnoreCase(@Param("categoryName") String categoryName, Pageable pageable);

    /**
     * Находит книги по идентификатору автора.
     * Доступно по URL: /books/search/findByAuthorsId?authorId={authorId}
     *
     * @param authorId идентификатор автора
     * @return список книг указанного автора
     */
    @RestResource(path = "byAuthorId")
    Page<Book> findByAuthorsId(@Param("authorId") Long authorId, Pageable pageable);

    /**
     * Находит книги, опубликованные после указанной даты.
     * Доступно по URL: /books/search/findByPublicationDateAfter?date={date}
     *
     * @param date дата публикации
     * @return список книг, опубликованных после указанной даты
     */
    @RestResource(path = "publishedAfter")
    Page<Book> findByPublicationDateAfter(@Param("date") LocalDate date, Pageable pageable);

    /**
     * Находит книги по языку.
     * Доступно по URL: /books/search/findByLanguageIgnoreCase?language={language}
     *
     * @param language язык книги
     * @return список книг на указанном языке
     */
    @RestResource(path = "byLanguage")
    Page<Book> findByLanguageIgnoreCase(@Param("language") String language, Pageable pageable);

    /**
     * Находит книги по издательству.
     * Доступно по URL: /books/search/findByPublisherContainingIgnoreCase?publisher={publisher}
     *
     * @param publisher издательство или его часть
     * @return список книг указанного издательства
     */
    @RestResource(path = "byPublisher")
    Page<Book> findByPublisherContainingIgnoreCase(@Param("publisher") String publisher, Pageable pageable);

    /**
     * Сложный запрос с использованием JPQL.
     * Находит книги по нескольким критериям.
     * Доступно по URL: /books/search/findByMultipleCriteria?title={title}&categoryId={categoryId}&language={language}
     *
     * @param title название или часть названия книги
     * @param categoryId идентификатор категории
     * @param language язык книги
     * @return список книг, соответствующих всем критериям
     */
    @Query("SELECT b FROM Book b WHERE " +
        "(:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
        "(:categoryId IS NULL OR b.category.id = :categoryId) AND " +
        "(:language IS NULL OR LOWER(b.language) = LOWER(:language))")
    @RestResource(path = "byMultipleCriteria")
    Page<Book> findByMultipleCriteria(
        @Param("title") String title,
        @Param("categoryId") Long categoryId,
        @Param("language") String language,
        Pageable pageable);
}