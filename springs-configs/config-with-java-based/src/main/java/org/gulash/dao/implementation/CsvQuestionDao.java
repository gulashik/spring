package org.gulash.dao.implementation;

import lombok.RequiredArgsConstructor;
import org.gulash.config.TestFileProvider;
import org.gulash.dao.QuestionDao;
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

        try (InputStream iStream = getResourceAsStreamOrException(provider.getTestFileName())) {
            Scanner scanner = new Scanner(iStream);

            skipFirstLines(scanner, provider.getSkipLines());

            while (scanner.hasNextLine()) {
                questions.add(
                    lineToQuestionMapper.toQuestion(
                        scanner.nextLine(),
                        provider.getQuestionTag(),
                        provider.getAnswerTag(),
                        provider.getAnswerSplitter()
                    )
                );
            }
        } catch (Exception e) {
            throw new QuestionReadException(e.getMessage());
        }
        return questions;
    }

    private void skipFirstLines(Scanner scanner, int lineCount) {
        for (int i = 0; i < lineCount; i++) {
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }
        }
    }

    private InputStream getResourceAsStreamOrException(String fileName) {
        return Objects.requireNonNull(
                 getClass().getClassLoader().getResourceAsStream(fileName),
                 String.format("Have no file %s", fileName)
        );
    }
}
