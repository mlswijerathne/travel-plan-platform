package com.travelplan.aiagent.service;

import com.travelplan.aiagent.dto.ConversationHistory;
import com.travelplan.aiagent.dto.ConversationHistory.ChatMessage;
import com.travelplan.aiagent.dto.QuickReplyChip;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SessionManager {

    private final ConcurrentHashMap<String, ConversationHistory> sessions = new ConcurrentHashMap<>();

    @Value("${agent.session.ttl-minutes:30}")
    private int sessionTtlMinutes;

    @Value("${agent.session.max-history:50}")
    private int maxHistory;

    public String getOrCreateSession(String sessionId, String userId) {
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = UUID.randomUUID().toString();
        }

        String finalSessionId = sessionId;
        sessions.computeIfAbsent(finalSessionId, id -> ConversationHistory.builder()
                .sessionId(id)
                .messages(Collections.synchronizedList(new ArrayList<>()))
                .createdAt(Instant.now())
                .lastActivityAt(Instant.now())
                .build());

        return sessionId;
    }

    public void addUserMessage(String sessionId, String content) {
        ConversationHistory history = sessions.get(sessionId);
        if (history != null) {
            addMessage(history, "user", content, null);
        }
    }

    public void addAssistantMessage(String sessionId, String content, List<QuickReplyChip> quickReplies) {
        ConversationHistory history = sessions.get(sessionId);
        if (history != null) {
            addMessage(history, "assistant", content, quickReplies);
        }
    }

    private void addMessage(ConversationHistory history, String role, String content, List<QuickReplyChip> quickReplies) {
        history.getMessages().add(ChatMessage.builder()
                .role(role)
                .content(content)
                .timestamp(Instant.now())
                .quickReplies(quickReplies)
                .build());

        history.setLastActivityAt(Instant.now());

        // Trim history if it exceeds max
        if (history.getMessages().size() > maxHistory) {
            List<ChatMessage> messages = history.getMessages();
            int excess = messages.size() - maxHistory;
            messages.subList(0, excess).clear();
        }
    }

    public ConversationHistory getHistory(String sessionId) {
        return sessions.get(sessionId);
    }

    public List<ConversationHistory.ChatMessage> getRecentMessages(String sessionId, int count) {
        ConversationHistory history = sessions.get(sessionId);
        if (history == null || history.getMessages().isEmpty()) {
            return Collections.emptyList();
        }

        List<ChatMessage> messages = history.getMessages();
        int fromIndex = Math.max(0, messages.size() - count);
        return new ArrayList<>(messages.subList(fromIndex, messages.size()));
    }

    public boolean sessionExists(String sessionId) {
        return sessions.containsKey(sessionId);
    }

    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void cleanExpiredSessions() {
        Instant cutoff = Instant.now().minus(sessionTtlMinutes, ChronoUnit.MINUTES);
        int before = sessions.size();

        sessions.entrySet().removeIf(entry ->
                entry.getValue().getLastActivityAt().isBefore(cutoff));

        int removed = before - sessions.size();
        if (removed > 0) {
            log.info("Cleaned {} expired sessions. Active sessions: {}", removed, sessions.size());
        }
    }

    public int getActiveSessionCount() {
        return sessions.size();
    }
}
