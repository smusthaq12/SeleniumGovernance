package com.ch.reviewer;

import java.util.List;

public class ClassInfo {
    private final String packageName;
    private final String className;
    private final String fqn;
    private final String sourceFile;
    private final int startLine;
    private final List<MethodInfo> methods;
    private final String normalizedClassBody;
    private final String classBodyHash;

    public ClassInfo(String packageName, String className, String fqn, String sourceFile,
                     int startLine, List<MethodInfo> methods, String normalizedClassBody, String classBodyHash) {
        this.packageName = packageName;
        this.className = className;
        this.fqn = fqn;
        this.sourceFile = sourceFile;
        this.startLine = startLine;
        this.methods = methods;
        this.normalizedClassBody = normalizedClassBody;
        this.classBodyHash = classBodyHash;
    }

    public String packageName() { return packageName; }
    public String className() { return className; }
    public String fqn() { return fqn; }
    public String sourceFile() { return sourceFile; }
    public int startLine() { return startLine; }
    public List<MethodInfo> methods() { return methods; }
    public String normalizedClassBody() { return normalizedClassBody; }
    public String classBodyHash() { return classBodyHash; }
}