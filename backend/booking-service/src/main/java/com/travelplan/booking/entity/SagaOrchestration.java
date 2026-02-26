package com.travelplan.booking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "saga_orchestration")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SagaOrchestration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "saga_state", nullable = false, length = 50)
    private String sagaState;

    @Column(name = "current_step", nullable = false)
    private int currentStep;

    @Column(name = "total_steps", nullable = false)
    private int totalSteps;

    @Column(name = "completed_steps")
    private String completedSteps;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "timeout_at")
    private Instant timeoutAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        if (sagaState == null) {
            sagaState = "INITIATED";
        }
        if (timeoutAt == null) {
            timeoutAt = Instant.now().plusSeconds(300); // 5 min timeout
        }
    }

    public void markStepCompleted(int step, String providerInfo) {
        this.currentStep = step;
        String entry = step + ":" + providerInfo;
        this.completedSteps = (completedSteps == null || completedSteps.isEmpty())
                ? entry
                : completedSteps + "," + entry;
    }

    public void markCompleted() {
        this.sagaState = "COMPLETED";
        this.completedAt = Instant.now();
    }

    public void markFailed(String reason) {
        this.sagaState = "FAILED";
        this.failureReason = reason;
        this.completedAt = Instant.now();
    }

    public void markRollingBack() {
        this.sagaState = "ROLLING_BACK";
    }

    public void markRolledBack() {
        this.sagaState = "ROLLED_BACK";
        this.completedAt = Instant.now();
    }
}
