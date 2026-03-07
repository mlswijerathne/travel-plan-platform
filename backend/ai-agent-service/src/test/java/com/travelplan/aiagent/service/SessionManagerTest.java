package com.travelplan.aiagent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplan.aiagent.dto.ConversationHistory;
import com.travelplan.aiagent.dto.QuickReplyChip;
import com.travelplan.aiagent.entity.ChatSessionEntity;
import com.travelplan.aiagent.repository.ChatMessageRepository;
import com.travelplan.aiagent.repository.ChatSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionManagerTest {

    @Mock
    private ChatSessionRepository sessionRepository;

    @Mock
    private ChatMessageRepository messageRepository;

    private SessionManager sessionManager;

    @BeforeEach
    void setUp() {
        sessionManager = new SessionManager(sessionRepository, messageRepository, new ObjectMapper());
        ReflectionTestUtils.setField(sessionManager, "sessionTtlMinutes", 30);
        ReflectionTestUtils.setField(sessionManager, "maxHistory", 50);

        // Stub: no existing session in DB, save returns the entity
        lenient().when(sessionRepository.findBySessionId(any())).thenReturn(Optional.empty());
        lenient().when(sessionRepository.save(any(ChatSessionEntity.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void getOrCreateSession_withNullSessionId_createsNewSession() {
        String sessionId = sessionManager.getOrCreateSession(null, "user1");
        assertNotNull(sessionId);
        assertTrue(sessionManager.sessionExists(sessionId));
    }

    @Test
    void getOrCreateSession_withExistingSessionId_returnsSameSession() {
        String sessionId = sessionManager.getOrCreateSession("test-session", "user1");
        assertEquals("test-session", sessionId);
        assertTrue(sessionManager.sessionExists("test-session"));
    }

    @Test
    void getOrCreateSession_calledTwice_reusesSession() {
        String first = sessionManager.getOrCreateSession("session-1", "user1");
        String second = sessionManager.getOrCreateSession("session-1", "user1");
        assertEquals(first, second);
        assertEquals(1, sessionManager.getActiveSessionCount());
    }

    @Test
    void addUserMessage_addsToHistory() {
        String sessionId = sessionManager.getOrCreateSession("session-1", "user1");
        sessionManager.addUserMessage(sessionId, "Hello");

        ConversationHistory history = sessionManager.getHistory(sessionId);
        assertNotNull(history);
        assertEquals(1, history.getMessages().size());
        assertEquals("user", history.getMessages().get(0).getRole());
        assertEquals("Hello", history.getMessages().get(0).getContent());
    }

    @Test
    void addAssistantMessage_addsToHistory() {
        String sessionId = sessionManager.getOrCreateSession("session-1", "user1");
        List<QuickReplyChip> chips = List.of(
                QuickReplyChip.builder().label("Option 1").value("opt1").build()
        );
        sessionManager.addAssistantMessage(sessionId, "Hi there!", chips);

        ConversationHistory history = sessionManager.getHistory(sessionId);
        assertEquals(1, history.getMessages().size());
        assertEquals("assistant", history.getMessages().get(0).getRole());
        assertEquals("Hi there!", history.getMessages().get(0).getContent());
        assertEquals(1, history.getMessages().get(0).getQuickReplies().size());
    }

    @Test
    void getRecentMessages_returnsLastNMessages() {
        String sessionId = sessionManager.getOrCreateSession("session-1", "user1");
        sessionManager.addUserMessage(sessionId, "Message 1");
        sessionManager.addAssistantMessage(sessionId, "Response 1", null);
        sessionManager.addUserMessage(sessionId, "Message 2");
        sessionManager.addAssistantMessage(sessionId, "Response 2", null);

        var recent = sessionManager.getRecentMessages(sessionId, 2);
        assertEquals(2, recent.size());
        assertEquals("Message 2", recent.get(0).getContent());
        assertEquals("Response 2", recent.get(1).getContent());
    }

    @Test
    void getRecentMessages_nonExistentSession_returnsEmpty() {
        var recent = sessionManager.getRecentMessages("nonexistent", 5);
        assertTrue(recent.isEmpty());
    }

    @Test
    void maxHistory_trimsOldMessages() {
        ReflectionTestUtils.setField(sessionManager, "maxHistory", 3);
        String sessionId = sessionManager.getOrCreateSession("session-1", "user1");

        sessionManager.addUserMessage(sessionId, "Message 1");
        sessionManager.addAssistantMessage(sessionId, "Response 1", null);
        sessionManager.addUserMessage(sessionId, "Message 2");
        sessionManager.addAssistantMessage(sessionId, "Response 2", null);

        ConversationHistory history = sessionManager.getHistory(sessionId);
        assertEquals(3, history.getMessages().size());
        // First message should have been trimmed
        assertEquals("Response 1", history.getMessages().get(0).getContent());
    }

    @Test
    void cleanExpiredSessions_removesOldSessions() {
        String sessionId = sessionManager.getOrCreateSession("old-session", "user1");
        // Manually set the lastActivityAt to a time in the past
        ConversationHistory history = sessionManager.getHistory(sessionId);
        history.setLastActivityAt(java.time.Instant.now().minus(60, java.time.temporal.ChronoUnit.MINUTES));

        sessionManager.cleanExpiredSessions();
        assertFalse(sessionManager.sessionExists("old-session"));
    }

    @Test
    void getHistory_nonExistentSession_returnsNull() {
        assertNull(sessionManager.getHistory("nonexistent"));
    }
}
