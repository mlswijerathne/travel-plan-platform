package com.travelplan.aiagent.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class GeminiConfig {

    @Value("${gemini.api-key:}")
    private String geminiApiKey;

    @Value("${gemini.model:gemini-2.0-flash}")
    private String modelName;

    @PostConstruct
    public void init() {
        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            System.setProperty("GOOGLE_API_KEY", geminiApiKey);
            log.info("GOOGLE_API_KEY set from gemini.api-key configuration");
        } else {
            log.warn("GEMINI_API_KEY is not configured. AI agent features will not work.");
        }
    }

    @Bean
    public String geminiModelName() {
        return modelName;
    }
}
