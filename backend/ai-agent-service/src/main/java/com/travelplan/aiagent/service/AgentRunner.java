package com.travelplan.aiagent.service;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.RunConfig;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AgentRunner {

    private final BaseAgent rootAgent;
    private InMemoryRunner runner;

    public AgentRunner(BaseAgent tripPlannerAgent) {
        this.rootAgent = tripPlannerAgent;
    }

    @PostConstruct
    public void init() {
        this.runner = new InMemoryRunner(rootAgent);
        log.info("AgentRunner initialized with root agent: {}", rootAgent.name());
    }

    public Session createSession(String userId) {
        return runner.sessionService()
                .createSession(runner.appName(), userId)
                .blockingGet();
    }

    public Session getOrCreateSession(String sessionId, String userId) {
        // Try to get existing session first
        try {
            Session existing = runner.sessionService()
                    .getSession(runner.appName(), userId, sessionId, java.util.Optional.empty())
                    .blockingGet();
            if (existing != null) {
                return existing;
            }
        } catch (Exception e) {
            log.debug("Session {} not found, creating new one", sessionId);
        }

        return createSession(userId);
    }

    public Flowable<Event> runAsync(String userId, String sessionId, String userMessage) {
        Content userContent = Content.fromParts(Part.fromText(userMessage));
        RunConfig runConfig = RunConfig.builder().build();

        log.debug("Running agent for user: {}, session: {}, message: {}",
                userId, sessionId, userMessage.substring(0, Math.min(100, userMessage.length())));

        return runner.runAsync(userId, sessionId, userContent, runConfig);
    }

    public InMemoryRunner getRunner() {
        return runner;
    }
}
