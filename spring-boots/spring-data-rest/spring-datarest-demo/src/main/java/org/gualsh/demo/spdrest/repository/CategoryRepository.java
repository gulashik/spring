package org.gualsh.demo.spdrest.repository;

import org.gualsh.demo.spdrest.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для сущности Category.
 *
 * Аннотация @RepositoryRestResource конфигурирует REST-ресурс:
 * - path: определяет путь для доступа к ресурсу (/categories)
 * - collectionResourceRel: определяет имя элемента в коллекции ресурсов (categories)
 */
@RepositoryRestResource(
    path = "categories",
    collectionResourceRel = "categories"
)
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Находит категорию по названию.
     * Доступно по URL: /categories/search/findByName?name={name}
     *
     * @param name название категории
     * @return категория с указанным названием
     */
    @RestResource(path = "byName")
    Optional<Category> findByName(@Param("name") String name);

    /**
     * Находит категории по части названия (без учета регистра).
     * Доступно по URL: /categories/search/findByNameContainingIgnoreCase?name={name}
     *
     * @param name часть названия категории
     * @return список категорий, содержащих указанную строку в названии
     */
    @RestResource(path = "byNameContaining")
    List<Category> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Находит категории, в описании которых содержится указанная строка (без учета регистра).
     * Доступно по URL: /categories/search/findByDescriptionContainingIgnoreCase?text={text}
     *
     * @param text строка, которую должно содержать описание
     * @return список категорий с подходящим описанием
     */
    @RestResource(path = "byDescription")
    List<Category> findByDescriptionContainingIgnoreCase(@Param("text") String text);
}