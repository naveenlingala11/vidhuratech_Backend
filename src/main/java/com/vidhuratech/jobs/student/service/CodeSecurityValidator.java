package com.vidhuratech.jobs.student.service;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class CodeSecurityValidator {

    private static final int MAX_SOURCE_CHARS = 20000;
    private static final int MAX_SOURCE_LINES = 600;

    private static final Map<String, List<Pattern>> BLOCKED_PATTERNS = new HashMap<>();

    static {
        // PYTHON
        BLOCKED_PATTERNS.put("PYTHON", compilePatterns(List.of(
            "\\bimport\\s+os\\b",
            "\\bfrom\\s+os\\s+import\\b",
            "\\bimport\\s+subprocess\\b",
            "\\bfrom\\s+subprocess\\s+import\\b",
            "\\bimport\\s+socket\\b",
            "\\bfrom\\s+socket\\s+import\\b",
            "\\bimport\\s+requests\\b",
            "\\bimport\\s+urllib\\b",
            "\\bfrom\\s+urllib\\s+import\\b",
            "\\bimport\\s+pathlib\\b",
            "\\bfrom\\s+pathlib\\s+import\\b",
            "\\bimport\\s+shutil\\b",
            "\\bfrom\\s+shutil\\s+import\\b",
            "\\bimport\\s+pickle\\b",
            "\\bfrom\\s+pickle\\s+import\\b",
            "\\bopen\\s*\\(",
            "\\beval\\s*\\(",
            "\\bexec\\s*\\(",
            "\\bcompile\\s*\\(",
            "\\b__import__\\s*\\(",
            "\\bglobals\\s*\\(",
            "\\blocals\\s*\\(",
            "\\bvars\\s*\\(",
            "\\bsys\\s*\\.\\s*modules\\b",
            "\\bsys\\s*\\.\\s*path\\b",
            "\\bsys\\s*\\.\\s*argv\\b",
            "\\bsys\\s*\\.\\s*exit\\s*\\(",
            "__dict__",
            "__class__",
            "__mro__",
            "__subclasses__"
        )));

        // JAVA
        BLOCKED_PATTERNS.put("JAVA", compilePatterns(List.of(
            "\\bimport\\s+java\\.io\\.File\\b",
            "\\bimport\\s+java\\.io\\.FileInputStream\\b",
            "\\bimport\\s+java\\.io\\.FileOutputStream\\b",
            "\\bimport\\s+java\\.io\\.RandomAccessFile\\b",
            "\\bimport\\s+java\\.nio\\.file\\.",
            "\\bimport\\s+java\\.net\\.",
            "\\bimport\\s+java\\.lang\\.reflect\\.",
            "\\bRuntime\\s*\\.\\s*getRuntime\\s*\\(",
            "\\bProcessBuilder\\b",
            "\\bSystem\\s*\\.\\s*exit\\s*\\(",
            "\\bClass\\s*\\.\\s*forName\\s*\\(",
            "\\bgetDeclaredMethod\\s*\\(",
            "\\bgetDeclaredField\\s*\\(",
            "\\bsetAccessible\\s*\\(",
            "\\bThread\\s*\\.\\s*sleep\\s*\\(",
            "\\bwhile\\s*\\(\\s*true\\s*\\)",
            "\\bfor\\s*\\(\\s*;\\s*;\\s*\\)"
        )));

        // C
        BLOCKED_PATTERNS.put("C", compilePatterns(List.of(
            "#\\s*include\\s*<\\s*unistd\\.h\\s*>",
            "#\\s*include\\s*<\\s*sys/",
            "#\\s*include\\s*<\\s*dirent\\.h\\s*>",
            "\\bsystem\\s*\\(",
            "\\bpopen\\s*\\(",
            "\\bfopen\\s*\\(",
            "\\bfreopen\\s*\\(",
            "\\bremove\\s*\\(",
            "\\brename\\s*\\(",
            "\\bwhile\\s*\\(\\s*1\\s*\\)",
            "\\bwhile\\s*\\(\\s*true\\s*\\)",
            "\\bfor\\s*\\(\\s*;\\s*;\\s*\\)"
        )));

        // CPP
        BLOCKED_PATTERNS.put("CPP", compilePatterns(List.of(
            "#\\s*include\\s*<\\s*fstream\\s*>",
            "#\\s*include\\s*<\\s*filesystem\\s*>",
            "#\\s*include\\s*<\\s*unistd\\.h\\s*>",
            "#\\s*include\\s*<\\s*sys/",
            "#\\s*include\\s*<\\s*dirent\\.h\\s*>",
            "\\bsystem\\s*\\(",
            "\\bpopen\\s*\\(",
            "\\bfopen\\s*\\(",
            "\\bfreopen\\s*\\(",
            "\\bremove\\s*\\(",
            "\\brename\\s*\\(",
            "\\bwhile\\s*\\(\\s*true\\s*\\)",
            "\\bfor\\s*\\(\\s*;\\s*;\\s*\\)"
        )));

        // CSHARP
        BLOCKED_PATTERNS.put("CSHARP", compilePatterns(List.of(
            "\\busing\\s+System\\.IO\\b",
            "\\busing\\s+System\\.Net\\b",
            "\\busing\\s+System\\.Reflection\\b",
            "\\busing\\s+System\\.Diagnostics\\b",
            "\\bFile\\s*\\.",
            "\\bDirectory\\s*\\.",
            "\\bProcess\\s*\\.",
            "\\bEnvironment\\s*\\.\\s*Exit\\s*\\(",
            "\\bwhile\\s*\\(\\s*true\\s*\\)",
            "\\bfor\\s*\\(\\s*;\\s*;\\s*\\)"
        )));

        // FSHARP
        BLOCKED_PATTERNS.put("FSHARP", compilePatterns(List.of(
            "\\bopen\\s+System\\.IO\\b",
            "\\bopen\\s+System\\.Net\\b",
            "\\bopen\\s+System\\.Reflection\\b",
            "\\bopen\\s+System\\.Diagnostics\\b",
            "\\bFile\\.",
            "\\bDirectory\\.",
            "\\bProcess\\.",
            "\\bwhile\\s+true\\s+do\\b"
        )));

        // PHP
        BLOCKED_PATTERNS.put("PHP", compilePatterns(List.of(
            "\\bshell_exec\\s*\\(",
            "\\bexec\\s*\\(",
            "\\bsystem\\s*\\(",
            "\\bpassthru\\s*\\(",
            "\\bproc_open\\s*\\(",
            "\\bpopen\\s*\\(",
            "\\bfopen\\s*\\(",
            "\\bfile_get_contents\\s*\\(",
            "\\bfile_put_contents\\s*\\(",
            "\\bunlink\\s*\\(",
            "\\beval\\s*\\("
        )));

        // RUBY
        BLOCKED_PATTERNS.put("RUBY", compilePatterns(List.of(
            "\\brequire\\s+['\"]socket['\"]",
            "\\brequire\\s+['\"]open-uri['\"]",
            "\\brequire\\s+['\"]fileutils['\"]",
            "\\bFile\\.",
            "\\bDir\\.",
            "\\bIO\\.",
            "\\bKernel\\.system\\s*\\(",
            "\\bsystem\\s*\\(",
            "\\bexec\\s*\\(",
            "`[^`]*`",
            "\\beval\\s*\\(",
            "\\bloop\\s+do\\b"
        )));

        // HASKELL
        BLOCKED_PATTERNS.put("HASKELL", compilePatterns(List.of(
            "\\bimport\\s+System\\.Process\\b",
            "\\bimport\\s+System\\.Directory\\b",
            "\\bimport\\s+System\\.IO\\b",
            "\\bimport\\s+Network\\b",
            "\\bunsafePerformIO\\b"
        )));

        // GO
        BLOCKED_PATTERNS.put("GO", compilePatterns(List.of(
            "\"os/exec\"",
            "\"net\"",
            "\"net/http\"",
            "\"syscall\"",
            "\\bos\\.Open\\s*\\(",
            "\\bos\\.Create\\s*\\(",
            "\\bos\\.Remove\\s*\\(",
            "\\bexec\\.Command\\s*\\(",
            "\\bfor\\s*\\{"
        )));

        // RUST
        BLOCKED_PATTERNS.put("RUST", compilePatterns(List.of(
            "\\bstd::fs\\b",
            "\\bstd::net\\b",
            "\\bstd::process\\b",
            "\\bstd::env\\b",
            "\\bunsafe\\b",
            "\\bloop\\s*\\{"
        )));

        // TYPESCRIPT
        BLOCKED_PATTERNS.put("TYPESCRIPT", compilePatterns(List.of(
            "\\bimport\\s+.*\\bfrom\\b",
            "\\brequire\\s*\\(",
            "\\bprocess\\b",
            "\\bchild_process\\b",
            "\\bDeno\\.Command\\b",
            "\\bDeno\\.run\\b",
            "\\bDeno\\.readTextFile\\b",
            "\\bDeno\\.writeTextFile\\b",
            "\\bDeno\\.remove\\b",
            "\\bDeno\\.env\\b",
            "\\bfetch\\s*\\(",
            "\\beval\\s*\\(",
            "\\bFunction\\s*\\(",
            "\\bwhile\\s*\\(\\s*true\\s*\\)",
            "\\bfor\\s*\\(\\s*;\\s*;\\s*\\)"
        )));
    }

    private static List<Pattern> compilePatterns(List<String> regexes) {
        return regexes.stream()
                .map(CodeSecurityValidator::p)
                .toList();
    }

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

        String normalized = normalizeLanguage(language);
        List<Pattern> patterns = BLOCKED_PATTERNS.get(normalized);
        if (patterns != null) {
            for (Pattern pattern : patterns) {
                if (pattern.matcher(code).find()) {
                    throw new RuntimeException("Security violation: Use of restricted library/keyword '" + readable(pattern.pattern()) + "' is blocked.");
                }
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