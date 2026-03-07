package com.travelplan.aiagent.controller;

import com.travelplan.aiagent.dto.*;
import com.travelplan.aiagent.service.ChatService;
import com.travelplan.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<ChatStreamEvent>> chat(
            @Valid @RequestBody ChatRequest request,
            Authentication authentication) {
        String userId = authentication != null ? authentication.getName() : "anonymous";
        log.info("Chat request from user: {}, session: {}", userId, request.getSessionId());
        return Flux.defer(() -> chatService.chat(userId, request))
                .onErrorResume(error -> {
                    log.error("Chat endpoint error: {}", error.getMessage());
                    return Flux.just(ServerSentEvent.<ChatStreamEvent>builder()
                            .event("error")
                            .data(ChatStreamEvent.error("Service temporarily unavailable. Please try again."))
                            .build());
                });
    }

    @GetMapping("/history")
    public ApiResponse<ConversationHistory> getHistory(
            @RequestParam String sessionId) {
        log.info("Getting history for session: {}", sessionId);
        return ApiResponse.success(chatService.getHistory(sessionId));
    }

    @GetMapping("/sessions")
    public ApiResponse<java.util.List<ConversationHistory>> getUserSessions(Authentication authentication) {
        String userId = authentication != null ? authentication.getName() : "anonymous";
        log.info("Listing sessions for user: {}", userId);
        return ApiResponse.success(chatService.getUserSessions(userId));
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ApiResponse<Void> deleteSession(
            @PathVariable String sessionId,
            Authentication authentication) {
        log.info("Deleting session: {} for user: {}", sessionId, authentication.getName());
        chatService.deleteSession(sessionId);
        return ApiResponse.success(null);
    }

    @PostMapping("/recommend")
    public ApiResponse<RecommendationResponse> recommend(
            @Valid @RequestBody RecommendationRequest request,
            Authentication authentication) {
        String userId = authentication != null ? authentication.getName() : "anonymous";
        log.info("Recommendation request from user: {} for {}", userId, request.getDestination());
        return ApiResponse.success(chatService.recommend(userId, request));
    }

    @PostMapping("/recommend-packages")
    public ApiResponse<RecommendationResponse> recommendPackages(
            @Valid @RequestBody PackageRecommendationRequest request,
            Authentication authentication) {
        String userId = authentication != null ? authentication.getName() : "anonymous";
        log.info("Package recommendation request from user: {} for {}", userId, request.getDestination());
        return ApiResponse.success(chatService.recommendPackages(userId, request));
    }

    @PostMapping(value = "/generate-plan", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<ChatStreamEvent>> generatePlan(
            @Valid @RequestBody TripPlanRequest request,
            Authentication authentication) {
        String userId = authentication != null ? authentication.getName() : "anonymous";
        log.info("Generate plan request from user: {} for {}", userId, request.getDestination());
        return Flux.defer(() -> chatService.generatePlan(userId, request))
                .onErrorResume(error -> {
                    log.error("Generate plan endpoint error: {}", error.getMessage());
                    return Flux.just(ServerSentEvent.<ChatStreamEvent>builder()
                            .event("error")
                            .data(ChatStreamEvent.error("Service temporarily unavailable. Please try again."))
                            .build());
                });
    }
}
