package com.travelplan.aiagent.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplan.aiagent.dto.ConversationHistory;
import com.travelplan.aiagent.dto.ConversationHistory.ChatMessage;
import com.travelplan.aiagent.dto.ProviderResult;
import com.travelplan.aiagent.dto.QuickReplyChip;
import com.travelplan.aiagent.entity.ChatMessageEntity;
import com.travelplan.aiagent.entity.ChatSessionEntity;
import com.travelplan.aiagent.repository.ChatMessageRepository;
import com.travelplan.aiagent.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionManager {

    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final ObjectMapper objectMapper;

    // In-memory cache for active sessions (fast read during streaming)
    private final ConcurrentHashMap<String, ConversationHistory> cache = new ConcurrentHashMap<>();

    @Value("${agent.session.ttl-minutes:30}")
    private int sessionTtlMinutes;

    @Value("${agent.session.max-history:50}")
    private int maxHistory;

    @Transactional
    public String getOrCreateSession(String sessionId, String userId) {
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = UUID.randomUUID().toString();
        }

        String finalSessionId = sessionId;

        // Check cache first
        if (cache.containsKey(finalSessionId)) {
            return finalSessionId;
        }

        // Check DB
        Optional<ChatSessionEntity> existing = sessionRepository.findBySessionId(finalSessionId);
        if (existing.isPresent()) {
            // Load from DB into cache
            ChatSessionEntity entity = existing.get();
            loadSessionToCache(entity);
            return finalSessionId;
        }

        // Create new session in DB
        ChatSessionEntity newSession = ChatSessionEntity.builder()
                .sessionId(finalSessionId)
                .userId(userId)
                .build();
        sessionRepository.save(newSession);

        // Also cache it
        cache.put(finalSessionId, ConversationHistory.builder()
                .sessionId(finalSessionId)
                .messages(Collections.synchronizedList(new ArrayList<>()))
                .createdAt(Instant.now())
                .lastActivityAt(Instant.now())
                .build());

        return finalSessionId;
    }

    @Transactional
    public void addUserMessage(String sessionId, String content) {
        addMessage(sessionId, "user", content, null, null);
    }

    @Transactional
    public void addAssistantMessage(String sessionId, String content, List<QuickReplyChip> quickReplies) {
        addAssistantMessage(sessionId, content, quickReplies, null);
    }

    @Transactional
    public void addAssistantMessage(String sessionId, String content, List<QuickReplyChip> quickReplies, List<ProviderResult> providers) {
        addMessage(sessionId, "assistant", content, quickReplies, providers);
    }

    private void addMessage(String sessionId, String role, String content, List<QuickReplyChip> quickReplies, List<ProviderResult> providers) {
        Instant now = Instant.now();

        // Persist to DB
        ChatMessageEntity messageEntity = ChatMessageEntity.builder()
                .sessionId(sessionId)
                .role(role)
                .content(content)
                .quickRepliesJson(toJson(quickReplies))
                .providersJson(toJson(providers))
                .build();
        messageRepository.save(messageEntity);

        // Update session last activity and auto-generate title from first user message
        sessionRepository.findBySessionId(sessionId).ifPresent(session -> {
            session.setLastActivityAt(now);
            if (session.getTitle() == null && "user".equals(role) && content != null) {
                session.setTitle(content.length() > 100 ? content.substring(0, 100) + "..." : content);
            }
            sessionRepository.save(session);
        });

        // Update cache
        ConversationHistory history = cache.get(sessionId);
        if (history != null) {
            history.getMessages().add(ChatMessage.builder()
                    .role(role)
                    .content(content)
                    .timestamp(now)
                    .quickReplies(quickReplies)
                    .providers(providers)
                    .build());
            history.setLastActivityAt(now);

            // Trim cache if exceeds max
            if (history.getMessages().size() > maxHistory) {
                List<ChatMessage> messages = history.getMessages();
                int excess = messages.size() - maxHistory;
                messages.subList(0, excess).clear();
            }
        }
    }

    public ConversationHistory getHistory(String sessionId) {
        // Try cache first
        ConversationHistory cached = cache.get(sessionId);
        if (cached != null) {
            return cached;
        }

        // Load from DB
        return sessionRepository.findBySessionId(sessionId)
                .map(this::loadSessionToCache)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<ConversationHistory> getUserSessions(String userId) {
        return sessionRepository.findByUserIdOrderByLastActivityAtDesc(userId).stream()
                .map(entity -> ConversationHistory.builder()
                        .sessionId(entity.getSessionId())
                        .messages(List.of()) // Don't load messages for list view
                        .createdAt(entity.getCreatedAt())
                        .lastActivityAt(entity.getLastActivityAt())
                        .title(entity.getTitle())
                        .build())
                .toList();
    }

    @Transactional
    public void deleteSession(String sessionId) {
        cache.remove(sessionId);
        messageRepository.deleteBySessionId(sessionId);
        sessionRepository.deleteBySessionId(sessionId);
        log.info("Deleted session: {}", sessionId);
    }

    public List<ConversationHistory.ChatMessage> getRecentMessages(String sessionId, int count) {
        ConversationHistory history = getHistory(sessionId);
        if (history == null || history.getMessages().isEmpty()) {
            return Collections.emptyList();
        }

        List<ChatMessage> messages = history.getMessages();
        int fromIndex = Math.max(0, messages.size() - count);
        return new ArrayList<>(messages.subList(fromIndex, messages.size()));
    }

    public boolean sessionExists(String sessionId) {
        return cache.containsKey(sessionId) || sessionRepository.findBySessionId(sessionId).isPresent();
    }

    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void cleanExpiredSessions() {
        Instant cutoff = Instant.now().minus(sessionTtlMinutes, ChronoUnit.MINUTES);
        int before = cache.size();
        cache.entrySet().removeIf(entry ->
                entry.getValue().getLastActivityAt().isBefore(cutoff));
        int removed = before - cache.size();
        if (removed > 0) {
            log.info("Evicted {} inactive sessions from cache. Cached: {}", removed, cache.size());
        }
    }

    public int getActiveSessionCount() {
        return cache.size();
    }

    private ConversationHistory loadSessionToCache(ChatSessionEntity entity) {
        List<ChatMessageEntity> messageEntities = messageRepository.findBySessionIdOrderByCreatedAtAsc(entity.getSessionId());

        List<ChatMessage> messages = Collections.synchronizedList(new ArrayList<>());
        for (ChatMessageEntity msg : messageEntities) {
            messages.add(ChatMessage.builder()
                    .role(msg.getRole())
                    .content(msg.getContent())
                    .timestamp(msg.getCreatedAt())
                    .quickReplies(fromJson(msg.getQuickRepliesJson(), new TypeReference<>() {}))
                    .providers(fromJson(msg.getProvidersJson(), new TypeReference<>() {}))
                    .build());
        }

        // Only keep last maxHistory messages in cache
        if (messages.size() > maxHistory) {
            int excess = messages.size() - maxHistory;
            messages.subList(0, excess).clear();
        }

        ConversationHistory history = ConversationHistory.builder()
                .sessionId(entity.getSessionId())
                .messages(messages)
                .createdAt(entity.getCreatedAt())
                .lastActivityAt(entity.getLastActivityAt())
                .title(entity.getTitle())
                .build();

        cache.put(entity.getSessionId(), history);
        return history;
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize to JSON", e);
            return null;
        }
    }

    private <T> T fromJson(String json, TypeReference<T> typeRef) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize JSON", e);
            return null;
        }
    }
}
