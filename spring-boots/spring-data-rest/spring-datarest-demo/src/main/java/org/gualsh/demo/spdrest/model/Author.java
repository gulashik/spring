package org.gualsh.demo.spdrest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
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
 * Модель данных, представляющая автора книг.
 * Использует JPA аннотации для связи с базой данных.
 * Включает базовую валидацию с помощью Bean Validation API.
 * Аудит создания и изменения с использованием Spring Data JPA Auditing.
 */
@Entity
@Table(name = "authors")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
@JsonIgnoreProperties(value = {"createdAt", "updatedAt"}, allowGetters = true)
public class Author {

    /**
     * Уникальный идентификатор автора.
     * Генерируется автоматически базой данных.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Имя автора. Не может быть пустым.
     */
    @NotBlank(message = "Имя автора не может быть пустым")
    @Column(name = "first_name", nullable = false)
    private String firstName;

    /**
     * Фамилия автора. Не может быть пустой.
     */
    @NotBlank(message = "Фамилия автора не может быть пустой")
    @Column(name = "last_name", nullable = false)
    private String lastName;

    /**
     * Дата рождения автора. Должна быть в прошлом.
     */
    @Past(message = "Дата рождения должна быть в прошлом")
    @Column(name = "birth_date")
    private LocalDate birthDate;

    /**
     * Краткая биография автора.
     */
    @Column(name = "biography", columnDefinition = "TEXT")
    private String biography;

    /**
     * Связь многие-ко-многим с книгами.
     * Владельцем связи является сущность Book.
     */
    @ManyToMany(mappedBy = "authors")
    @JsonIgnoreProperties("authors")
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
