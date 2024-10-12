package ru.gulash.example.ormdemo.queries.crud;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.gulash.example.ormdemo.models.Student;

import java.util.Optional;

public interface JpaCrudRepository extends JpaRepository<Student, Long> {

    // todo entity graph добавляем в CRUD Repository т.е. придётся явно достать метод из CRUD репозитория
    @EntityGraph(value = "student-entity-graph", type = EntityGraph.EntityGraphType.FETCH)
    @Override
    Optional<Student> findById(Long id);
}
