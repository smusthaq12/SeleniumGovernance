package com.ch.reviewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SimilarityRule implements DuplicateRule {

    private final double threshold;

    public SimilarityRule(double threshold) {
        this.threshold = threshold;
    }

    @Override
    public String id() {
        return "D6";
    }

    @Override
    public List<Finding> detect(List<ClassInfo> classes) {
        List<Finding> findings = new ArrayList<>();

        // Compare every class against every other class
        for (int i = 0; i < classes.size(); i++) {
            for (int j = i + 1; j < classes.size(); j++) {
                ClassInfo c1 = classes.get(i);
                ClassInfo c2 = classes.get(j);

                double similarity = calculateSimilarity(c1.normalizedClassBody(), c2.normalizedClassBody());

                if (similarity >= threshold) {
                    findings.add(new Finding(id(),
                            String.format("High similarity (%.0f%%) with %s", similarity * 100, c1.className()),
                            c2.sourceFile(), c2.startLine(), 2));
                }
            }
        }
        return findings;
    }

    private double calculateSimilarity(String s1, String s2) {
        Set<String> set1 = new HashSet<>(Arrays.asList(s1.split("\\s+")));
        Set<String> set2 = new HashSet<>(Arrays.asList(s2.split("\\s+")));

        if (set1.isEmpty() && set2.isEmpty()) return 1.0;

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        return (double) intersection.size() / union.size();
    }
}