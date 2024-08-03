package org.gulash.config;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class AppProperties implements TestFileProvider {
    private String testFileName;

    private String questionTag;

    private String answerTag;

    private String answerSpliter;
}
