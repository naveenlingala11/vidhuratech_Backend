package com.vidhuratech.jobs.student.service;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class CodeSecurityValidator {

    private static final int MAX_SOURCE_CHARS = 20000;
    private static final int MAX_SOURCE_LINES = 600;

    public void validate(String language, String sourceCode) {
        String code = sourceCode == null ? "" : sourceCode;

        if (code.trim().isEmpty()) {
            throw new RuntimeException("Source code is required");
        }

        if (code.length() > MAX_SOURCE_CHARS) {
            throw new RuntimeException("Code is too large. Maximum " + MAX_SOURCE_CHARS + " characters allowed.");
        }

        if (code.split("\\R", -1).length > MAX_SOURCE_LINES) {
            throw new RuntimeException("Code has too many lines. Maximum " + MAX_SOURCE_LINES + " lines allowed.");
        }

        if (containsControlCharacters(code)) {
            throw new RuntimeException("Code contains invalid control characters.");
        }

        if (looksLikeShellCommand(code)) {
            throw new RuntimeException("Shell commands are not allowed in code editor.");
        }
    }

    private String normalizeLanguage(String language) {
        String value = language == null
                ? ""
                : language.trim().toUpperCase(Locale.ROOT).replace("-", "").replace("_", "");

        return switch (value) {
            case "C++", "CPLUSPLUS" -> "CPP";
            case "C#", "CS" -> "CSHARP";
            case "F#", "FS" -> "FSHARP";
            case "TS" -> "TYPESCRIPT";
            default -> value;
        };
    }

    private boolean containsControlCharacters(String code) {
        for (int i = 0; i < code.length(); i++) {
            char ch = code.charAt(i);

            if (ch == '\n' || ch == '\r' || ch == '\t') {
                continue;
            }

            if (Character.isISOControl(ch)) {
                return true;
            }
        }

        return false;
    }

    private boolean looksLikeShellCommand(String code) {
        String trimmed = code == null ? "" : code.trim();

        if (trimmed.isBlank()) {
            return false;
        }

        String firstLine = trimmed.lines()
                .findFirst()
                .orElse("")
                .trim();
        String firstLineLower = firstLine.toLowerCase(Locale.ROOT);

        // If it's a comment or common programming starting construct, it's not a shell command
        List<String> codeStarters = List.of(
            "import ", "from ", "package ", "using ", "public ", "class ", "private ", 
            "protected ", "void ", "int ", "float ", "double ", "char ", "bool ", "boolean ",
            "let ", "const ", "var ", "function ", "def ", "namespace ", 
            "#include", "include ", "struct ", "enum ", "//", "/*", "*", "<?php", "<?",
            "const", "let", "var", "def", "import", "from"
        );

        boolean startsWithCodeKeyword = codeStarters.stream().anyMatch(starter -> {
            if (starter.startsWith("#") || starter.startsWith("//") || starter.startsWith("/*") || starter.startsWith("*")) {
                return firstLine.startsWith(starter);
            }
            return firstLineLower.startsWith(starter);
        });

        if (startsWithCodeKeyword) {
            return false;
        }

        // Shell command pattern matching actual commands
        boolean isShell = firstLineLower.matches("^(find|cat|ls|pwd|whoami|id|uname|ps|env|printenv|curl|wget|nc|netcat|bash|sh|zsh|python|python3|perl|ruby|chmod|chown|rm|mv|cp|mkdir|rmdir|touch|grep|egrep|fgrep|sed|awk|tar|zip|unzip|git|docker|kubectl|systemctl|service|apt|yum|dnf|pacman|pip|npm|yarn|npx)\\b.*")
                || firstLineLower.startsWith("./")
                || firstLineLower.startsWith("/")
                || firstLineLower.startsWith("../");

        if (isShell) {
            return true;
        }

        // Suspicious file paths or redirection operators on the first line
        List<String> suspicious = List.of("/etc/passwd", "/proc/", "/root/", "2>/dev/null", "> /dev/null");
        if (suspicious.stream().anyMatch(firstLineLower::contains)) {
            return true;
        }

        return false;
    }

    private static Pattern p(String regex) {
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    }

    private String readable(String pattern) {
        return pattern
                .replace("\\b", "")
                .replace("\\s*", " ")
                .replace("\\s+", " ")
                .replace("\\(", "(")
                .replace("\\.", ".")
                .replace("\\s", " ")
                .replace("\\", "")
                .trim();
    }
}