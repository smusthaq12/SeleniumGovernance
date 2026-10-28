package com.ch.reviewer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class HtmlReporter {
    // Added 'title' and 'filename' parameters
    public void write(Path outDir, String filename, String title, List<Finding> findings) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='utf-8'>")
                .append("<title>").append(title).append("</title></head><body>")
                .append("<h1>").append(title).append(" (").append(findings.size()).append(")</h1>")
                .append("<table border='1' cellpadding='6'>")
                .append("<tr><th>Rule</th><th>Message</th><th>File</th><th>Line</th><th>Count</th></tr>");

        for (Finding f : findings) {
            sb.append("<tr>")
                    .append("<td>").append(f.ruleId()).append("</td>")
                    .append("<td>").append(f.message()).append("</td>")
                    .append("<td>").append(f.file()).append("</td>")
                    .append("<td>").append(f.line()).append("</td>")
                    .append("<td>").append(f.occurrences()).append("</td>")
                    .append("</tr>");
        }
        sb.append("</table></body></html>");
        Files.write(outDir.resolve(filename), sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}