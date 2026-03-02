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
        return chatService.chat(userId, request);
    }

    @GetMapping("/history")
    public ApiResponse<ConversationHistory> getHistory(
            @RequestParam String sessionId) {
        log.info("Getting history for session: {}", sessionId);
        return ApiResponse.success(chatService.getHistory(sessionId));
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
        return chatService.generatePlan(userId, request);
    }
}
