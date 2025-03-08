package ru.otus.hw.service.mapper;

import org.springframework.stereotype.Component;
import ru.otus.hw.model.dto.AuthorDto;
import ru.otus.hw.model.entity.Author;

@Component
public class AuthorMapper {
    public AuthorDto toDto(Author entity) {
        return new AuthorDto(entity.getId(), entity.getFullName());
    }

    public Author toEntity(AuthorDto dto) {
        return new Author(dto.getId(), dto.getFullName());
    }
}