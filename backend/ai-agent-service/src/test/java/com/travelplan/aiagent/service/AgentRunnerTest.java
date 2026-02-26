package com.travelplan.aiagent.service;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AgentRunnerTest {

    @Test
    void constructor_acceptsRootAgent() {
        BaseAgent mockAgent = LlmAgent.builder()
                .name("TestAgent")
                .model("gemini-2.0-flash")
                .instruction("Test instruction")
                .build();

        AgentRunner runner = new AgentRunner(mockAgent);
        assertNotNull(runner);
    }
}
