package ru.otus.hw.migration.item.writer;

import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import ru.otus.hw.model.sourcedb.dto.GenreDto;

import javax.sql.DataSource;
import java.util.List;

@Component
public class GenreItemWriter {

    private final DataSource dataSource;

    public GenreItemWriter(
        @Qualifier("postgresDataSource") DataSource dataSource
    ) {
        this.dataSource = dataSource;
    }

    // Insert from Java to Temp table
    @Bean
    public JdbcBatchItemWriter<GenreDto> genreInsertTempTable() {
        final JdbcBatchItemWriter<GenreDto> writer = new JdbcBatchItemWriter<>();

        writer.setDataSource(dataSource);
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        writer.setSql(
            "INSERT INTO temp_table_genre(id_src, id_trg) " +
            "VALUES (:id, nextval('seq_genre_tmp'))")
        ;

        return writer;
    }

    // Insert from Temp table to Target table
    @Bean
    public JdbcBatchItemWriter<GenreDto> genreJdbcBatchItemWriter() {
        final JdbcBatchItemWriter<GenreDto> writer = new JdbcBatchItemWriter<>();

        writer.setDataSource(dataSource);
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());

        writer.setSql(
            "INSERT INTO genres(id, name) " +
            "VALUES ((SELECT id_trg FROM temp_table_genre WHERE id_src = :id), :name)"
        );

        return writer;
    }

    // Combine steps. Java -> Temp table -> Target table
    @Bean
    public CompositeItemWriter<GenreDto> compositeGenreWriter(
        final JdbcBatchItemWriter<GenreDto> genreInsertTempTable,
        final JdbcBatchItemWriter<GenreDto> genreJdbcBatchItemWriter
    ) {
        final CompositeItemWriter<GenreDto> writer = new CompositeItemWriter<>();

        writer.setDelegates(List.of(genreInsertTempTable, genreJdbcBatchItemWriter));

        return writer;
    }
}
