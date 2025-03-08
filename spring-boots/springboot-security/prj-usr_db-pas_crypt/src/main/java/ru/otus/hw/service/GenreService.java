package ru.otus.hw.service;

import ru.otus.hw.model.dto.GenreDto;

import java.util.List;

public interface GenreService {
    List<GenreDto> findAll();
}
