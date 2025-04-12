package ru.otus.hw.migration.item.writer;

import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import ru.otus.hw.model.sourcedb.dto.CommentDto;

import javax.sql.DataSource;
import java.util.List;

@Component
public class CommentItemWriter {
    private final DataSource dataSource;

    public CommentItemWriter(
        @Qualifier("postgresDataSource") DataSource dataSource
    ) {
        this.dataSource = dataSource;
    }

    // Insert from Java to Temp table
    @Bean
    public JdbcBatchItemWriter<CommentDto> commentInsertTempTable() {
        final JdbcBatchItemWriter<CommentDto> writer = new JdbcBatchItemWriter<>();

        writer.setDataSource(dataSource);
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        writer.setSql(
            "INSERT INTO temp_table_comment(id_src, id_trg) " +
                "VALUES (:id, nextval('seq_comment_tmp'))"
        );

        return writer;
    }

    // Insert from Temp table to Target table
    @Bean
    public JdbcBatchItemWriter<CommentDto> commentJdbcBatchItemWriter() {
        final JdbcBatchItemWriter<CommentDto> writer = new JdbcBatchItemWriter<>();

        writer.setDataSource(dataSource);
        writer.setItemPreparedStatementSetter(
            (commentDto, statement) -> {
                statement.setString(1, commentDto.getCommentText());
                statement.setString(2, commentDto.getId());
                statement.setString(3, commentDto.getBookId());
            }
        );
        writer.setSql(
            "INSERT INTO comments(comment_text, id, book_id) " +
            "VALUES (?, " +
            "(SELECT id_trg FROM temp_table_comment WHERE id_src = ?), " +
            "(SELECT id_trg FROM temp_table_book WHERE id_src = ?))"
        );

        return writer;
    }

    // Combine steps. Java -> Temp table -> Target table
    @Bean
    public CompositeItemWriter<CommentDto> compositeCommentWriter(
        final JdbcBatchItemWriter<CommentDto> commentInsertTempTable,
        final JdbcBatchItemWriter<CommentDto> commentJdbcBatchItemWriter
    ) {
        final CompositeItemWriter<CommentDto> writer = new CompositeItemWriter<>();

        writer.setDelegates(
            List.of(commentInsertTempTable, commentJdbcBatchItemWriter)
        );

        return writer;
    }
}
