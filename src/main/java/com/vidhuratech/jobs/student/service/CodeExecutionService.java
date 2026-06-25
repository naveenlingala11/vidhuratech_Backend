package com.vidhuratech.jobs.student.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CodeExecutionService {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${onlinecompiler.base-url:https://judge0-ce.p.rapidapi.com}")
    private String baseUrl;

    @Value("${onlinecompiler.api-key:}")
    private String apiKey;

    public ExecutionResult run(String language, String sourceCode, String inputData) {
        try {
            String normalizedLanguage = normalizeLanguage(language);
            Integer langId = judge0LanguageId(normalizedLanguage);
            String wrappedCode = wrapSourceCode(normalizedLanguage, sourceCode == null ? "" : sourceCode);

            String base64Code = encodeBase64(wrappedCode);
            String base64Input = encodeBase64(inputData == null ? "" : inputData);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("source_code", base64Code);
            body.put("language_id", langId);
            body.put("stdin", base64Input);
            // Strict resource and network constraints for sandboxing (LeetCode-style security)
            body.put("cpu_time_limit", 2.0);
            body.put("memory_limit", 131072);
            body.put("max_processes_and_threads", 30);
            body.put("enable_network", false);

            String url = cleanBaseUrl(baseUrl) + "/submissions?wait=true&base64_encoded=true";

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)));

            if (url.contains("rapidapi.com")) {
                if (hasText(apiKey)) {
                    builder.header("x-rapidapi-key", apiKey);
                }
                builder.header("x-rapidapi-host", URI.create(url).getHost());
            } else if (hasText(apiKey)) {
                builder.header("Authorization", apiKey);
                builder.header("X-Auth-Token", apiKey);
            }

            HttpRequest request = builder.build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            String rawBody = response.body();

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return ExecutionResult.builder()
                        .success(false)
                        .output("")
                        .error("Judge0 HTTP " + response.statusCode() + ": " + rawBody)
                        .executionTimeMs(0L)
                        .build();
            }

            Map<String, Object> result = objectMapper.readValue(
                    rawBody,
                    new TypeReference<Map<String, Object>>() {}
            );

            String stdout = decodeBase64(stringValue(result.get("stdout")));
            String stderr = decodeBase64(stringValue(result.get("stderr")));
            String compileOutput = decodeBase64(stringValue(result.get("compile_output")));
            String message = decodeBase64(stringValue(result.get("message")));

            Map<String, Object> statusMap = (Map<String, Object>) result.get("status");
            int statusId = 0;
            String statusDescription = "";
            if (statusMap != null) {
                statusId = intValue(statusMap.get("id"));
                statusDescription = stringValue(statusMap.get("description"));
            }

            boolean success = (statusId == 3);

            String finalError = "";
            if (!success) {
                if (statusId == 6) {
                    finalError = hasText(compileOutput) ? compileOutput : "Compilation Error";
                } else if (hasText(stderr)) {
                    finalError = stderr;
                } else if (hasText(message)) {
                    finalError = message;
                } else {
                    finalError = "Execution failed with status: " + statusDescription;
                }
            }

            return ExecutionResult.builder()
                    .success(success)
                    .output(stdout)
                    .error(finalError)
                    .executionTimeMs(timeToMillis(result.get("time")))
                    .build();

        } catch (Exception e) {
            return ExecutionResult.builder()
                    .success(false)
                    .output("")
                    .error("Compiler service failed: " + e.getMessage())
                    .executionTimeMs(0L)
                    .build();
        }
    }

    private static final Map<String, Integer> JUDGE0_LANGUAGES = Map.ofEntries(
            Map.entry("JAVA", 91),
            Map.entry("PYTHON", 71),
            Map.entry("C", 75),
            Map.entry("CPP", 76),
            Map.entry("CSHARP", 82),
            Map.entry("FSHARP", 87),
            Map.entry("PHP", 68),
            Map.entry("RUBY", 72),
            Map.entry("HASKELL", 61),
            Map.entry("GO", 95),
            Map.entry("RUST", 73),
            Map.entry("TYPESCRIPT", 94)
    );

    private String wrapSourceCode(String language, String sourceCode) {
        String code = sourceCode == null ? "" : sourceCode.strip();

        return switch (language) {
            case "PYTHON" -> wrapPython(code);
            case "JAVA" -> wrapJava(code);
            case "C" -> wrapC(code);
            case "CPP" -> wrapCpp(code);
            case "CSHARP" -> wrapCSharp(code);
            case "FSHARP" -> wrapFSharp(code);
            case "PHP" -> wrapPhp(code);
            case "RUBY" -> wrapRuby(code);
            case "HASKELL" -> wrapHaskell(code);
            case "GO" -> wrapGo(code);
            case "RUST" -> wrapRust(code);
            case "TYPESCRIPT" -> wrapTypeScript(code);
            default -> code;
        };
    }

    private String wrapPython(String code) {
        return """
                import sys
                import math
                from collections import deque, Counter, defaultdict
                from heapq import heappush, heappop, heapify
                from bisect import bisect_left, bisect_right

                %s
                """.formatted(code);
    }

    private String wrapJava(String code) {
        String defaults = """
                import java.util.*;
                import java.math.*;
                import java.io.BufferedInputStream;
                import java.io.IOException;

                """;

        if (code.matches("(?s).*\\bclass\\s+\\w+.*")) {
            // Replace "public class <name>" with "class <name>" to avoid filename mismatch compilation errors in Judge0
            String processedCode = code.replaceAll("\\bpublic\\s+class\\s+", "class ");
            return defaults + processedCode;
        }

        return defaults + """
                public class Main {
                    public static void main(String[] args) throws Exception {
                %s
                    }
                }
                """.formatted(indent(code, 8));
    }

    private String wrapC(String code) {
        String defaults = """
                #include <stdio.h>
                #include <stdlib.h>
                #include <string.h>
                #include <math.h>
                #include <limits.h>
                #include <stdbool.h>

                """;

        if (code.matches("(?s).*\\bmain\\s*\\(.*")) {
            return defaults + code;
        }

        return defaults + """
                int main(void) {
                %s
                    return 0;
                }
                """.formatted(indent(code, 4));
    }

    private String wrapCpp(String code) {
        String defaults = """
                #include <bits/stdc++.h>
                using namespace std;

                """;

        if (code.matches("(?s).*\\bmain\\s*\\(.*")) {
            return defaults + code;
        }

        return defaults + """
                int main() {
                    ios::sync_with_stdio(false);
                    cin.tie(nullptr);

                %s
                    return 0;
                }
                """.formatted(indent(code, 4));
    }

    private String wrapCSharp(String code) {
        String defaults = """
                using System;
                using System.Collections.Generic;
                using System.Linq;
                using System.Text;

                """;

        if (code.matches("(?s).*\\bclass\\s+\\w+.*")) {
            return defaults + code;
        }

        return defaults + """
                public class Program {
                    public static void Main(string[] args) {
                %s
                    }
                }
                """.formatted(indent(code, 8));
    }

    private String wrapFSharp(String code) {
        return """
                open System
                open System.Collections.Generic

                %s
                """.formatted(code);
    }

    private String wrapPhp(String code) {
        if (code.startsWith("<?php")) {
            return code;
        }

        return """
                <?php
                %s
                ?>
                """.formatted(code);
    }

    private String wrapRuby(String code) {
        return """
                input = STDIN.read

                %s
                """.formatted(code);
    }

    private String wrapHaskell(String code) {
        return """
                import Data.List
                import Data.Char
                import qualified Data.Map as Map
                import qualified Data.Set as Set

                %s
                """.formatted(code);
    }

    private String wrapGo(String code) {
        if (code.matches("(?s).*\\bpackage\\s+main\\b.*")) {
            return code;
        }

        if (code.matches("(?s).*\\bfunc\\s+main\\s*\\(\\s*\\).*")) {
            return """
                    package main

                    import (
                        "bufio"
                        "fmt"
                        "sort"
                        "strings"
                    )

                    %s
                    """.formatted(code);
        }

        return """
                package main

                import (
                    "bufio"
                    "fmt"
                    "sort"
                    "strings"
                )

                func main() {
                %s
                }
                """.formatted(indent(code, 4));
    }

    private String wrapRust(String code) {
        if (code.matches("(?s).*\\bfn\\s+main\\s*\\(\\s*\\).*")) {
            return """
                    use std::io::{self, Read};
                    use std::cmp::{min, max};
                    use std::collections::{HashMap, HashSet, VecDeque, BinaryHeap};

                    %s
                    """.formatted(code);
        }

        return """
                use std::io::{self, Read};
                use std::cmp::{min, max};
                use std::collections::{HashMap, HashSet, VecDeque, BinaryHeap};

                fn main() {
                %s
                }
                """.formatted(indent(code, 4));
    }

    private String wrapTypeScript(String code) {
        return """
                const input = await new Response(Deno.stdin.readable).text();
                const tokens = input.trim().length ? input.trim().split(/\\s+/) : [];

                %s
                """.formatted(code);
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

    private Integer judge0LanguageId(String language) {
        Integer id = JUDGE0_LANGUAGES.get(language);

        if (id == null) {
            throw new RuntimeException(
                    "Unsupported language. Supported languages: JAVA, PYTHON, C, CPP, CSHARP, FSHARP, PHP, RUBY, HASKELL, GO, RUST, TYPESCRIPT"
            );
        }

        return id;
    }

    private String encodeBase64(String value) {
        if (value == null) {
            return "";
        }
        return Base64.getEncoder().encodeToString(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private String decodeBase64(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        try {
            return new String(Base64.getDecoder().decode(value.trim()), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }

    private String cleanBaseUrl(String value) {
        String url = hasText(value) ? value.trim() : "https://api.onlinecompiler.io";
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private int intValue(Object value) {
        if (value == null) return 0;

        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception ignored) {
            return 0;
        }
    }

    private Long timeToMillis(Object value) {
        if (value == null || String.valueOf(value).isBlank()) return 0L;

        try {
            return Math.round(Double.parseDouble(String.valueOf(value)) * 1000);
        } catch (Exception ignored) {
            return 0L;
        }
    }

    private String firstText(String... values) {
        for (String value : values) {
            if (hasText(value)) return value;
        }
        return "";
    }

    private String indent(String code, int spaces) {
        String prefix = " ".repeat(spaces);
        return code.lines()
                .map(line -> line.isBlank() ? line : prefix + line)
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");
    }

    @Getter
    @Builder
    public static class ExecutionResult {
        private boolean success;
        private String output;
        private String error;
        private Long executionTimeMs;
    }
}