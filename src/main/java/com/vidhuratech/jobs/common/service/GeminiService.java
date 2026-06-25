package com.vidhuratech.jobs.common.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gemini.api-key:}")
    private String geminiApiKey;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String geminiModel;

    @Value("${groq.api-key:}")
    private String groqApiKey;

    @Value("${groq.model:llama-3.3-70b-specdec}")
    private String groqModel;

    @Value("${deepseek.api-key:}")
    private String deepseekApiKey;

    @Value("${deepseek.model:deepseek-chat}")
    private String deepseekModel;

    @Value("${openrouter.api-key:}")
    private String openrouterApiKey;

    @Value("${openrouter.model:meta-llama/llama-3-8b-instruct:free}")
    private String openrouterModel;

    @Value("${ai.active-provider:GEMINI}")
    private String activeProvider;

    // Getters and Setters for runtime customization
    public String getGeminiApiKey() {
        return geminiApiKey;
    }

    public void setGeminiApiKey(String geminiApiKey) {
        this.geminiApiKey = geminiApiKey;
    }

    public String getGeminiModel() {
        return geminiModel;
    }

    public void setGeminiModel(String geminiModel) {
        this.geminiModel = geminiModel;
    }

    public String getGroqApiKey() {
        return groqApiKey;
    }

    public void setGroqApiKey(String groqApiKey) {
        this.groqApiKey = groqApiKey;
    }

    public String getGroqModel() {
        return groqModel;
    }

    public void setGroqModel(String groqModel) {
        this.groqModel = groqModel;
    }

    public String getDeepseekApiKey() {
        return deepseekApiKey;
    }

    public void setDeepseekApiKey(String deepseekApiKey) {
        this.deepseekApiKey = deepseekApiKey;
    }

    public String getDeepseekModel() {
        return deepseekModel;
    }

    public void setDeepseekModel(String deepseekModel) {
        this.deepseekModel = deepseekModel;
    }

    public String getOpenrouterApiKey() {
        return openrouterApiKey;
    }

    public void setOpenrouterApiKey(String openrouterApiKey) {
        this.openrouterApiKey = openrouterApiKey;
    }

    public String getOpenrouterModel() {
        return openrouterModel;
    }

    public void setOpenrouterModel(String openrouterModel) {
        this.openrouterModel = openrouterModel;
    }

    public String getActiveProvider() {
        return activeProvider;
    }

    public void setActiveProvider(String activeProvider) {
        this.activeProvider = activeProvider;
    }

    private boolean isValidApiKey(String apiKey) {
        return apiKey != null && !apiKey.isBlank() 
                && !apiKey.startsWith("YOUR_") 
                && !apiKey.contains("PLACEHOLDER");
    }

    public boolean isGeminiConfigured() {
        return isValidApiKey(geminiApiKey);
    }

    public boolean isGroqConfigured() {
        return isValidApiKey(groqApiKey);
    }

    public boolean isDeepseekConfigured() {
        return isValidApiKey(deepseekApiKey);
    }

    public boolean isOpenrouterConfigured() {
        return isValidApiKey(openrouterApiKey);
    }

    public boolean isConfigured() {
        if ("GROQ".equalsIgnoreCase(activeProvider)) {
            return isGroqConfigured();
        } else if ("DEEPSEEK".equalsIgnoreCase(activeProvider)) {
            return isDeepseekConfigured();
        } else if ("OPENROUTER".equalsIgnoreCase(activeProvider)) {
            return isOpenrouterConfigured();
        } else {
            return isGeminiConfigured();
        }
    }

    public String getReview(String problemTitle, String problemDescription, String code, String language) {
        if (!isConfigured()) {
            return "### ⚠️ AI Service (" + activeProvider + ") not configured\n" +
                   "Please verify your API key settings for " + activeProvider + " in the admin console.";
        }

        String systemInstruction = "You are an expert AI coding companion and code reviewer for the Vidhura Tech platform.\n" +
                "Analyze the user's submitted code against the problem statement and output a concise, structured review in markdown format.\n" +
                "The review MUST contain the following sections:\n" +
                "1. **Analysis & Correctness**: Check if the code is correct, passes edge cases, and solves the problem.\n" +
                "2. **Complexity Analysis**: State the Time and Space complexity in Big O notation (e.g. O(N), O(N log N), O(1)) and explain why.\n" +
                "3. **Code Smells & Improvements**: List any issues with readability, naming conventions, redundancy, or styling.\n" +
                "4. **Optimized Alternative**: Provide a clean, fully optimized version of the code in the same language. Wrap it in a markdown code block.\n\n" +
                "Be encouraging but rigorous in your evaluation.";

        String userPrompt = String.format(
                "Problem: %s\n" +
                "Description: %s\n" +
                "Language: %s\n" +
                "User's Code:\n```%s\n%s\n```",
                problemTitle, problemDescription, language, language.toLowerCase(), code
        );

        if ("GROQ".equalsIgnoreCase(activeProvider)) {
            return callGroq(systemInstruction, userPrompt);
        } else if ("DEEPSEEK".equalsIgnoreCase(activeProvider)) {
            return callDeepSeek(systemInstruction, userPrompt);
        } else if ("OPENROUTER".equalsIgnoreCase(activeProvider)) {
            return callOpenRouter(systemInstruction, userPrompt);
        } else {
            return callGemini(systemInstruction, userPrompt);
        }
    }

    public String getAiHints(String problemTitle, String problemDescription, String constraints, String inputFormat, String outputFormat) {
        if (!isConfigured()) {
            return "Error generating hints: AI Service (" + activeProvider + ") not configured.";
        }

        String systemInstruction = "You are an expert AI coding tutor.\n" +
                "Generate exactly 3 helpful, progressive hints to help a student solve the coding challenge without giving away the actual solution code.\n" +
                "Follow this strict structure for the 3 hints:\n" +
                "Hint 1 (DSA Method & Approach): Identify the core Data Structure and Algorithm (DSA) method or approach required to solve this problem (e.g., Two Pointers, Sliding Window, Dynamic Programming, Stack, Heap, Binary Search, etc.). Explain why this method fits the constraints and how to choose it.\n" +
                "Hint 2 (Step-by-Step Algorithm): Describe the step-by-step algorithmic logic in a clear, sequential manner (e.g., Step 1: ..., Step 2: ..., Step 3: ...). Walk through the solution conceptually without writing code.\n" +
                "Hint 3 (Edge Cases & Complexity): Detail crucial edge cases to consider (e.g., empty arrays, single elements, extreme boundaries, duplicates) and outline the target Time and Space Complexity (e.g., O(N) Time, O(1) Space).\n" +
                "\n" +
                "CRITICAL FORMATTING RULES:\n" +
                "1. Output exactly 3 hints, separated ONLY by a double newline (\\n\\n).\n" +
                "2. Each hint MUST be written on a single line. Do NOT use any internal newlines (\\n or \\r) inside a hint, as the frontend splits hints by any newline. Use spaces or punctuation to separate sentences.\n" +
                "3. Do NOT add any bullet points, step numbers, or numbering prefixes at the start of the hints (e.g. do not start with '1.', 'Hint 1:', etc.).\n" +
                "4. Do NOT write any code snippets or programming syntax. Use markdown bolding for emphasis on key concepts.";

        String userPrompt = String.format(
                "Problem: %s\n" +
                "Description: %s\n" +
                "Constraints: %s\n" +
                "Input Format: %s\n" +
                "Output Format: %s\n",
                problemTitle, problemDescription, constraints, inputFormat, outputFormat
        );

        if ("GROQ".equalsIgnoreCase(activeProvider)) {
            return callGroq(systemInstruction, userPrompt);
        } else if ("DEEPSEEK".equalsIgnoreCase(activeProvider)) {
            return callDeepSeek(systemInstruction, userPrompt);
        } else if ("OPENROUTER".equalsIgnoreCase(activeProvider)) {
            return callOpenRouter(systemInstruction, userPrompt);
        } else {
            return callGemini(systemInstruction, userPrompt);
        }
    }

    private String callGemini(String systemInstruction, String userPrompt) {
        try {
            Map<String, Object> parts = Map.of("text", userPrompt + "\n\nSystem Instructions:\n" + systemInstruction);
            Map<String, Object> contents = Map.of("parts", List.of(parts));
            Map<String, Object> requestBody = Map.of("contents", List.of(contents));
            String requestBodyJson = objectMapper.writeValueAsString(requestBody);

            String url = "https://generativelanguage.googleapis.com/v1beta/models/" + geminiModel + ":generateContent?key=" + geminiApiKey;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return "❌ Gemini API error (Status Code: " + response.statusCode() + "): " + response.body();
            }

            JsonNode rootNode = objectMapper.readTree(response.body());
            JsonNode textNode = rootNode.path("candidates").get(0)
                    .path("content").path("parts").get(0).path("text");

            return textNode.asText("No response generated by Gemini.");

        } catch (Exception e) {
            return "❌ Error communicating with Gemini API: " + e.getMessage();
        }
    }

    private String callOpenAiCompatible(String url, String apiKey, String model, String systemInstruction, String userPrompt, Map<String, String> extraHeaders) {
        try {
            Map<String, String> systemMessage = Map.of("role", "system", "content", systemInstruction);
            Map<String, String> userMessage = Map.of("role", "user", "content", userPrompt);
            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", List.of(systemMessage, userMessage),
                    "temperature", 0.2
            );
            String requestBodyJson = objectMapper.writeValueAsString(requestBody);

            var builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson));

            if (extraHeaders != null) {
                extraHeaders.forEach(builder::header);
            }

            HttpRequest request = builder.build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return "❌ API error (Status Code: " + response.statusCode() + "): " + response.body();
            }

            JsonNode rootNode = objectMapper.readTree(response.body());
            JsonNode textNode = rootNode.path("choices").get(0)
                    .path("message").path("content");

            return textNode.asText("No response generated.");
        } catch (Exception e) {
            return "❌ Error communicating with API: " + e.getMessage();
        }
    }

    private String callGroq(String systemInstruction, String userPrompt) {
        return callOpenAiCompatible("https://api.groq.com/openai/v1/chat/completions", groqApiKey, groqModel, systemInstruction, userPrompt, null);
    }

    private String callDeepSeek(String systemInstruction, String userPrompt) {
        return callOpenAiCompatible("https://api.deepseek.com/chat/completions", deepseekApiKey, deepseekModel, systemInstruction, userPrompt, null);
    }

    private String callOpenRouter(String systemInstruction, String userPrompt) {
        return callOpenAiCompatible("https://openrouter.ai/api/v1/chat/completions", openrouterApiKey, openrouterModel, systemInstruction, userPrompt, 
            Map.of("HTTP-Referer", "https://vidhuratech.com", "X-Title", "Vidhura Tech"));
    }
}
