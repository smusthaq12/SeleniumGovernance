package com.ch.reviewer;

import java.util.List;

public interface DuplicateRule {
    String id();
    List<Finding> detect(List<ClassInfo> classes);
}