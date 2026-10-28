package com.ch.reviewer;

import java.util.List;
import java.util.stream.Collectors;

public class DuplicateClassNameRule implements DuplicateRule {
    @Override
    public String id() { return "D1"; }

    @Override
    public List<Finding> detect(List<ClassInfo> all) {
        if (all == null || all.isEmpty()) return new java.util.ArrayList<>();

        return all.stream()
                .filter(java.util.Objects::nonNull) // <--- ADD THIS HERE
                .collect(java.util.stream.Collectors.groupingBy(ClassInfo::className))
                .entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .flatMap(e -> e.getValue().stream()
                        .map(c -> new Finding(id(),
                                "Duplicate class name: " + c.className(),
                                c.sourceFile(), c.startLine(), e.getValue().size())))
                .collect(java.util.stream.Collectors.toList());
    }
}