package com.travelplan.aiagent.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplan.aiagent.dto.*;
import com.travelplan.aiagent.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ChatService chatService;

    @InjectMocks
    private ChatController chatController;

    private ObjectMapper objectMapper;

    private UsernamePasswordAuthenticationToken authentication;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(chatController).build();
        objectMapper = new ObjectMapper();
        authentication = new UsernamePasswordAuthenticationToken("user1", null, List.of());
    }

    @Test
    void getHistory_returnsConversationHistory() throws Exception {
        ConversationHistory history = ConversationHistory.builder()
                .sessionId("session-1")
                .messages(List.of(
                        ConversationHistory.ChatMessage.builder()
                                .role("user")
                                .content("Hello")
                                .build()
                ))
                .build();

        when(chatService.getHistory("session-1")).thenReturn(history);

        mockMvc.perform(get("/api/chat/history")
                        .param("sessionId", "session-1")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sessionId").value("session-1"))
                .andExpect(jsonPath("$.data.messages[0].role").value("user"))
                .andExpect(jsonPath("$.data.messages[0].content").value("Hello"));
    }

    @Test
    void recommend_returnsRecommendationResponse() throws Exception {
        RecommendationRequest request = RecommendationRequest.builder()
                .destination("Colombo")
                .duration(5)
                .budget(1000.0)
                .build();

        RecommendationResponse response = RecommendationResponse.builder()
                .summary("Here are my recommendations for Colombo")
                .quickReplies(List.of(
                        QuickReplyChip.builder().label("Show hotels").value("Show hotels").build()
                ))
                .build();

        when(chatService.recommend(eq("user1"), any(RecommendationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/chat/recommend")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.summary").value("Here are my recommendations for Colombo"))
                .andExpect(jsonPath("$.data.quickReplies[0].label").value("Show hotels"));
    }

    @Test
    void recommendPackages_returnsRecommendationResponse() throws Exception {
        PackageRecommendationRequest request = PackageRecommendationRequest.builder()
                .destination("Kandy")
                .duration(3)
                .build();

        RecommendationResponse response = RecommendationResponse.builder()
                .summary("Package recommendations for Kandy")
                .build();

        when(chatService.recommendPackages(eq("user1"), any(PackageRecommendationRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/chat/recommend-packages")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.summary").value("Package recommendations for Kandy"));
    }

    @Test
    void chat_returnsSseStream() throws Exception {
        ChatStreamEvent doneEvent = ChatStreamEvent.done("session-1");
        ServerSentEvent<ChatStreamEvent> sse = ServerSentEvent.<ChatStreamEvent>builder()
                .event("done")
                .data(doneEvent)
                .build();

        when(chatService.chat(eq("user1"), any(ChatRequest.class)))
                .thenReturn(Flux.just(sse));

        mockMvc.perform(post("/api/chat")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                ChatRequest.builder().message("Hello").build())))
                .andExpect(status().isOk());
    }
}
