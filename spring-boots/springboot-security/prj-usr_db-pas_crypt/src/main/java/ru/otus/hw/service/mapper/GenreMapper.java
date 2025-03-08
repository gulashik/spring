package ru.otus.hw.service.mapper;

import org.springframework.stereotype.Component;
import ru.otus.hw.model.dto.GenreDto;
import ru.otus.hw.model.entity.Genre;

@Component
public class GenreMapper {
    public GenreDto toDto(Genre entity) {
        return new GenreDto(entity.getId(), entity.getName());
    }

    public Genre toEntity(GenreDto dto) {
        return new Genre(dto.getId(), dto.getName());
    }
}
