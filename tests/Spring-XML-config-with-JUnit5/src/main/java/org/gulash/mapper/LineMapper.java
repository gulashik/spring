package org.gulash.mapper;

import lombok.RequiredArgsConstructor;
import org.gulash.domain.Answer;
import org.gulash.domain.Question;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@RequiredArgsConstructor
public class LineMapper {

    public Question toQuestion(
            String line,
            String questionTag,
            String answerTag,
            String answerSpliter
    ) {
        String[] split = line.trim().split(questionTag);

        return new Question(
                /*text*/    split[0],
                /*answers*/ this.toAnswer(split[1], answerTag, answerSpliter)
        );
    }

    private List<Answer> toAnswer(String line, String answerTag, String answerSpliter) {
        List<Answer> answers = new ArrayList<>();

        Scanner rawAnswers = new Scanner(line).useDelimiter(answerTag);
        while (rawAnswers.hasNext()) {
            String[] rawAnswer = rawAnswers.next().trim().split(answerSpliter);
            answers.add(new Answer(rawAnswer[0], Boolean.parseBoolean(rawAnswer[1])));
        }
        return answers;
    }
}
