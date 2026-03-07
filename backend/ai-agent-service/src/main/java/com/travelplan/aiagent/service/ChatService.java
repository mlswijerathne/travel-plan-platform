package com.travelplan.aiagent.service;

import com.travelplan.aiagent.dto.*;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

public interface ChatService {

    Flux<ServerSentEvent<ChatStreamEvent>> chat(String userId, ChatRequest request);

    ConversationHistory getHistory(String sessionId);

    java.util.List<ConversationHistory> getUserSessions(String userId);

    void deleteSession(String sessionId);

    RecommendationResponse recommend(String userId, RecommendationRequest request);

    RecommendationResponse recommendPackages(String userId, PackageRecommendationRequest request);

    Flux<ServerSentEvent<ChatStreamEvent>> generatePlan(String userId, TripPlanRequest request);
}
