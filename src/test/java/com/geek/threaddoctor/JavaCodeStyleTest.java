package com.geek.threaddoctor;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class JavaCodeStyleTest {
    private static final Path SOURCE_ROOT = Path.of("src/main/java");
    private static final String COPYRIGHT = "zhiwu Technologies Co., Ltd. All rights reserved.";
    private static final Pattern COPYRIGHT_PACKAGE_GAP = Pattern.compile(
            "\\A/\\*\\*\\R \\* Copyright &copy; \\d{4}-\\d{4} zhiwu Technologies Co\\., Ltd\\. All rights reserved\\.\\R \\*/\\R\\Rpackage ");
    private static final Pattern BLANK_JAVADOC_LINE = Pattern.compile("(?m)^\\s*$");
    private static final Pattern CJK_TEXT = Pattern.compile("\\p{IsHan}");
    private static final Pattern PLACEHOLDER_JAVADOC_TEXT = Pattern.compile(
            "(?m)\\b(?:Handles|Executes)\\s+\\w+\\.|@param\\s+\\w+\\s+\\w+\\s+value$|@throws\\s+\\w+\\s+\\w+\\s+thrown$");
    private static final Pattern PUBLIC_TYPE = Pattern.compile(
            "(?m)^(\\s*)(?:@[\\w.]+(?:\\([^\\n]*\\))?\\s*\\n\\s*)*public\\s+(class|interface|enum|record)\\s+(\\w+)");
    private static final Pattern PUBLIC_METHOD = Pattern.compile(
            "(?ms)^(\\s*)(?:@[\\w.]+(?:\\([^\\n]*\\))?\\s*\\n\\s*)*public\\s+((?!(?:class|interface|enum|record)\\s).*?(?:\\{|;))");
    private static final Pattern PARAM_TAG = Pattern.compile("(?m)^\\s*\\*\\s*@param\\s+(\\w+)\\s+\\S+.*$");
    private static final Pattern RETURN_TAG = Pattern.compile("(?m)^\\s*\\*\\s*@return\\s+\\S+.*$");
    private static final Pattern THROWS_TAG = Pattern.compile("(?m)^\\s*\\*\\s*@throws\\s+(\\w+)\\s+\\S+.*$");

    @Test
    void maintainedJavaSourcesHaveCopyrightAndPublicJavadocs() throws IOException {
        List<String> violations = new ArrayList<>();
        for (Path source : javaSources()) {
            String content = Files.readString(source);
            String label = SOURCE_ROOT.relativize(source).toString();
            if (!content.startsWith("/**") || !content.substring(0, Math.min(content.length(), 180)).contains(COPYRIGHT)) {
                violations.add(label + ": missing copyright header before package declaration");
            }
            if (!COPYRIGHT_PACKAGE_GAP.matcher(content).find()) {
                violations.add(label + ": copyright header must be followed by exactly one blank line before package declaration");
            }
            verifyPublicTypes(label, content, violations);
            verifyPublicMethods(label, content, violations);
        }
        assertThat(violations).isEmpty();
    }

    private List<Path> javaSources() throws IOException {
        try (var stream = Files.walk(SOURCE_ROOT)) {
            return stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .sorted()
                    .toList();
        }
    }

    private void verifyPublicTypes(String label, String content, List<String> violations) {
        Matcher matcher = PUBLIC_TYPE.matcher(content);
        while (matcher.find()) {
            String javadoc = javadocBefore(content, matcher.start());
            String typeName = matcher.group(3);
            if (javadoc == null) {
                violations.add(label + ": public type " + typeName + " is missing Javadoc");
                continue;
            }
            if (!javadoc.contains("@author ") || !javadoc.contains("@since ")) {
                violations.add(label + ": public type " + typeName + " Javadoc must include @author and @since");
            }
            if (summaryLine(javadoc).isBlank()) {
                violations.add(label + ": public type " + typeName + " Javadoc must include a summary");
            }
            rejectBlankJavadocLines(label, typeName, javadoc, violations);
            verifyTagSeparator(label, typeName, javadoc, violations);
            verifyChineseDescriptions(label, typeName, javadoc, violations);
            rejectInvalidTags(label, typeName, javadoc, violations);
        }
    }

    private void verifyPublicMethods(String label, String content, List<String> violations) {
        String typeName = firstPublicTypeName(content);
        Matcher matcher = PUBLIC_METHOD.matcher(content);
        while (matcher.find()) {
            MethodSignature signature = MethodSignature.parse(matcher.group(2), typeName);
            if (signature == null) {
                continue;
            }
            String javadoc = javadocBefore(content, matcher.start());
            if (javadoc == null) {
                violations.add(label + ": public method " + signature.name() + " is missing Javadoc");
                continue;
            }
            rejectBlankJavadocLines(label, signature.name(), javadoc, violations);
            verifyTagSeparator(label, signature.name(), javadoc, violations);
            verifyChineseDescriptions(label, signature.name(), javadoc, violations);
            rejectInvalidTags(label, signature.name(), javadoc, violations);
            for (String parameter : signature.parameters()) {
                if (!hasTag(PARAM_TAG, javadoc, parameter)) {
                    violations.add(label + ": public method " + signature.name() + " missing @param description for " + parameter);
                }
            }
            if (signature.requiresReturnTag() && !RETURN_TAG.matcher(javadoc).find()) {
                violations.add(label + ": public method " + signature.name() + " missing @return description");
            }
            for (String thrown : signature.thrownTypes()) {
                if (!hasTag(THROWS_TAG, javadoc, thrown)) {
                    violations.add(label + ": public method " + signature.name() + " missing @throws description for " + thrown);
                }
            }
        }
    }

    private String firstPublicTypeName(String content) {
        Matcher matcher = PUBLIC_TYPE.matcher(content);
        return matcher.find() ? matcher.group(3) : "";
    }

    private String javadocBefore(String content, int declarationStart) {
        String prefix = content.substring(0, declarationStart).stripTrailing();
        int end = prefix.lastIndexOf("*/");
        if (end < 0 || end != prefix.length() - 2) {
            return null;
        }
        int start = prefix.lastIndexOf("/**", end);
        return start < 0 ? null : prefix.substring(start, end + 2);
    }

    private String summaryLine(String javadoc) {
        return javadoc.lines()
                .map(line -> line.replaceFirst("^\\s*\\*\\s?", "").trim())
                .filter(line -> !line.isBlank())
                .filter(line -> !line.startsWith("/**"))
                .filter(line -> !line.startsWith("@"))
                .findFirst()
                .orElse("");
    }

    private void rejectInvalidTags(String label, String member, String javadoc, List<String> violations) {
        if (javadoc.contains("@invalidTag")) {
            violations.add(label + ": " + member + " Javadoc contains invalid tag");
        }
        if (PLACEHOLDER_JAVADOC_TEXT.matcher(javadoc).find()) {
            violations.add(label + ": " + member + " Javadoc contains placeholder text");
        }
    }

    private void rejectBlankJavadocLines(String label, String member, String javadoc, List<String> violations) {
        if (BLANK_JAVADOC_LINE.matcher(javadoc).find()) {
            violations.add(label + ": " + member + " Javadoc must not contain real blank lines");
        }
    }

    private void verifyTagSeparator(String label, String member, String javadoc, List<String> violations) {
        List<String> lines = javadoc.lines().toList();
        for (int index = 1; index < lines.size(); index++) {
            String line = lines.get(index);
            if (!line.matches("\\s*\\*\\s+@\\w+\\b.*")) {
                continue;
            }
            if (!lines.get(index - 1).matches("\\s*\\*\\s*")) {
                violations.add(label + ": " + member + " Javadoc must separate description and tags with a blank star line");
                return;
            }
            return;
        }
    }

    private void verifyChineseDescriptions(String label, String member, String javadoc, List<String> violations) {
        for (String line : javadoc.lines().toList()) {
            String raw = line.trim();
            if (raw.equals("/**") || raw.equals("*/")) {
                continue;
            }
            String text = line.replaceFirst("^\\s*\\*\\s?", "").trim();
            if (text.isBlank() || text.startsWith("@author") || text.startsWith("@since")) {
                continue;
            }
            if (text.contains("?")) {
                violations.add(label + ": " + member + " Javadoc description contains replacement question marks");
                return;
            }
            String description = text.replaceFirst("^@(param|return|throws)\\s+\\S+\\s+", "");
            if (!CJK_TEXT.matcher(description).find()) {
                violations.add(label + ": " + member + " Javadoc description must be written in Chinese");
                return;
            }
        }
    }

    private boolean hasTag(Pattern pattern, String javadoc, String name) {
        Matcher matcher = pattern.matcher(javadoc);
        while (matcher.find()) {
            if (matcher.group(1).equals(name)) {
                return true;
            }
        }
        return false;
    }

    private record MethodSignature(String name, String returnType, List<String> parameters, List<String> thrownTypes) {
        static MethodSignature parse(String signature, String typeName) {
            String compact = signature.replaceAll("\\s+", " ").replaceAll("[{;]\\s*$", "").trim();
            if (!compact.contains("(") || !compact.contains(")")) {
                return null;
            }
            String before = stripGenerics(compact.substring(0, compact.indexOf('('))).trim();
            String params = compact.substring(compact.indexOf('(') + 1, compact.lastIndexOf(')')).trim();
            String after = compact.substring(compact.lastIndexOf(')') + 1).trim();
            String[] tokens = before.split("\\s+");
            if (tokens.length == 0) {
                return null;
            }
            String name = tokens[tokens.length - 1];
            if (List.of("if", "for", "while", "switch", "catch").contains(name)) {
                return null;
            }
            boolean constructor = name.equals(typeName);
            String returnType = constructor || tokens.length < 2 ? "" : tokens[tokens.length - 2];
            List<String> thrown = after.startsWith("throws ")
                    ? splitTopLevel(after.substring("throws ".length())).stream().map(MethodSignature::lastToken).toList()
                    : List.of();
            return new MethodSignature(name, returnType, splitTopLevel(params).stream()
                    .map(MethodSignature::parameterName)
                    .filter(value -> !value.isBlank())
                    .toList(), thrown);
        }

        boolean requiresReturnTag() {
            return !returnType.isBlank() && !"void".equals(returnType);
        }

        private static String parameterName(String parameter) {
            String cleaned = parameter.replaceAll("@[\\w.]+(?:\\([^)]*\\))?\\s*", "").replace("final ", "").trim();
            return lastToken(cleaned).replace("...", "").replace("[]", "");
        }

        private static String lastToken(String value) {
            String[] tokens = value.trim().split("\\s+");
            return tokens.length == 0 ? "" : tokens[tokens.length - 1];
        }

        private static String stripGenerics(String value) {
            StringBuilder result = new StringBuilder();
            int depth = 0;
            for (char ch : value.toCharArray()) {
                if (ch == '<') {
                    depth++;
                } else if (ch == '>') {
                    depth = Math.max(0, depth - 1);
                } else if (depth == 0) {
                    result.append(ch);
                }
            }
            return result.toString();
        }

        private static List<String> splitTopLevel(String value) {
            if (value == null || value.isBlank()) {
                return List.of();
            }
            List<String> parts = new ArrayList<>();
            StringBuilder current = new StringBuilder();
            int depth = 0;
            for (char ch : value.toCharArray()) {
                if (ch == '<') {
                    depth++;
                } else if (ch == '>') {
                    depth = Math.max(0, depth - 1);
                } else if (ch == ',' && depth == 0) {
                    parts.add(current.toString().trim());
                    current.setLength(0);
                    continue;
                }
                current.append(ch);
            }
            if (!current.isEmpty()) {
                parts.add(current.toString().trim());
            }
            return parts;
        }
    }
}
