package com.ch.reviewer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

public class JavaSourceParser {
    private final JavaParser parser = new JavaParser(
            new ParserConfiguration()
                    .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_11)); // Changed to 11

    public Optional<CompilationUnit> parse(Path file) {
        try {
            // Replaced 'var' with the explicit generic type
            com.github.javaparser.ParseResult<CompilationUnit> result = parser.parse(file);

            if (result.isSuccessful() && result.getResult().isPresent()) {
                CompilationUnit cu = result.getResult().get();
                // CRITICAL FIX: Attach the file path to the AST
                cu.setStorage(file, java.nio.charset.StandardCharsets.UTF_8);
                return Optional.of(cu);
            } else {
                System.err.println("Parse failed for " + file + ": " + result.getProblems());
                return Optional.empty();
            }
        } catch (IOException e) {
            System.err.println("IO error reading " + file + ": " + e.getMessage());
            return Optional.empty();
        }
    }
}