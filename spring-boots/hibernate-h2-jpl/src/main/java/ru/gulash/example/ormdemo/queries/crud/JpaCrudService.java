package ru.gulash.example.ormdemo.queries.crud;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.gulash.example.ormdemo.models.Student;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class JpaCrudService {
    private final JpaCrudRepository jpaCrudRepository;

    @Transactional
    public Optional<Student> getStudent(Long id) {
        return jpaCrudRepository.findById(id);
    }
}
