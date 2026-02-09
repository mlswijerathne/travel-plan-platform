package com.travelplan.aiagent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatStreamEvent {

    public enum EventType {
        TEXT_DELTA,
        TOOL_CALL,
        AGENT_TRANSFER,
        FINAL_RESPONSE,
        ERROR,
        DONE
    }

    private EventType type;
    private String content;
    private String agentName;
    private String toolName;
    private List<QuickReplyChip> quickReplies;
    private List<ProviderResult> providers;
    private String sessionId;

    public static ChatStreamEvent textDelta(String content) {
        return ChatStreamEvent.builder()
                .type(EventType.TEXT_DELTA)
                .content(content)
                .build();
    }

    public static ChatStreamEvent toolCall(String toolName, String agentName) {
        return ChatStreamEvent.builder()
                .type(EventType.TOOL_CALL)
                .toolName(toolName)
                .agentName(agentName)
                .build();
    }

    public static ChatStreamEvent agentTransfer(String agentName) {
        return ChatStreamEvent.builder()
                .type(EventType.AGENT_TRANSFER)
                .agentName(agentName)
                .build();
    }

    public static ChatStreamEvent finalResponse(String content, List<QuickReplyChip> quickReplies,
                                                  List<ProviderResult> providers, String sessionId) {
        return ChatStreamEvent.builder()
                .type(EventType.FINAL_RESPONSE)
                .content(content)
                .quickReplies(quickReplies)
                .providers(providers)
                .sessionId(sessionId)
                .build();
    }

    public static ChatStreamEvent error(String message) {
        return ChatStreamEvent.builder()
                .type(EventType.ERROR)
                .content(message)
                .build();
    }

    public static ChatStreamEvent done(String sessionId) {
        return ChatStreamEvent.builder()
                .type(EventType.DONE)
                .sessionId(sessionId)
                .build();
    }
}
