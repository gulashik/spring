package ru.otus.hw.service;

import ru.otus.hw.model.dto.AuthorDto;

import java.util.List;

public interface AuthorService {
    List<AuthorDto> findAll();
}
