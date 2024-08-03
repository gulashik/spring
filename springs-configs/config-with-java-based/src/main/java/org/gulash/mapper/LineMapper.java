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
            String answerSplitter
    ) {
        String[] split = line.trim().split(questionTag);

        return new Question(
                /*text*/    split[0],
                /*answers*/ this.toAnswer(split[1], answerTag, answerSplitter)
        );
    }

    private List<Answer> toAnswer(String line, String answerTag, String answerSplitter) {
        List<Answer> answers = new ArrayList<>();
        int questionIndex = 0;

        Scanner rawAnswers = new Scanner(line).useDelimiter(answerTag);
        while (rawAnswers.hasNext()) {
            String[] rawAnswer = rawAnswers.next().trim().split(answerSplitter);
            answers.add(
                new Answer(
                    String.format("%1$s) %2$s", ++questionIndex, rawAnswer[0]),
                    Boolean.parseBoolean(rawAnswer[1])
                )
            );
        }
        return answers;
    }
}
