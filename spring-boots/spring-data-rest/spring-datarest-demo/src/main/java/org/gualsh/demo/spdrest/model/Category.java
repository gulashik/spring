package org.gualsh.demo.spdrest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Модель данных, представляющая категорию книг (жанр).
 * Использует JPA аннотации для связи с базой данных.
 * Включает базовую валидацию с помощью Bean Validation API.
 */
@Entity
@Table(name = "categories")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
@JsonIgnoreProperties(value = {"createdAt", "updatedAt"}, allowGetters = true)
public class Category {

    /**
     * Уникальный идентификатор категории.
     * Генерируется автоматически базой данных.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Название категории. Не может быть пустым.
     */
    @NotBlank(message = "Название категории не может быть пустым")
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    /**
     * Описание категории.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Связь один-ко-многим с книгами.
     * Одна категория может содержать множество книг.
     */
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("category")
    private Set<Book> books = new HashSet<>();

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
}
