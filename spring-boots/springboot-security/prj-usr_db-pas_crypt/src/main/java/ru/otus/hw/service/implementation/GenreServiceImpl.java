package ru.otus.hw.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.model.dto.GenreDto;
import ru.otus.hw.repository.GenreRepository;
import ru.otus.hw.service.mapper.GenreMapper;
import ru.otus.hw.service.GenreService;

import java.util.List;

@RequiredArgsConstructor
@Service
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;

    private final GenreMapper genreMapper;

    @Override
    public List<GenreDto> findAll() {
        return genreRepository
            .findAll()
            .stream()
            .map(genreMapper::toDto)
            .toList();
    }
}
