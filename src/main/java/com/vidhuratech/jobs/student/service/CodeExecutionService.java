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
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CodeExecutionService {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${onlinecompiler.base-url:https://api.onlinecompiler.io}")
    private String baseUrl;

    @Value("${onlinecompiler.api-key:}")
    private String apiKey;

    public ExecutionResult run(String language, String sourceCode, String inputData) {
        try {
            if (!hasText(apiKey)) {
                return ExecutionResult.builder()
                        .success(false)
                        .output("")
                        .error("OnlineCompiler API key is missing")
                        .executionTimeMs(0L)
                        .build();
            }

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("compiler", compiler(language));
            body.put("code", sourceCode == null ? "" : sourceCode);
            body.put("input", inputData == null ? "" : inputData);

            String url = cleanBaseUrl(baseUrl) + "/api/run-code-sync/";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            String rawBody = response.body();

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return ExecutionResult.builder()
                        .success(false)
                        .output("")
                        .error("OnlineCompiler HTTP " + response.statusCode() + ": " + rawBody)
                        .executionTimeMs(0L)
                        .build();
            }

            Map<String, Object> result = objectMapper.readValue(
                    rawBody,
                    new TypeReference<Map<String, Object>>() {}
            );

            String output = firstText(
                    stringValue(result.get("output")),
                    stringValue(result.get("stdout"))
            );

            String error = firstText(
                    stringValue(result.get("error")),
                    stringValue(result.get("stderr")),
                    stringValue(result.get("compile_output")),
                    stringValue(result.get("message")),
                    stringValue(result.get("exception")),
                    stringValue(result.get("details"))
            );

            String status = stringValue(result.get("status"));
            int exitCode = intValue(result.get("exit_code"));

            boolean success =
                    "success".equalsIgnoreCase(status)
                            && exitCode == 0
                            && !hasText(error);

            String finalError = "";
            if (!success) {
                if (hasText(error)) {
                    finalError = error;
                } else if (hasText(output) && !"success".equalsIgnoreCase(status)) {
                    finalError = output;
                } else {
                    finalError = "Code execution failed.\nRaw compiler response:\n" + rawBody;
                }
            }

            return ExecutionResult.builder()
                    .success(success)
                    .output(output)
                    .error(finalError)
                    .executionTimeMs(timeToMillis(result.get("total")))
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

    private static final Map<String, String> COMPILERS = Map.ofEntries(
            Map.entry("JAVA", "openjdk-25"),
            Map.entry("PYTHON", "python-3.14"),
            Map.entry("C", "gcc-15"),
            Map.entry("CPP", "g++-15"),
            Map.entry("C++", "g++-15"),
            Map.entry("CPLUSPLUS", "g++-15"),
            Map.entry("CSHARP", "dotnet-csharp-9"),
            Map.entry("C#", "dotnet-csharp-9"),
            Map.entry("F#", "dotnet-fsharp-9"),
            Map.entry("FSHARP", "dotnet-fsharp-9"),
            Map.entry("PHP", "php-8.5"),
            Map.entry("RUBY", "ruby-4.0"),
            Map.entry("HASKELL", "haskell-9.12"),
            Map.entry("GO", "go-1.26"),
            Map.entry("RUST", "rust-1.93"),
            Map.entry("TYPESCRIPT", "typescript-deno"),
            Map.entry("TS", "typescript-deno")
    );

    private String compiler(String language) {
        String value = language == null
                ? ""
                : language.trim().toUpperCase().replace("-", "").replace("_", "");

        String compiler = COMPILERS.get(value);

        if (compiler == null) {
            throw new RuntimeException(
                    "Unsupported language. Supported languages: JAVA, PYTHON, C, CPP, CSHARP, FSHARP, PHP, RUBY, HASKELL, GO, RUST, TYPESCRIPT"
            );
        }

        return compiler;
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
        return Integer.parseInt(String.valueOf(value));
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

    @Getter
    @Builder
    public static class ExecutionResult {
        private boolean success;
        private String output;
        private String error;
        private Long executionTimeMs;
    }
}