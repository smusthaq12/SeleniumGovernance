package com.ch.reviewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NearDuplicateMethodRule implements DuplicateRule {

    private final double threshold;

    public NearDuplicateMethodRule() {
        this(0.80); // Default 80% similarity
    }

    public NearDuplicateMethodRule(double threshold) {
        this.threshold = threshold;
    }

    @Override
    public String id() {
        return "D4b";
    }

    @Override
    public List<Finding> detect(List<ClassInfo> classes) {
        List<Finding> findings = new ArrayList<>();

        // Flatten all methods into a single list for comparison
        List<MethodContext> allMethods = new ArrayList<>();
        for (ClassInfo c : classes) {
            for (MethodInfo m : c.methods()) {
                // Ignore trivial/short methods (less than 5 lines/tokens)
                if (m.normalizedBody().split("\\s+").length < 5) {
                    continue;
                }
                allMethods.add(new MethodContext(c, m));
            }
        }

        // Compare every method against every other method
        for (int i = 0; i < allMethods.size(); i++) {
            for (int j = i + 1; j < allMethods.size(); j++) {
                MethodContext m1 = allMethods.get(i);
                MethodContext m2 = allMethods.get(j);

                // Skip if they are in the same class (D3/D4 handles internal class duplicates)
                if (m1.classInfo.className().equals(m2.classInfo.className())) {
                    continue;
                }

                // Skip if the exact hashes match (D4 already caught this)
                if (m1.methodInfo.bodyHash().equals(m2.methodInfo.bodyHash())) {
                    continue;
                }

                double similarity = calculateSimilarity(m1.methodInfo.normalizedBody(), m2.methodInfo.normalizedBody());

                // If they are highly similar but not exact duplicates
                if (similarity >= threshold) {
                    findings.add(new Finding(id(),
                            String.format("Near-duplicate method (%.0f%% similar) with %s.%s() on line %d",
                                    similarity * 100,
                                    m2.classInfo.className(),
                                    m2.methodInfo.name(),
                                    m2.methodInfo.startLine()), // Added line number here
                            m1.classInfo.sourceFile(),
                            m1.methodInfo.startLine(),
                            2));
                }
            }
        }

        return findings;
    }

    private double calculateSimilarity(String s1, String s2) {
        // Split into tokens (words/lines) using whitespace
        Set<String> set1 = new HashSet<>(Arrays.asList(s1.split("\\s+")));
        Set<String> set2 = new HashSet<>(Arrays.asList(s2.split("\\s+")));

        if (set1.isEmpty() || set2.isEmpty()) return 0.0;

        // Jaccard Similarity: (Intersection / Union)
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        return (double) intersection.size() / union.size();
    }

    // Helper class to keep track of which class a method belongs to
    private static class MethodContext {
        final ClassInfo classInfo;
        final MethodInfo methodInfo;

        MethodContext(ClassInfo classInfo, MethodInfo methodInfo) {
            this.classInfo = classInfo;
            this.methodInfo = methodInfo;
        }
    }
}