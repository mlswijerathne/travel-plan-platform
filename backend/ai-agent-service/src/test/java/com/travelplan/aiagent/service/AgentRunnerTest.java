package com.travelplan.aiagent.service;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.models.langchain4j.LangChain4j;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AgentRunnerTest {

    @Test
    void constructor_acceptsRootAgent() {
        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .apiKey("test-key")
                .modelName("gpt-4o-mini")
                .build();

        BaseAgent mockAgent = LlmAgent.builder()
                .name("TestAgent")
                .model(new LangChain4j(chatModel))
                .instruction("Test instruction")
                .build();

        AgentRunner runner = new AgentRunner(mockAgent);
        assertNotNull(runner);
    }
}
