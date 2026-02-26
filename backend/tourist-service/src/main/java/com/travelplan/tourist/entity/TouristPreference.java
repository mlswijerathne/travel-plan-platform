package com.travelplan.tourist.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "tourist_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TouristPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tourist_id", nullable = false, unique = true)
    private Tourist tourist;

    @Column(name = "preferred_budget", length = 50)
    private String preferredBudget;

    @Column(name = "travel_style", length = 50)
    private String travelStyle;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "dietary_restrictions", columnDefinition = "text[]")
    private List<String> dietaryRestrictions;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "interests", columnDefinition = "text[]")
    private List<String> interests;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "preferred_languages", columnDefinition = "text[]")
    private List<String> preferredLanguages;

    @Column(name = "accessibility_needs")
    private String accessibilityNeeds;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
