package com.travelplan.aiagent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationHistory {

    private String sessionId;
    private String title;
    private List<ChatMessage> messages;
    private Instant createdAt;
    private Instant lastActivityAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessage {
        private String role;
        private String content;
        private Instant timestamp;
        private List<QuickReplyChip> quickReplies;
        private List<ProviderResult> providers;
    }
}
