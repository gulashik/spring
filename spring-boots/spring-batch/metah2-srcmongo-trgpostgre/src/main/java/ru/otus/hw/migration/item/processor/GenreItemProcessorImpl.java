package ru.otus.hw.migration.item.processor;

import org.springframework.batch.item.ItemProcessor;
import ru.otus.hw.model.sourcedb.dto.GenreDto;
import ru.otus.hw.model.sourcedb.entity.Genre;

public class GenreItemProcessorImpl implements ItemProcessor<Genre, GenreDto> {

    @Override
    public GenreDto process(final Genre item) {
        return new GenreDto(item.getId(), item.getName());
    }
}
