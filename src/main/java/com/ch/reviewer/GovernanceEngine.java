package com.ch.reviewer;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GovernanceEngine {

    public static void main(String[] args) throws Exception {
        Path root = java.nio.file.Paths.get(args.length > 0 ? args[0] : "src/main/java");
        Path out  = java.nio.file.Paths.get("target");

        // 1. Scan & Extract
        List<Path> files = new SourceScanner().scan(root);
        JavaSourceParser parser = new JavaSourceParser();
        ClassInfoExtractor extractor = new ClassInfoExtractor();

        List<ClassInfo> classes = files.stream()
                .map(parser::parse)
                .filter(java.util.Optional::isPresent) // 1. Keep only successful parses
                .map(java.util.Optional::get)          // 2. Unwrap the Optional
                .map(extractor::extract)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toList());

        // 2. Rules
        List<DuplicateRule> rules = java.util.Arrays.asList(
                new DuplicateClassNameRule(),
                new DuplicateMethodSignatureRule(),
                new DuplicateMethodBodyRule(),
                new NearDuplicateMethodRule(0.80),
                new DuplicateClassBodyRule(),
                new SimilarityRule(0.85),
                new DuplicateLocatorRule()
        );

        // 3. Detect All
        List<Finding> allFindings = rules.stream()
                .flatMap(r -> r.detect(classes).stream())
                .collect(Collectors.toList());

        // DEBUG: Print all unique rule IDs found
        /*System.out.println("--- DEBUG: RULE IDS FOUND ---");
        allFindings.stream().map(Finding::ruleId).distinct().forEach(id -> System.out.println("ID: [" + id + "]"));
        System.out.println("-----------------------------");*/

        // 4. Split into Categories
        List<Finding> locatorFindings = allFindings.stream()
                .filter(f -> f.ruleId().equals("D7"))
                .collect(Collectors.toList());

        List<Finding> classFindings = allFindings.stream()
                .filter(f -> f.ruleId().equals("D1") || f.ruleId().equals("D2") || f.ruleId().equals("D5") || f.ruleId().equals("D6"))
                .collect(Collectors.toList());

        List<Finding> methodFindings = allFindings.stream()
                .filter(f -> f.ruleId().equals("D3") || f.ruleId().equals("D4") || f.ruleId().equals("D4b"))
                .collect(Collectors.toList());

        // 5. Generate Reports
        JsonReporter jsonReporter = new JsonReporter();
        HtmlReporter htmlReporter = new HtmlReporter();

        // Locators Report
        jsonReporter.write(out, "report-locators.json", locatorFindings);
        htmlReporter.write(out, "report-locators.html", "Duplicate Locators", locatorFindings);

        // Class Report
        jsonReporter.write(out, "report-classes.json", classFindings);
        htmlReporter.write(out, "report-classes.html", "Duplicate Classes", classFindings);

        // Methods Report
        jsonReporter.write(out, "report-methods.json", methodFindings);
        htmlReporter.write(out, "report-methods.html", "Duplicate Methods", methodFindings);

        // 6. Summary
        System.out.printf("Scanned %d classes. Found: %d Locators, %d Classes, %d Methods.%n",
                classes.size(), locatorFindings.size(), classFindings.size(), methodFindings.size());
    }
}