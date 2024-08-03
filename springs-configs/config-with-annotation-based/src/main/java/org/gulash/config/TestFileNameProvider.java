package org.gulash.config;

public interface TestFileNameProvider {
    String testFileName();

    String questionTag();

    String answerTag();

    String answerSplitter();

    int skipLines();
}
