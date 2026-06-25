package com.vidhuratech.jobs.admin.controller;

import com.vidhuratech.jobs.common.api.ApiResponse;
import com.vidhuratech.jobs.common.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/super-admin/ai-config")
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
@RequiredArgsConstructor
public class AdminAiConfigController {

    private final GeminiService geminiService;

    @GetMapping
    public ApiResponse<?> getAiConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("activeProvider", geminiService.getActiveProvider());
        config.put("geminiModel", geminiService.getGeminiModel());
        config.put("groqModel", geminiService.getGroqModel());
        config.put("deepseekModel", geminiService.getDeepseekModel());
        config.put("openrouterModel", geminiService.getOpenrouterModel());
        
        // Hide API key contents for security, but return configured flag
        config.put("geminiConfigured", geminiService.isGeminiConfigured());
        config.put("groqConfigured", geminiService.isGroqConfigured());
        config.put("deepseekConfigured", geminiService.isDeepseekConfigured());
        config.put("openrouterConfigured", geminiService.isOpenrouterConfigured());
        
        config.put("availableProviders", List.of("GEMINI", "GROQ", "DEEPSEEK", "OPENROUTER"));
        
        // Model suggestions
        config.put("suggestedGeminiModels", List.of("gemini-2.5-flash", "gemini-2.5-pro", "gemini-1.5-flash", "gemini-1.5-pro"));
        config.put("suggestedGroqModels", List.of("llama-3.3-70b-specdec", "llama-3.1-70b-versatile", "llama3-70b-8192", "mixtral-8x7b-32768", "gemma2-9b-it"));
        config.put("suggestedDeepSeekModels", List.of("deepseek-chat", "deepseek-reasoner"));
        config.put("suggestedOpenRouterModels", List.of(
                "meta-llama/llama-3-8b-instruct:free", 
                "google/gemma-2-9b-it:free", 
                "qwen/qwen-2.5-72b-instruct:free",
                "microsoft/phi-3-medium-128k-instruct:free",
                "openchat/openchat-7b:free"
        ));
        
        return ApiResponse.success(config);
    }

    @PutMapping
    public ApiResponse<?> updateAiConfig(@RequestBody Map<String, String> payload) {
        if (payload.containsKey("activeProvider")) {
            String provider = payload.get("activeProvider");
            if ("GEMINI".equalsIgnoreCase(provider) || "GROQ".equalsIgnoreCase(provider) 
                    || "DEEPSEEK".equalsIgnoreCase(provider) || "OPENROUTER".equalsIgnoreCase(provider)) {
                geminiService.setActiveProvider(provider.toUpperCase());
            } else {
                return ApiResponse.error("Invalid activeProvider. Must be GEMINI, GROQ, DEEPSEEK, or OPENROUTER.");
            }
        }
        
        if (payload.containsKey("geminiModel") && payload.get("geminiModel") != null && !payload.get("geminiModel").isBlank()) {
            geminiService.setGeminiModel(payload.get("geminiModel").trim());
        }
        
        if (payload.containsKey("groqModel") && payload.get("groqModel") != null && !payload.get("groqModel").isBlank()) {
            geminiService.setGroqModel(payload.get("groqModel").trim());
        }
        
        if (payload.containsKey("deepseekModel") && payload.get("deepseekModel") != null && !payload.get("deepseekModel").isBlank()) {
            geminiService.setDeepseekModel(payload.get("deepseekModel").trim());
        }
        
        if (payload.containsKey("openrouterModel") && payload.get("openrouterModel") != null && !payload.get("openrouterModel").isBlank()) {
            geminiService.setOpenrouterModel(payload.get("openrouterModel").trim());
        }
        
        if (payload.containsKey("groqApiKey") && payload.get("groqApiKey") != null && !payload.get("groqApiKey").isBlank()) {
            geminiService.setGroqApiKey(payload.get("groqApiKey").trim());
        }
        
        if (payload.containsKey("geminiApiKey") && payload.get("geminiApiKey") != null && !payload.get("geminiApiKey").isBlank()) {
            geminiService.setGeminiApiKey(payload.get("geminiApiKey").trim());
        }
        
        if (payload.containsKey("deepseekApiKey") && payload.get("deepseekApiKey") != null && !payload.get("deepseekApiKey").isBlank()) {
            geminiService.setDeepseekApiKey(payload.get("deepseekApiKey").trim());
        }
        
        if (payload.containsKey("openrouterApiKey") && payload.get("openrouterApiKey") != null && !payload.get("openrouterApiKey").isBlank()) {
            geminiService.setOpenrouterApiKey(payload.get("openrouterApiKey").trim());
        }

        // Return the updated config (with flags)
        Map<String, Object> config = new HashMap<>();
        config.put("activeProvider", geminiService.getActiveProvider());
        config.put("geminiModel", geminiService.getGeminiModel());
        config.put("groqModel", geminiService.getGroqModel());
        config.put("deepseekModel", geminiService.getDeepseekModel());
        config.put("openrouterModel", geminiService.getOpenrouterModel());
        config.put("geminiConfigured", geminiService.isGeminiConfigured());
        config.put("groqConfigured", geminiService.isGroqConfigured());
        config.put("deepseekConfigured", geminiService.isDeepseekConfigured());
        config.put("openrouterConfigured", geminiService.isOpenrouterConfigured());
        
        return ApiResponse.success(config, "AI Model configuration updated successfully.");
    }
}
