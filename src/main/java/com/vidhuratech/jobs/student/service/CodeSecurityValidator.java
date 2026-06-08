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

    private static final Map<String, List<Pattern>> BLOCKED_PATTERNS = Map.ofEntries(
            Map.entry("PYTHON", List.of(
                    p("\\bimport\\s+os\\b"),
                    p("\\bfrom\\s+os\\s+import\\b"),
                    p("\\bimport\\s+subprocess\\b"),
                    p("\\bfrom\\s+subprocess\\s+import\\b"),
                    p("\\bimport\\s+socket\\b"),
                    p("\\bfrom\\s+socket\\s+import\\b"),
                    p("\\bimport\\s+requests\\b"),
                    p("\\bimport\\s+urllib\\b"),
                    p("\\bfrom\\s+urllib\\s+import\\b"),
                    p("\\bimport\\s+pathlib\\b"),
                    p("\\bfrom\\s+pathlib\\s+import\\b"),
                    p("\\bimport\\s+shutil\\b"),
                    p("\\bfrom\\s+shutil\\s+import\\b"),
                    p("\\bimport\\s+pickle\\b"),
                    p("\\bfrom\\s+pickle\\s+import\\b"),
                    p("\\bopen\\s*\\("),
                    p("\\beval\\s*\\("),
                    p("\\bexec\\s*\\("),
                    p("\\bcompile\\s*\\("),
                    p("\\b__import__\\s*\\("),
                    p("\\bglobals\\s*\\("),
                    p("\\blocals\\s*\\("),
                    p("\\bvars\\s*\\("),
                    p("\\bsys\\s*\\.\\s*modules\\b"),
                    p("\\bsys\\s*\\.\\s*path\\b"),
                    p("\\bsys\\s*\\.\\s*argv\\b"),
                    p("\\bsys\\s*\\.\\s*exit\\s*\\("),
                    p("__dict__"),
                    p("__class__"),
                    p("__mro__"),
                    p("__subclasses__")
            )),
            Map.entry("JAVA", List.of(
                    p("\\bimport\\s+java\\.io\\.File\\b"),
                    p("\\bimport\\s+java\\.io\\.FileInputStream\\b"),
                    p("\\bimport\\s+java\\.io\\.FileOutputStream\\b"),
                    p("\\bimport\\s+java\\.io\\.RandomAccessFile\\b"),
                    p("\\bimport\\s+java\\.nio\\.file\\."),
                    p("\\bimport\\s+java\\.net\\."),
                    p("\\bimport\\s+java\\.lang\\.reflect\\."),
                    p("\\bRuntime\\s*\\.\\s*getRuntime\\s*\\("),
                    p("\\bProcessBuilder\\b"),
                    p("\\bSystem\\s*\\.\\s*exit\\s*\\("),
                    p("\\bClass\\s*\\.\\s*forName\\s*\\("),
                    p("\\bgetDeclaredMethod\\s*\\("),
                    p("\\bgetDeclaredField\\s*\\("),
                    p("\\bsetAccessible\\s*\\("),
                    p("\\bThread\\s*\\.\\s*sleep\\s*\\("),
                    p("\\bwhile\\s*\\(\\s*true\\s*\\)"),
                    p("\\bfor\\s*\\(\\s*;\\s*;\\s*\\)")
            )),
            Map.entry("C", List.of(
                    p("#\\s*include\\s*<\\s*unistd\\.h\\s*>"),
                    p("#\\s*include\\s*<\\s*sys/"),
                    p("#\\s*include\\s*<\\s*dirent\\.h\\s*>"),
                    p("\\bsystem\\s*\\("),
                    p("\\bpopen\\s*\\("),
                    p("\\bfopen\\s*\\("),
                    p("\\bfreopen\\s*\\("),
                    p("\\bremove\\s*\\("),
                    p("\\brename\\s*\\("),
                    p("\\bwhile\\s*\\(\\s*1\\s*\\)"),
                    p("\\bwhile\\s*\\(\\s*true\\s*\\)"),
                    p("\\bfor\\s*\\(\\s*;\\s*;\\s*\\)")
            )),
            Map.entry("CPP", List.of(
                    p("#\\s*include\\s*<\\s*fstream\\s*>"),
                    p("#\\s*include\\s*<\\s*filesystem\\s*>"),
                    p("#\\s*include\\s*<\\s*unistd\\.h\\s*>"),
                    p("#\\s*include\\s*<\\s*sys/"),
                    p("#\\s*include\\s*<\\s*dirent\\.h\\s*>"),
                    p("\\bsystem\\s*\\("),
                    p("\\bpopen\\s*\\("),
                    p("\\bfopen\\s*\\("),
                    p("\\bfreopen\\s*\\("),
                    p("\\bremove\\s*\\("),
                    p("\\brename\\s*\\("),
                    p("\\bwhile\\s*\\(\\s*true\\s*\\)"),
                    p("\\bfor\\s*\\(\\s*;\\s*;\\s*\\)")
            )),
            Map.entry("CSHARP", List.of(
                    p("\\busing\\s+System\\.IO\\b"),
                    p("\\busing\\s+System\\.Net\\b"),
                    p("\\busing\\s+System\\.Reflection\\b"),
                    p("\\busing\\s+System\\.Diagnostics\\b"),
                    p("\\bFile\\s*\\."),
                    p("\\bDirectory\\s*\\."),
                    p("\\bProcess\\s*\\."),
                    p("\\bEnvironment\\s*\\.\\s*Exit\\s*\\("),
                    p("\\bwhile\\s*\\(\\s*true\\s*\\)"),
                    p("\\bfor\\s*\\(\\s*;\\s*;\\s*\\)")
            )),
            Map.entry("FSHARP", List.of(
                    p("\\bopen\\s+System\\.IO\\b"),
                    p("\\bopen\\s+System\\.Net\\b"),
                    p("\\bopen\\s+System\\.Reflection\\b"),
                    p("\\bopen\\s+System\\.Diagnostics\\b"),
                    p("\\bFile\\."),
                    p("\\bDirectory\\."),
                    p("\\bProcess\\."),
                    p("\\bwhile\\s+true\\s+do\\b")
            )),
            Map.entry("PHP", List.of(
                    p("\\bshell_exec\\s*\\("),
                    p("\\bexec\\s*\\("),
                    p("\\bsystem\\s*\\("),
                    p("\\bpassthru\\s*\\("),
                    p("\\bproc_open\\s*\\("),
                    p("\\bpopen\\s*\\("),
                    p("\\bfopen\\s*\\("),
                    p("\\bfile_get_contents\\s*\\("),
                    p("\\bfile_put_contents\\s*\\("),
                    p("\\bunlink\\s*\\("),
                    p("\\beval\\s*\\(")
            )),
            Map.entry("RUBY", List.of(
                    p("\\brequire\\s+['\"]socket['\"]"),
                    p("\\brequire\\s+['\"]open-uri['\"]"),
                    p("\\brequire\\s+['\"]fileutils['\"]"),
                    p("\\bFile\\."),
                    p("\\bDir\\."),
                    p("\\bIO\\."),
                    p("\\bKernel\\.system\\s*\\("),
                    p("\\bsystem\\s*\\("),
                    p("\\bexec\\s*\\("),
                    p("`[^`]*`"),
                    p("\\beval\\s*\\("),
                    p("\\bloop\\s+do\\b")
            )),
            Map.entry("HASKELL", List.of(
                    p("\\bimport\\s+System\\.Process\\b"),
                    p("\\bimport\\s+System\\.Directory\\b"),
                    p("\\bimport\\s+System\\.IO\\b"),
                    p("\\bimport\\s+Network\\b"),
                    p("\\bunsafePerformIO\\b")
            )),
            Map.entry("GO", List.of(
                    p("\"os/exec\""),
                    p("\"net\""),
                    p("\"net/http\""),
                    p("\"syscall\""),
                    p("\\bos\\.Open\\s*\\("),
                    p("\\bos\\.Create\\s*\\("),
                    p("\\bos\\.Remove\\s*\\("),
                    p("\\bexec\\.Command\\s*\\("),
                    p("\\bfor\\s*\\{")
            )),
            Map.entry("RUST", List.of(
                    p("\\bstd::fs\\b"),
                    p("\\bstd::net\\b"),
                    p("\\bstd::process\\b"),
                    p("\\bstd::env\\b"),
                    p("\\bunsafe\\b"),
                    p("\\bloop\\s*\\{")
            )),
            Map.entry("TYPESCRIPT", List.of(
                    p("\\bimport\\s+.*\\bfrom\\b"),
                    p("\\brequire\\s*\\("),
                    p("\\bprocess\\b"),
                    p("\\bchild_process\\b"),
                    p("\\bDeno\\.Command\\b"),
                    p("\\bDeno\\.run\\b"),
                    p("\\bDeno\\.readTextFile\\b"),
                    p("\\bDeno\\.writeTextFile\\b"),
                    p("\\bDeno\\.remove\\b"),
                    p("\\bDeno\\.env\\b"),
                    p("\\bfetch\\s*\\("),
                    p("\\beval\\s*\\("),
                    p("\\bFunction\\s*\\("),
                    p("\\bwhile\\s*\\(\\s*true\\s*\\)"),
                    p("\\bfor\\s*\\(\\s*;\\s*;\\s*\\)")
            ))
    );

    public void validate(String language, String sourceCode) {
        String lang = normalizeLanguage(language);
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

        List<Pattern> patterns = BLOCKED_PATTERNS.getOrDefault(lang, List.of());

        for (Pattern pattern : patterns) {
            if (pattern.matcher(code).find()) {
                throw new RuntimeException("Blocked unsafe code pattern: " + readable(pattern.pattern()));
            }
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
                .trim()
                .toLowerCase(Locale.ROOT);

        return firstLine.matches("^(find|cat|ls|pwd|whoami|id|uname|ps|env|printenv|curl|wget|nc|netcat|bash|sh|zsh|python|python3|perl|ruby)\\b.*")
                || firstLine.contains("2>/dev/null")
                || firstLine.contains("/etc/passwd")
                || firstLine.contains("/home")
                || firstLine.contains("/proc")
                || firstLine.contains("/root")
                || firstLine.contains("&&")
                || firstLine.contains("||")
                || firstLine.contains(";")
                || firstLine.contains("|");
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