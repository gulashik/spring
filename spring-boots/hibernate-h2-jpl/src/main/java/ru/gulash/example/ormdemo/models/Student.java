package ru.gulash.example.ormdemo.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity // Указывает, что данный класс является сущностью
@Table(name = "students") // Задает имя таблицы, на которую будет отображаться сущность
@NamedEntityGraph(name = "student-entity-graph",
    attributeNodes = {@NamedAttributeNode("avatar"), @NamedAttributeNode("emails"), @NamedAttributeNode("courses")}
)
@NamedEntityGraph(name = "student-entity-graph-avatar-only",
    attributeNodes = {@NamedAttributeNode("avatar")}
)
public class Student {
    @Id // Позволяет указать какое поле является идентификатором
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Стратегия генерации идентификаторов
    private long id;

    // Задает имя и некоторые свойства поля таблицы, на которое будет отображаться поле сущности
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    // Указывает на связь между таблицами "один к одному"
    @OneToOne(targetEntity = Avatar.class, cascade = CascadeType.ALL)
    // Задает поле, по которому происходит объединение с таблицей для хранения связанной сущности
    @JoinColumn(name = "avatar_id")
    private Avatar avatar;

    // Указывает на связь между таблицами "один ко многим"
    @OneToMany(targetEntity = EMail.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private List<EMail> emails;

    @Fetch(FetchMode.SELECT)
    @BatchSize(size = 5)
    // Указывает на связь между таблицами "многие ко многим"
    @ManyToMany(targetEntity = Course.class, fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    // Задает таблицу связей между таблицами для хранения родительской и связанной сущностью
    @JoinTable(name = "student_courses", joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id"))
    private List<Course> courses;
}

