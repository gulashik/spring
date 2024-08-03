package org.gulash.configxml.implemetation;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.gulash.configxml.TestFileProvider;

@AllArgsConstructor
@Data
public class TestFileProviderImpl implements TestFileProvider {
    private String testFileName;

    private String questionTag;

    private String answerTag;

    private String answerSplitter;

    private int skipLines;
}
