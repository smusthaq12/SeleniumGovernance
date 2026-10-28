package com.ch.reviewer;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Mojo(name = "scan", defaultPhase = LifecyclePhase.VERIFY)
public class GovernanceMojo extends AbstractMojo {

    // This injects the current project's base directory into the plugin
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;
    @Override
    public void execute() throws MojoExecutionException {
        getLog().info("Starting Codebase Governance Agent...");

        try {
            // 1. Get absolute paths
            String baseDir = project.getBasedir().getAbsolutePath();
            Path root = java.nio.file.Paths.get(baseDir, "src", "main", "java");
            Path out = java.nio.file.Paths.get(baseDir, "target");

            // 2. Ensure the target directory exists
            if (!java.nio.file.Files.exists(out)) {
                java.nio.file.Files.createDirectories(out);
            }

            getLog().info("Scanning directory: " + root.toString());

            // 3. Scan & Extract
            List<Path> files = new SourceScanner().scan(root);
            JavaSourceParser parser = new JavaSourceParser();
            ClassInfoExtractor extractor = new ClassInfoExtractor();

            List<ClassInfo> classes = files.stream()
                    .map(parser::parse)
                    .filter(java.util.Optional::isPresent)
                    .map(java.util.Optional::get)
                    .map(extractor::extract)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());

            // 4. Rules
            List<DuplicateRule> rules = java.util.Arrays.asList(
                    new DuplicateClassNameRule(),
                    new DuplicateMethodSignatureRule(),
                    new DuplicateMethodBodyRule(),
                    new NearDuplicateMethodRule(0.80),
                    new DuplicateClassBodyRule(),
                    new SimilarityRule(0.85),
                    new DuplicateLocatorRule()
            );

            // 5. Detect
            List<Finding> allFindings = rules.stream()
                    .flatMap(r -> r.detect(classes).stream())
                    .collect(Collectors.toList());

            // 6. Split into Categories
            List<Finding> locatorFindings = allFindings.stream()
                    .filter(f -> f.ruleId().equals("D7"))
                    .collect(Collectors.toList());

            List<Finding> classFindings = allFindings.stream()
                    .filter(f -> java.util.Arrays.asList("D1", "D2", "D5", "D6").contains(f.ruleId()))
                    .collect(Collectors.toList());

            List<Finding> methodFindings = allFindings.stream()
                    .filter(f -> java.util.Arrays.asList("D3", "D4", "D4b").contains(f.ruleId()))
                    .collect(Collectors.toList());

            // 7. Generate Reports
            JsonReporter jsonReporter = new JsonReporter();
            HtmlReporter htmlReporter = new HtmlReporter();

            // Locators Report
            jsonReporter.write(out, "report-locators.json", locatorFindings);
            htmlReporter.write(out, "report-locators.html", "Duplicate Locators", locatorFindings);

            // Class Report
            jsonReporter.write(out, "report-classes.json", classFindings);
            htmlReporter.write(out, "report-classes.html", "Duplicate Classes", classFindings);

            // Methods Report
            jsonReporter.write(out, "report-methods.json", methodFindings);
            htmlReporter.write(out, "report-methods.html", "Duplicate Methods", methodFindings);

            // 8. Summary
            getLog().info("Scanned " + classes.size() + " classes.");
            getLog().info("Found: " + locatorFindings.size() + " Locators, " + classFindings.size() + " Classes, " + methodFindings.size() + " Methods.");
            getLog().info("Reports generated in: " + out.toString());

        } catch (Exception e) {
            throw new MojoExecutionException("Agent failed to execute", e);
        }
    }

}