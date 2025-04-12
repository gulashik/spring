package ru.otus.hw.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.model.targetdb.entity.Genre;

public interface JpaGenreRepository extends JpaRepository<Genre, Long> {
}
