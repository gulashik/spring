package org.gulash.mapper;

import lombok.RequiredArgsConstructor;
import org.gulash.domain.Answer;
import org.gulash.domain.Question;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuestionMapper {
    public String toFormatedString(Question question) {
        validateQuestion(question);

        StringBuilder resultString = new StringBuilder();
        resultString.append("Question:").append(question.text()).append("\n");

        for (Answer answer : question.answers()) {
            resultString.append(answer.text()).append("\n");
        }

        return resultString.toString();
    }

    private void validateQuestion (Question question) {
        if (question == null) {
            throw new IllegalArgumentException("Question must not be null");
        }
    }
}
