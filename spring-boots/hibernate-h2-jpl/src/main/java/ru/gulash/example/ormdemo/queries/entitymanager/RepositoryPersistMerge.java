package ru.gulash.example.ormdemo.queries.entitymanager;

import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.gulash.example.ormdemo.models.Student;

import java.util.List;

import static org.springframework.data.jpa.repository.EntityGraph.EntityGraphType.FETCH;

// @Transactional должна стоять на методе сервиса.
// Причем, если метод не подразумевает изменения данных в БД то категорически желательно выставить у аннотации параметр readOnly в true.
@Transactional

@RequiredArgsConstructor
@Repository
public class RepositoryPersistMerge {

    @PersistenceContext // не обязательно
    private final EntityManager entityManager;


    public Student save(Student student) {
        if (student.getId() == 0) {
            entityManager.persist(student); // todo persist
            return student;
        }
        return entityManager.merge(student); // todo merge
    }

    public List<Student> findAll() {
        EntityGraph<?> entityGraph = entityManager.getEntityGraph("student-entity-graph");
        TypedQuery<Student> query = entityManager.createQuery("select distinct s from Student s left join fetch s.emails", Student.class);
        query.setHint(FETCH.getKey(), entityGraph);
        return query.getResultList();
    }

    public List<Student> findByName(String name) {
        TypedQuery<Student> query = entityManager.createQuery("select s " +
                        "from Student s " +
                        "where s.name = :name",
                Student.class);
        query.setParameter("name", name);
        return query.getResultList();
    }

    // Только для примера, в реальности JPQL лучше использовать только для массовых операций
    public void updateNameById(long id, String name) {
        Query query = entityManager.createQuery("update Student s " +
                "set s.name = :name " +
                "where s.id = :id");
        query.setParameter("name", name);
        query.setParameter("id", id);
        query.executeUpdate();
    }

    // Только для примера, в реальности JPQL лучше использовать только для массовых операций
    public void deleteById(long id) {
        Query query = entityManager.createQuery("delete " +
                "from Student s " +
                "where s.id = :id");
        query.setParameter("id", id);
        query.executeUpdate();
    }

}
