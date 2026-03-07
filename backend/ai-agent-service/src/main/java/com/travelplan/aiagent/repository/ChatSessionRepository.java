package com.travelplan.aiagent.repository;

import com.travelplan.aiagent.entity.ChatSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSessionEntity, Long> {
    Optional<ChatSessionEntity> findBySessionId(String sessionId);

    List<ChatSessionEntity> findByUserIdOrderByLastActivityAtDesc(String userId);

    void deleteBySessionId(String sessionId);
}
