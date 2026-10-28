package com.ch.reviewer;

import java.util.List;

public class MethodInfo {
    private final String name;
    private final List<String> paramTypes;
    private final String returnType;
    private final String normalizedBody;
    private final String bodyHash;
    private final int startLine;

    public MethodInfo(String name, List<String> paramTypes, String returnType,
                      String normalizedBody, String bodyHash, int startLine) {
        this.name = name;
        this.paramTypes = paramTypes;
        this.returnType = returnType;
        this.normalizedBody = normalizedBody;
        this.bodyHash = bodyHash;
        this.startLine = startLine;
    }

    public String name() { return name; }
    public List<String> paramTypes() { return paramTypes; }
    public String returnType() { return returnType; }
    public String normalizedBody() { return normalizedBody; }
    public String bodyHash() { return bodyHash; }
    public int startLine() { return startLine; }
}