package com.ch.reviewer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DuplicateMethodBodyRule implements DuplicateRule {

    @Override
    public String id() {
        return "D4";
    }

    @Override
    public List<Finding> detect(List<ClassInfo> classes) {
        List<Finding> findings = new ArrayList<>();
        Map<String, Finding> firstSeen = new HashMap<>();

        for (ClassInfo c : classes) {
            for (MethodInfo m : c.methods()) {
                String body = m.normalizedBody();

                if (body.isEmpty() || body.length() < 20) {
                    continue;
                }

                if (isTrivialAccessor(m.name(), body)) {
                    continue;
                }

                String hash = m.bodyHash();
                if (firstSeen.containsKey(hash)) {
                    Finding original = firstSeen.get(hash);

                    // THE FIX: Extracting the filename and appending the original line number
                    String originalFileName = original.file().substring(original.file().lastIndexOf("\\") + 1);

                    findings.add(new Finding(id(),
                            "Duplicate method body matches " + originalFileName + " (Line " + original.line() + ")",
                            c.sourceFile(), m.startLine(), 2));
                } else {
                    firstSeen.put(hash, new Finding(id(), "Tracked", c.sourceFile(), m.startLine(), 1));
                }
            }
        }
        return findings;
    }

    // Updated helper method to check BOTH name and body
    private boolean isTrivialAccessor(String methodName, String body) {
        String lowerName = methodName.toLowerCase();

        // Rule A: Ignore by Name (Getters, Setters, Booleans)
        if (lowerName.startsWith("get") || lowerName.startsWith("set") || lowerName.startsWith("is")) {
            return true;
        }

        // Rule B: Ignore by Body (Foolproof string matching)
        String compact = body.replaceAll("\\s+", "");

        // Ignore "return null;"
        if (compact.equals("returnnull;")) {
            return true;
        }

        // Ignore "return this.fieldName;" or "return fieldName;"
        if (compact.startsWith("return") && compact.endsWith(";")) {
            // If it's just returning a variable with no method calls or math
            if (!compact.contains("(") && !compact.contains("\"") && !compact.contains("+") && !compact.contains("-")) {
                return true;
            }
        }

        return false;
    }
}