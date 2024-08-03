package org.gulash.service.implementation;

import org.gulash.service.IOService;

import java.io.PrintStream;

public class IOServiceStreams implements IOService {
    private final PrintStream printStream;

    public IOServiceStreams() {
        this.printStream = System.out;
    }

    public IOServiceStreams(PrintStream printStream) {
        this.printStream = printStream;
    }

    @Override
    public void printLine(String s) {
        printStream.println(s);
    }

    @Override
    public void printFormattedLine(String s, Object... args) {
        printStream.printf(s + "%n", args);
    }
}
