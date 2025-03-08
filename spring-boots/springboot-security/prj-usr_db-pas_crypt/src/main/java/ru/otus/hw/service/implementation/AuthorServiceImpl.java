package ru.otus.hw.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.model.dto.AuthorDto;
import ru.otus.hw.repository.AuthorRepository;
import ru.otus.hw.service.AuthorService;
import ru.otus.hw.service.mapper.AuthorMapper;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;

    private final AuthorMapper authorMapper;

    @Override
    public List<AuthorDto> findAll() {
        return authorRepository
            .findAll()
            .stream()
            .map(authorMapper::toDto)
            .toList();
    }
}
