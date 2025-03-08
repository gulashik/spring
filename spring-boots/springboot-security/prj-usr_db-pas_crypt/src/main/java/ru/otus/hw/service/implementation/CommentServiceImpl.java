package ru.otus.hw.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.exception.EntityNotFoundException;
import ru.otus.hw.model.dto.CommentDto;
import ru.otus.hw.model.entity.Book;
import ru.otus.hw.model.entity.Comment;
import ru.otus.hw.repository.BookRepository;
import ru.otus.hw.repository.CommentRepository;
import ru.otus.hw.service.CommentService;
import ru.otus.hw.service.mapper.CommentMapper;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    private final CommentMapper commentMapper;

    private final BookRepository bookRepository;

    @Override
    public CommentDto findById(long id) {
        return commentRepository
            .findById(id)
            .map(commentMapper::toDto)
            .orElseThrow(() -> new EntityNotFoundException("No comment"));
    }

    @Override
    public List<CommentDto> findAllForBook(long bookId) {
        return commentRepository
            .findAllByBookId(bookId)
            .stream()
            .map(commentMapper::toDto)
            .toList();
    }

    @Override
    public CommentDto save(CommentDto commentDto) {
        var commentEntity = commentRepository.save(commentMapper.toEntity(commentDto));
        return commentMapper.toDto(commentEntity);
    }

    @Transactional
    @Override
    public CommentDto update(long id, String text) {
        Comment comment = commentRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Comment with id " + id + " not found"));

        comment.setText(text);

        return commentMapper.toDto(
            commentRepository.save(comment)
        );
    }

    @Transactional
    @Override
    public CommentDto create(Long bookId, String text) {
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new EntityNotFoundException("Book with id " + bookId + " not found"));

        Comment comment = commentRepository.save(
            new Comment(0L, text, book)
        );

        return commentMapper.toDto(comment);
    }

    @Transactional
    @Override
    public void deleteById(long id) {
        commentRepository.deleteById(id);
    }
}
