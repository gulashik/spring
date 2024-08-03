package org.gulash.dao;

import lombok.RequiredArgsConstructor;
import org.gulash.config.TestFileProvider;
import org.gulash.domain.Question;
import org.gulash.exceptions.QuestionReadException;
import org.gulash.mapper.LineMapper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

@RequiredArgsConstructor
public class CsvQuestionDao implements QuestionDao {
    private final TestFileProvider provider;

    private final LineMapper lineToQuestionMapper;

    @Override
    public List<Question> findAll() {
        List<Question> questions = new ArrayList<>();

        try (
                InputStream iStream =
                         getClass()
                        .getClassLoader()
                        .getResourceAsStream(provider.getTestFileName());
        ) {
            Objects.requireNonNull(iStream, String.format("Have no file %s", provider.getTestFileName()));
            Scanner scanner = new Scanner(Objects.requireNonNull(iStream));

            scanner.nextLine(); // skip first line
            while (scanner.hasNextLine()) {
                questions.add(
                    lineToQuestionMapper.toQuestion(
                            scanner.nextLine(),
                            provider.getQuestionTag(),
                            provider.getAnswerTag(),
                            provider.getAnswerSpliter()
                    )
                );
            }
        } catch (Exception e) {
            throw new QuestionReadException(e.getMessage());
        }
        return questions;
    }
}
