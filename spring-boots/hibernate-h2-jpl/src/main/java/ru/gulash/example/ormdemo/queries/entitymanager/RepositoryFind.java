package ru.gulash.example.ormdemo.queries.entitymanager;

import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.gulash.example.ormdemo.models.Student;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.springframework.data.jpa.repository.EntityGraph.EntityGraphType.FETCH;

// @Transactional должна стоять на методе сервиса.
// Причем, если метод не подразумевает изменения данных в БД то категорически желательно выставить у аннотации параметр readOnly в true.
@Transactional

@RequiredArgsConstructor
@Repository
public class RepositoryFind {

    @PersistenceContext // не обязательно
    private final EntityManager entityManager;

    public Optional<Student> findById(long id) {
        EntityGraph<?> entityGraph = entityManager.getEntityGraph("student-entity-graph");
        Map<String, Object> hints = new HashMap<>();
        hints.put(FETCH.getKey(), entityGraph);
        return Optional.ofNullable(entityManager.find(Student.class, id, hints)); // todo find
    }
}
