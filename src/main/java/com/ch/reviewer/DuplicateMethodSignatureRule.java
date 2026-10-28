package com.ch.reviewer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DuplicateMethodSignatureRule implements DuplicateRule {

    @Override
    public String id() {
        return "D3";
    }

    @Override
    public List<Finding> detect(List<ClassInfo> classes) {
        List<Finding> findings = new ArrayList<>();

        for (ClassInfo c : classes) {
            Set<String> seenSignatures = new HashSet<>();
            for (MethodInfo m : c.methods()) {
                // Signature = name + param types
                String signature = m.name() + "(" + String.join(",", m.paramTypes()) + ")";

                if (!seenSignatures.add(signature)) {
                    findings.add(new Finding(id(),
                            "Duplicate method signature: " + signature,
                            c.sourceFile(), m.startLine(), 2));
                }
            }
        }
        return findings;
    }
}