package com.travelplan.aiagent.service;

import com.google.adk.events.Event;
import com.google.adk.sessions.Session;
import com.travelplan.aiagent.dto.*;
import com.travelplan.aiagent.service.impl.ChatServiceImpl;
import io.reactivex.rxjava3.core.Flowable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    @Mock
    private SessionManager sessionManager;

    @Mock
    private AgentRunner agentRunner;

    @Mock
    private StreamingService streamingService;

    @InjectMocks
    private ChatServiceImpl chatService;

    @Mock
    private Session mockSession;

    @BeforeEach
    void setUp() {
        lenient().when(mockSession.userId()).thenReturn("user1");
        lenient().when(mockSession.id()).thenReturn("session-1");
    }

    @Test
    void chat_createsSessionAndRunsAgent() {
        ChatRequest request = ChatRequest.builder()
                .message("Hello")
                .sessionId("session-1")
                .build();

        when(sessionManager.getOrCreateSession("session-1", "user1")).thenReturn("session-1");
        when(agentRunner.getOrCreateSession("session-1", "user1")).thenReturn(mockSession);
        when(agentRunner.runAsync("user1", "session-1", "Hello")).thenReturn(Flowable.empty());

        ChatStreamEvent doneEvent = ChatStreamEvent.done("session-1");
        ServerSentEvent<ChatStreamEvent> sse = ServerSentEvent.<ChatStreamEvent>builder()
                .event("done")
                .data(doneEvent)
                .build();
        when(streamingService.convertToSSE(any(), eq("session-1")))
                .thenReturn(Flux.just(sse));

        Flux<ServerSentEvent<ChatStreamEvent>> result = chatService.chat("user1", request);

        StepVerifier.create(result)
                .expectNextCount(1)
                .verifyComplete();

        verify(sessionManager).getOrCreateSession("session-1", "user1");
        verify(sessionManager).addUserMessage("session-1", "Hello");
    }

    @Test
    void chat_withNullSessionId_createsNewSession() {
        ChatRequest request = ChatRequest.builder()
                .message("Hello")
                .build();

        when(sessionManager.getOrCreateSession(null, "user1")).thenReturn("new-session");
        when(agentRunner.getOrCreateSession("new-session", "user1")).thenReturn(mockSession);
        when(agentRunner.runAsync(anyString(), anyString(), anyString())).thenReturn(Flowable.empty());
        when(streamingService.convertToSSE(any(), eq("new-session"))).thenReturn(Flux.empty());

        chatService.chat("user1", request);

        verify(sessionManager).getOrCreateSession(null, "user1");
    }

    @Test
    void getHistory_returnsExistingHistory() {
        ConversationHistory history = ConversationHistory.builder()
                .sessionId("session-1")
                .messages(List.of())
                .build();

        when(sessionManager.getHistory("session-1")).thenReturn(history);

        ConversationHistory result = chatService.getHistory("session-1");
        assertEquals("session-1", result.getSessionId());
    }

    @Test
    void getHistory_nonExistentSession_returnsEmptyHistory() {
        when(sessionManager.getHistory("nonexistent")).thenReturn(null);

        ConversationHistory result = chatService.getHistory("nonexistent");
        assertEquals("nonexistent", result.getSessionId());
        assertTrue(result.getMessages().isEmpty());
    }

    @Test
    void recommend_buildsQueryAndReturnsResponse() {
        RecommendationRequest request = RecommendationRequest.builder()
                .destination("Colombo")
                .duration(5)
                .budget(1000.0)
                .interests(List.of("Culture", "Food"))
                .travelStyle("Adventure")
                .build();

        when(sessionManager.getOrCreateSession(isNull(), eq("user1"))).thenReturn("rec-session");
        when(agentRunner.getOrCreateSession("rec-session", "user1")).thenReturn(mockSession);
        when(agentRunner.runAsync(anyString(), anyString(), anyString()))
                .thenReturn(Flowable.empty());
        when(streamingService.extractQuickReplyChips(anyString())).thenReturn(List.of());
        when(streamingService.removeChipMarkup(anyString())).thenReturn("");

        RecommendationResponse response = chatService.recommend("user1", request);
        assertNotNull(response);
    }

    @Test
    void recommendPackages_buildsQueryAndReturnsResponse() {
        PackageRecommendationRequest request = PackageRecommendationRequest.builder()
                .destination("Kandy")
                .duration(3)
                .minBudget(500.0)
                .maxBudget(1500.0)
                .travelers(2)
                .interests(List.of("Nature"))
                .build();

        when(sessionManager.getOrCreateSession(isNull(), eq("user1"))).thenReturn("pkg-session");
        when(agentRunner.getOrCreateSession("pkg-session", "user1")).thenReturn(mockSession);
        when(agentRunner.runAsync(anyString(), anyString(), anyString()))
                .thenReturn(Flowable.empty());
        when(streamingService.extractQuickReplyChips(anyString())).thenReturn(List.of());
        when(streamingService.removeChipMarkup(anyString())).thenReturn("");

        RecommendationResponse response = chatService.recommendPackages("user1", request);
        assertNotNull(response);
    }
}
