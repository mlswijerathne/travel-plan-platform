package com.travelplan.aiagent.config;

import com.google.adk.models.BaseLlm;
import com.google.adk.models.langchain4j.LangChain4j;
import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class OpenAiConfig {

    @Value("${openai.api-key:}")
    private String openAiApiKey;

    @Value("${openai.model:gpt-4.1-mini}")
    private String modelName;

    @PostConstruct
    public void init() {
        if (openAiApiKey != null && !openAiApiKey.isBlank()) {
            log.info("OpenAI API key configured, model: {}", modelName);
        } else {
            log.warn("OPENAI_API_KEY is not configured. AI agent features will not work.");
        }
    }

    @Bean
    public BaseLlm agentModel() {
        log.info("OpenAI key configured, length: {}", openAiApiKey != null ? openAiApiKey.length() : 0);
        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .baseUrl("https://api.openai.com/v1")
                .apiKey(openAiApiKey)
                .modelName(modelName)
                .logRequests(true)
                .logResponses(true)
                .strictTools(false)
                .parallelToolCalls(false)
                .build();
        log.info("Created OpenAI chat model: {}", modelName);
        return new LangChain4j(chatModel);
    }
}
