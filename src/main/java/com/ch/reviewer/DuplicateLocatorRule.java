package com.ch.reviewer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DuplicateLocatorRule implements DuplicateRule {

    @Override
    public String id() {
        return "D7";
    }

    @Override
    public List<Finding> detect(List<ClassInfo> classes) {
        List<Finding> findings = new ArrayList<>();
        Map<String, Finding> firstSeen = new HashMap<>();

        String regex = "By\\.(xpath|id|name|cssSelector|className|accessibilityId)\\s*\\(\\s*\"([^\"]+)\"";
        Pattern pattern = Pattern.compile(regex);

        for (ClassInfo c : classes) {
            Matcher matcher = pattern.matcher(c.normalizedClassBody());

           /* while (matcher.find()) {
                String locatorType = matcher.group(1);
                String locatorValue = matcher.group(2);

                String key = locatorType + ":" + locatorValue;

                if (firstSeen.containsKey(key)) {
                    Finding original = firstSeen.get(key);

                    // Extract just the filename from the full path
                    String originalFileName = original.file().substring(original.file().lastIndexOf("\\") + 1);

                    // Add the original line number to the message
                    findings.add(new Finding(id(),
                            "Duplicate locator [" + key + "] also found in " + originalFileName + " (Line " + original.line() + ")",
                            c.sourceFile(),
                            c.startLine(),
                            2));
                } else {
                    firstSeen.put(key, new Finding(id(), c.sourceFile(), c.sourceFile(), c.startLine(), 1));
                }
            }*/
            while (matcher.find()) {
                String locatorType = matcher.group(1);
                String locatorValue = matcher.group(2);

                // 1. Ignore if the regex accidentally catches an empty or whitespace string
                if (locatorValue == null || locatorValue.trim().isEmpty()) {
                    continue;
                }

                // 2. Ignore locators that are actually the interface implementation string
                if (locatorValue.contains("SupplyMobileLocatorInfo")) {
                    continue;
                }

                String key = locatorType + ":" + locatorValue;

                if (firstSeen.containsKey(key)) {
                    Finding original = firstSeen.get(key);
                    String originalFileName = original.file().substring(original.file().lastIndexOf("\\") + 1);

                    findings.add(new Finding(id(),
                            "Duplicate locator [" + key + "] also found in " + originalFileName + " (Line " + original.line() + ")",
                            c.sourceFile(),
                            c.startLine(),
                            2));
                } else {
                    firstSeen.put(key, new Finding(id(), c.sourceFile(), c.sourceFile(), c.startLine(), 1));
                }
            }
        }
        return findings;
    }
}
/*package com.mobileappsautomationsuite.reviewer;

            import java.util.ArrayList;
            import java.util.HashMap;
            import java.util.List;
            import java.util.Map;
            import java.util.regex.Matcher;
            import java.util.regex.Pattern;

public class DuplicateLocatorRule implements DuplicateRule {

    @Override
    public String id() {
        return "D7";
    }

    @Override
    public List<Finding> detect(List<ClassInfo> classes) {
        List<Finding> findings = new ArrayList<>();
        Map<String, Finding> firstSeen = new HashMap<>();

        String regex = "By\\.(xpath|id|name|cssSelector|className|accessibilityId)\\s*\\(\\s*\"([^\"]+)\"";
        Pattern pattern = Pattern.compile(regex);

        for (ClassInfo c : classes) {
            // IGNORE RULE: Skip any class that ends with _OR (Object Repository)
            if (c.className().endsWith("_OR")) {
                continue;
            }

            Matcher matcher = pattern.matcher(c.normalizedClassBody());

            while (matcher.find()) {
                String locatorType = matcher.group(1);
                String locatorValue = matcher.group(2);

                if (locatorValue == null || locatorValue.trim().isEmpty()) {
                    continue;
                }

                String key = locatorType + ":" + locatorValue;

                if (firstSeen.containsKey(key)) {
                    Finding original = firstSeen.get(key);
                    String originalFileName = original.file().substring(original.file().lastIndexOf("\\") + 1);

                    findings.add(new Finding(id(),
                            "Duplicate locator [" + key + "] also found in " + originalFileName + " (Line " + original.line() + ")",
                            c.sourceFile(),
                            c.startLine(),
                            2));
                } else {
                    firstSeen.put(key, new Finding(id(), c.sourceFile(), c.sourceFile(), c.startLine(), 1));
                }
            }
        }
        return findings;
    }
}*/