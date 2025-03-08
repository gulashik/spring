package ru.otus.hw.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.model.entity.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
   List<Comment> findAllByBookId(long bookId);
}