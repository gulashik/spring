package org.gualsh.demo.spdrest.repository;

import org.gualsh.demo.spdrest.model.Author;
import org.gualsh.demo.spdrest.projection.AuthorWithBooks;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.time.LocalDate;
import java.util.List;

/**
 * Репозиторий для сущности Author.
 *
 * Аннотация @RepositoryRestResource конфигурирует REST-ресурс:
 * - path: определяет путь для доступа к ресурсу (/authors)
 * - collectionResourceRel: определяет имя элемента в коллекции ресурсов (authors)
 */
@RepositoryRestResource( // см BookRepository.java
    path = "authors",
    collectionResourceRel = "authors",
    excerptProjection = AuthorWithBooks.class
)
public interface AuthorRepository extends JpaRepository<Author, Long> {

    /**
     * Находит авторов по имени.
     * Доступно по URL: /authors/search/findByFirstNameContainingIgnoreCase?name={name}
     *
     * @param firstName имя или часть имени автора (без учета регистра)
     * @return список авторов с указанным именем
     */
    @RestResource(path = "byFirstName")
    List<Author> findByFirstNameContainingIgnoreCase(@Param("name") String firstName);

    /**
     * Находит авторов по фамилии.
     * Доступно по URL: /authors/search/findByLastNameContainingIgnoreCase?name={name}
     *
     * @param lastName фамилия или часть фамилии автора (без учета регистра)
     * @return список авторов с указанной фамилией
     */
    @RestResource(path = "byLastName")
    List<Author> findByLastNameContainingIgnoreCase(@Param("name") String lastName);

    /**
     * Находит авторов, родившихся после указанной даты.
     * Доступно по URL: /authors/search/findByBirthDateAfter?date={date}
     *
     * @param date дата, после которой родился автор
     * @return список авторов, родившихся после указанной даты
     */
    @RestResource(path = "bornAfter")
    Page<Author> findByBirthDateAfter(@Param("date") LocalDate date, Pageable pageable);

    /**
     * Находит авторов по имени и фамилии.
     * Доступно по URL: /authors/search/findByFirstNameAndLastName?firstName={firstName}&lastName={lastName}
     *
     * @param firstName имя автора
     * @param lastName фамилия автора
     * @return автор с указанным именем и фамилией
     */
    @RestResource(path = "byFullName")
    Author findByFirstNameAndLastName(@Param("firstName") String firstName, @Param("lastName") String lastName);

    /**
     * Скрытый метод, не доступный через REST API.
     * Аннотация @RestResource(exported = false) запрещает экспорт метода.
     *
     * @param id идентификатор автора
     */
    @Override
    @RestResource(exported = false)
    void deleteById(Long id);
}