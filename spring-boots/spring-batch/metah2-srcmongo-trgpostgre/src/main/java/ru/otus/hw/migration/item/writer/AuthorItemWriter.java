package ru.otus.hw.migration.item.writer;

import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import ru.otus.hw.model.sourcedb.dto.AuthorDto;

import javax.sql.DataSource;
import java.util.List;

@Component
public class AuthorItemWriter {
    private final DataSource dataSource;

    public AuthorItemWriter(
        @Qualifier("postgresDataSource") DataSource dataSource
    ) {
        this.dataSource = dataSource;
    }

    // Insert from Java to Temp table
    @Bean
    public JdbcBatchItemWriter<AuthorDto> authorInsertTempTable() {
        final JdbcBatchItemWriter<AuthorDto> writer = new JdbcBatchItemWriter<>();

        writer.setDataSource(dataSource);
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        writer.setSql(
            "INSERT INTO temp_table_author(id_src, id_trg) " +
            "VALUES (:id, nextval('seq_author_tmp'))"
        );

        return writer;
    }

    // Insert from Temp table to Target table
    @Bean
    public JdbcBatchItemWriter<AuthorDto> authorJdbcBatchItemWriter() {
        final JdbcBatchItemWriter<AuthorDto> writer = new JdbcBatchItemWriter<>();

        writer.setDataSource(dataSource);
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        writer.setSql(
            "INSERT INTO authors(id, full_name) VALUES " +
            "((SELECT id_trg FROM temp_table_author WHERE id_src = :id), :fullName)"
        );

        return writer;
    }

    // Combine steps. Java -> Temp table -> Target table
    @Bean
    public CompositeItemWriter<AuthorDto> compositeAuthorWriter(
        final JdbcBatchItemWriter<AuthorDto> authorInsertTempTable,
        final JdbcBatchItemWriter<AuthorDto> authorJdbcBatchItemWriter
    ) {
        final CompositeItemWriter<AuthorDto> writer = new CompositeItemWriter<>();

        writer.setDelegates(
            List.of(authorInsertTempTable, authorJdbcBatchItemWriter)
        );

        return writer;
    }
}
