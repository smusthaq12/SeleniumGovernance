package com.ch.reviewer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/*public class JsonReporter {
    public void write(Path out, List<Finding> findings) throws IOException {
        ObjectMapper mapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT);
        mapper.writeValue(out.toFile(), findings);
    }*/

public class JsonReporter {
    // Added a 'filename' parameter
    public void write(Path outDir, String filename, List<Finding> findings) throws IOException {
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        mapper.writeValue(outDir.resolve(filename).toFile(), findings);
    }
}