package com.travelplan.aiagent.service.impl;

import com.google.adk.events.Event;
import com.google.adk.sessions.Session;
import com.travelplan.aiagent.dto.*;
import com.travelplan.aiagent.service.*;
import io.reactivex.rxjava3.core.Flowable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final SessionManager sessionManager;
    private final AgentRunner agentRunner;
    private final StreamingService streamingService;

    @Override
    public Flux<ServerSentEvent<ChatStreamEvent>> chat(String userId, ChatRequest request) {
        String sessionId = sessionManager.getOrCreateSession(request.getSessionId(), userId);

        // Create or get ADK session
        Session adkSession = agentRunner.getOrCreateSession(sessionId, userId);

        // Record user message in history
        sessionManager.addUserMessage(sessionId, request.getMessage());

        // Run agent asynchronously
        Flowable<Event> events = agentRunner.runAsync(
                adkSession.userId(),
                adkSession.id(),
                request.getMessage()
        );

        // Convert to SSE stream and track the final response for history
        return streamingService.convertToSSE(events, sessionId)
                .doOnNext(sse -> {
                    if (sse.data() != null && sse.data().getType() == ChatStreamEvent.EventType.FINAL_RESPONSE) {
                        sessionManager.addAssistantMessage(
                                sessionId,
                                sse.data().getContent(),
                                sse.data().getQuickReplies()
                        );
                    }
                })
                .doOnError(error -> {
                    log.error("Error in chat stream for session {}: {}", sessionId, error.getMessage());
                    sessionManager.addAssistantMessage(sessionId,
                            "I encountered an error processing your request. Please try again.",
                            null);
                });
    }

    @Override
    public ConversationHistory getHistory(String sessionId) {
        ConversationHistory history = sessionManager.getHistory(sessionId);
        if (history == null) {
            log.warn("No history found for session: {}", sessionId);
            return ConversationHistory.builder()
                    .sessionId(sessionId)
                    .messages(List.of())
                    .build();
        }
        return history;
    }

    @Override
    public RecommendationResponse recommend(String userId, RecommendationRequest request) {
        // Build a natural language query from the recommendation request
        StringBuilder query = new StringBuilder("I need travel recommendations for ");
        query.append(request.getDestination());

        if (request.getDuration() != null) {
            query.append(" for ").append(request.getDuration()).append(" days");
        }
        if (request.getBudget() != null) {
            query.append(" with a budget of $").append(request.getBudget());
        }
        if (request.getInterests() != null && !request.getInterests().isEmpty()) {
            query.append(". My interests: ").append(String.join(", ", request.getInterests()));
        }
        if (request.getTravelStyle() != null) {
            query.append(". Travel style: ").append(request.getTravelStyle());
        }
        query.append(". Please search for hotels, tour guides, and vehicles and provide recommendations.");

        // Run through agent and collect final response
        String sessionId = sessionManager.getOrCreateSession(null, userId);
        Session adkSession = agentRunner.getOrCreateSession(sessionId, userId);

        String finalContent = runAgentSync(adkSession, query.toString());

        List<QuickReplyChip> chips = streamingService.extractQuickReplyChips(finalContent);
        String cleanContent = streamingService.removeChipMarkup(finalContent);

        return RecommendationResponse.builder()
                .summary(cleanContent)
                .quickReplies(chips)
                .build();
    }

    @Override
    public RecommendationResponse recommendPackages(String userId, PackageRecommendationRequest request) {
        StringBuilder query = new StringBuilder("Search for travel packages to ");
        query.append(request.getDestination());

        if (request.getDuration() != null) {
            query.append(" for ").append(request.getDuration()).append(" days");
        }
        if (request.getMinBudget() != null || request.getMaxBudget() != null) {
            query.append(" with a budget of ");
            if (request.getMinBudget() != null) query.append("$").append(request.getMinBudget());
            if (request.getMinBudget() != null && request.getMaxBudget() != null) query.append(" to ");
            if (request.getMaxBudget() != null) query.append("$").append(request.getMaxBudget());
        }
        if (request.getTravelers() != null) {
            query.append(" for ").append(request.getTravelers()).append(" travelers");
        }
        if (request.getInterests() != null && !request.getInterests().isEmpty()) {
            query.append(". Interests: ").append(String.join(", ", request.getInterests()));
        }
        query.append(". Please search available packages and recommend the best options.");

        String sessionId = sessionManager.getOrCreateSession(null, userId);
        Session adkSession = agentRunner.getOrCreateSession(sessionId, userId);

        String finalContent = runAgentSync(adkSession, query.toString());

        List<QuickReplyChip> chips = streamingService.extractQuickReplyChips(finalContent);
        String cleanContent = streamingService.removeChipMarkup(finalContent);

        return RecommendationResponse.builder()
                .summary(cleanContent)
                .quickReplies(chips)
                .build();
    }

    @Override
    public Flux<ServerSentEvent<ChatStreamEvent>> generatePlan(String userId, TripPlanRequest request) {
        StringBuilder query = new StringBuilder("Generate a detailed day-by-day itinerary for a trip to ");
        query.append(request.getDestination());
        query.append(" from ").append(request.getStartDate()).append(" to ").append(request.getEndDate());

        if (request.getBudget() != null) {
            query.append(" with a budget of $").append(request.getBudget());
        }
        if (request.getTravelers() != null) {
            query.append(" for ").append(request.getTravelers()).append(" travelers");
        }
        if (request.getInterests() != null && !request.getInterests().isEmpty()) {
            query.append(". Interests: ").append(String.join(", ", request.getInterests()));
        }
        if (request.getTravelStyle() != null) {
            query.append(". Travel style: ").append(request.getTravelStyle());
        }
        query.append(". Search for real hotels, tour guides, and vehicles from the platform. ");
        query.append("Include cost breakdown and budget analysis. ");
        query.append("Format as a complete trip plan with daily activities, accommodations, and transport.");

        String sessionId = sessionManager.getOrCreateSession(request.getSessionId(), userId);
        Session adkSession = agentRunner.getOrCreateSession(sessionId, userId);

        sessionManager.addUserMessage(sessionId, query.toString());

        Flowable<Event> events = agentRunner.runAsync(
                adkSession.userId(),
                adkSession.id(),
                query.toString()
        );

        return streamingService.convertToSSE(events, sessionId)
                .doOnNext(sse -> {
                    if (sse.data() != null && sse.data().getType() == ChatStreamEvent.EventType.FINAL_RESPONSE) {
                        sessionManager.addAssistantMessage(
                                sessionId,
                                sse.data().getContent(),
                                sse.data().getQuickReplies()
                        );
                    }
                })
                .doOnError(error -> {
                    log.error("Error in generate-plan stream for session {}: {}", sessionId, error.getMessage());
                    sessionManager.addAssistantMessage(sessionId,
                            "I encountered an error generating your plan. Please try again.",
                            null);
                })
                .onErrorResume(error -> {
                    log.error("Terminating generate-plan stream due to error: {}", error.getMessage());
                    return Flux.empty();
                });
    }

    private String runAgentSync(Session session, String message) {
        try {
            Flowable<Event> events = agentRunner.runAsync(
                    session.userId(),
                    session.id(),
                    message
            );

            StringBuilder response = new StringBuilder();
            events.blockingForEach(event -> {
                if (event.finalResponse()) {
                    String content = event.stringifyContent();
                    if (content != null && !content.isBlank()) {
                        response.append(content);
                    }
                }
            });

            return response.toString();
        } catch (Exception e) {
            log.error("Error running agent synchronously: {}", e.getMessage());
            return "I encountered an error processing your request. Please try again.";
        }
    }
}
