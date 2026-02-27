package com.travelplan.itinerary.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.ZonedDateTime;

@Entity
@Table(name = "itinerary_activities", schema = "itinerary")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItineraryActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "day_id", nullable = false)
    private ItineraryDay day;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ActivityType activityType;

    @Column(length = 50)
    private String providerType;

    @Column
    private Long providerId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private LocalTime startTime;

    @Column
    private LocalTime endTime;

    @Column(columnDefinition = "TEXT")
    private String location;

    @Column(precision = 10, scale = 2)
    private BigDecimal estimatedCost;

    @Column
    private Long bookingId;

    @Column
    private Integer sortOrder;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now();
        if (sortOrder == null) {
            sortOrder = 0;
        }
    }
}
