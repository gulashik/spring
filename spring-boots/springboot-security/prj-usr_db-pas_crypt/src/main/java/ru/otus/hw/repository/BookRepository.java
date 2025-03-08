package ru.otus.hw.repository;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.model.entity.Book;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    @EntityGraph("BookEntity-author-genre")
    @NotNull
    Optional<Book> findById(@NotNull Long id);

    @EntityGraph("BookEntity-author-genre")
    @NotNull
    List<Book> findAll();
}
