package ru.otus.hw.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.model.targetdb.entity.Comment;

public interface JpaCommentRepository extends JpaRepository<Comment, Long> {
}
