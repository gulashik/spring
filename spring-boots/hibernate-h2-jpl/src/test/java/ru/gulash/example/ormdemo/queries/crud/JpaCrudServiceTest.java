package ru.gulash.example.ormdemo.queries.crud;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import ru.gulash.example.ormdemo.models.Student;

import java.util.Optional;

@DataJpaTest
@Import({JpaCrudService.class})
// или
//  @SpringBootTest

class JpaCrudServiceTest {

    @Autowired
    JpaCrudService service;

    @Test
    void getStudent() {
        Optional<Student> student = service.getStudent(1L);
        System.out.println(student);
    }
}