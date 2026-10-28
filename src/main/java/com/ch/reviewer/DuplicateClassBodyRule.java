package com.ch.reviewer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DuplicateClassBodyRule implements DuplicateRule {

    @Override
    public String id() {
        return "D5";
    }

    @Override
    public List<Finding> detect(List<ClassInfo> classes) {
        List<Finding> findings = new ArrayList<>();
        Map<String, ClassInfo> firstSeen = new HashMap<>();

        for (ClassInfo c : classes) {
            String hash = c.classBodyHash();

            if (firstSeen.containsKey(hash)) {
                ClassInfo original = firstSeen.get(hash);
                findings.add(new Finding(id(),
                        "Class body identical to " + original.className(),
                        c.sourceFile(), c.startLine(), 2));
            } else {
                firstSeen.put(hash, c);
            }
        }
        return findings;
    }
}