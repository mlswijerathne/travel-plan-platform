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
public class ChatResponse {
    private String message;
    private String sessionId;
    private List<QuickReplyChip> quickReplies;
    private List<ProviderResult> providers;
}
