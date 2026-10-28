package com.ch.reviewer;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

public class ClassInfoExtractor {

    public ClassInfo extract(CompilationUnit cu) {
        String className = null;
        String packageName = cu.getPackageDeclaration().map(pd -> pd.getNameAsString()).orElse("default");
        int startLine = 0;
        String normalizedClassBody = "";
        String classBodyHash = "";
        java.util.List<MethodInfo> methods = new java.util.ArrayList<>();

        // 1. Try to extract as a standard Class
        java.util.Optional<ClassOrInterfaceDeclaration> classDeclOpt = cu.findFirst(ClassOrInterfaceDeclaration.class);
        if (classDeclOpt.isPresent() && !classDeclOpt.get().isInterface()) {
            ClassOrInterfaceDeclaration classDecl = classDeclOpt.get();
            className = classDecl.getNameAsString();
            startLine = classDecl.getBegin().map(p -> p.line).orElse(0);
            methods = classDecl.getMethods().stream()
                    .map(this::extractMethodInfo)
                    .collect(java.util.stream.Collectors.toList());

            // FIX: Remove the class name before normalizing and hashing
            String rawBody = classDecl.toString().replaceAll(classDecl.getNameAsString(), "CLASS_NAME");
            normalizedClassBody = normalize(rawBody);

        }
        // 2. Try to extract as an Enum
        else {
            java.util.Optional<com.github.javaparser.ast.body.EnumDeclaration> enumDeclOpt = cu.findFirst(com.github.javaparser.ast.body.EnumDeclaration.class);
            if (enumDeclOpt.isPresent()) {
                com.github.javaparser.ast.body.EnumDeclaration enumDecl = enumDeclOpt.get();
                className = enumDecl.getNameAsString();
                startLine = enumDecl.getBegin().map(p -> p.line).orElse(0);
                methods = enumDecl.getMethods().stream()
                        .map(this::extractMethodInfo)
                        .collect(java.util.stream.Collectors.toList());

                // FIX: Remove the enum name before normalizing and hashing
                String rawBody = enumDecl.toString().replaceAll(enumDecl.getNameAsString(), "CLASS_NAME");
                normalizedClassBody = normalize(rawBody);
            }
        }

        // 3. If it's an interface or empty file, skip it
        if (className == null) {
            return null;
        }

        String fqn = packageName + "." + className;
        String sourceFile = cu.getStorage().map(s -> s.getPath().toString()).orElse("unknown");
        classBodyHash = sha256(normalizedClassBody);

        return new ClassInfo(packageName, className, fqn, sourceFile, startLine, methods, normalizedClassBody, classBodyHash);
    }

    private MethodInfo extractMethodInfo(MethodDeclaration method) {
        String name = method.getNameAsString();
        List<String> paramTypes = method.getParameters().stream()
                .map(p -> p.getTypeAsString())
                .collect(java.util.stream.Collectors.toList());
        String returnType = method.getTypeAsString();

        String normalizedBody = normalize(method.getBody().map(Object::toString).orElse(""));
        String bodyHash = sha256(normalizedBody);
        int startLine = method.getBegin().map(p -> p.line).orElse(0);

        return new MethodInfo(name, paramTypes, returnType, normalizedBody, bodyHash, startLine);
    }

    private String normalize(String body) {
        return body.replaceAll("//.*|/\\*.*?\\*/", "") // Strip comments
                .replaceAll("\\s+", " ")               // Collapse whitespace
                .trim();
    }

    private String sha256(String s) {
        try {
            byte[] h = MessageDigest.getInstance("SHA-256").digest(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : h) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return "hash-error";
        }
    }
}