package ru.otus.hw.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.model.entity.Genre;

public interface GenreRepository extends JpaRepository<Genre, Long> {
}
