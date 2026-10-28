package com.ch.reviewer;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Finding {
    private final String ruleId;
    private final String message;
    private final String file;
    private final int line;
    private final int occurrences;

    public Finding(String ruleId, String message, String file, int line, int occurrences) {
        this.ruleId = ruleId;
        this.message = message;
        this.file = file;
        this.line = line;
        this.occurrences = occurrences;
    }

    @JsonProperty("ruleId")
    public String ruleId() { return ruleId; }

    @JsonProperty("message")
    public String message() { return message; }

    @JsonProperty("file")
    public String file() { return file; }

    @JsonProperty("line")
    public int line() { return line; }

    @JsonProperty("occurrences")
    public int occurrences() { return occurrences; }
}