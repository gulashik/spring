package ru.otus.hw.service.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.hw.model.dto.CommentDto;
import ru.otus.hw.model.entity.Comment;

@RequiredArgsConstructor
@Component
public class CommentMapper {

    private final BookMapper bookMapper;

    public CommentDto toDto(Comment entity) {
        return new CommentDto(
            entity.getId(),
            entity.getText(),
            bookMapper.toDto(entity.getBook())
        );
    }

    public Comment toEntity(CommentDto dto) {
        return new Comment(
            dto.getId(),
            dto.getText(),
            bookMapper.toEntity(dto.getBookDto())
        );
    }
}
