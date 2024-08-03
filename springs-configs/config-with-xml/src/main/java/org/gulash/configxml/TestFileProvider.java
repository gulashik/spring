package org.gulash.configxml;

public interface TestFileProvider {
    String getTestFileName();

    String getQuestionTag();

    String getAnswerTag();

    String getAnswerSplitter();

    int getSkipLines();
}
