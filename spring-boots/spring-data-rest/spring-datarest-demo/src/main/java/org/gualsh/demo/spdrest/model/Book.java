package org.gualsh.demo.spdrest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Модель данных, представляющая книгу.
 * Использует JPA аннотации для связи с базой данных.
 * Включает базовую валидацию с помощью Bean Validation API.
 */
@Entity
@Table(name = "books")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
@JsonIgnoreProperties(value = {"createdAt", "updatedAt"}, allowGetters = true)
public class Book {

    /**
     * Уникальный идентификатор книги.
     * Генерируется автоматически базой данных.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Название книги. Не может быть пустым.
     */
    @NotBlank(message = "Название книги не может быть пустым")
    @Column(name = "title", nullable = false)
    private String title;

    /**
     * ISBN (International Standard Book Number) книги.
     * Уникальный идентификатор для книг.
     */
    @Column(name = "isbn", unique = true)
    private String isbn;

    /**
     * Дата публикации книги. Должна быть в прошлом или настоящем.
     */
    @PastOrPresent(message = "Дата публикации должна быть в прошлом или настоящем")
    @Column(name = "publication_date")
    private LocalDate publicationDate;

    /**
     * Количество страниц в книге. Должно быть положительным числом.
     */
    @Positive(message = "Количество страниц должно быть положительным числом")
    @Column(name = "page_count")
    private Integer pageCount;

    /**
     * Краткое описание книги.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Категория, к которой относится книга.
     * Связь многие-к-одному.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties({"books", "hibernateLazyInitializer"})
    private Category category;

    /**
     * Авторы книги.
     * Связь многие-ко-многим.
     */
    @ManyToMany(cascade = {CascadeType.MERGE})
    @JoinTable(
        name = "book_authors",
        joinColumns = @JoinColumn(name = "book_id"),
        inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    @JsonIgnoreProperties("books")
    private Set<Author> authors = new HashSet<>();

    /**
     * Язык, на котором написана книга.
     */
    @Column(name = "language", length = 50)
    private String language;

    /**
     * Издательство, выпустившее книгу.
     */
    @Column(name = "publisher", length = 100)
    private String publisher;

    /**
     * Дата и время создания записи.
     * Заполняется автоматически.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Дата и время последнего обновления записи.
     * Обновляется автоматически.
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Добавляет автора к книге.
     *
     * @param author Автор, которого нужно добавить
     * @return Текущий экземпляр книги для цепочки вызовов
     */
    public Book addAuthor(Author author) {
        this.authors.add(author);
        author.getBooks().add(this);
        return this;
    }

    /**
     * Удаляет автора из книги.
     *
     * @param author Автор, которого нужно удалить
     * @return Текущий экземпляр книги для цепочки вызовов
     */
    public Book removeAuthor(Author author) {
        this.authors.remove(author);
        author.getBooks().remove(this);
        return this;
    }
}