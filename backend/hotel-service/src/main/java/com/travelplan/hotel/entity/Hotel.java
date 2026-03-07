package com.travelplan.hotel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Hotel Entity - Represents a hotel in the travel platform
 * This entity stores all hotel information including location, ratings, and amenities
 * Mapped to the "hotels" table in the PostgreSQL database
 */
@Entity
@Table(name = "hotels")
@Data // Lombok: Generates getters, setters, toString, equals, and hashCode
@Builder // Lombok: Implements builder pattern for object creation
@NoArgsConstructor // Lombok: Generates no-args constructor (required by JPA)
@AllArgsConstructor // Lombok: Generates constructor with all fields
public class Hotel {

    // Primary key - Auto-generated unique identifier for each hotel
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Owner identification - Links hotel to the owner (hotel provider)
    // This is the Supabase user ID of the hotel owner
    @Column(nullable = false, name = "owner_id")
    private String ownerId;

    // Hotel basic information
    @Column(nullable = false)
    private String name; // Hotel name (e.g., "Grand Hotel Colombo")

    @Column(columnDefinition = "TEXT")
    private String description; // Detailed description of the hotel

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address; // Full street address

    @Column(nullable = false, length = 100)
    private String city; // City name for searching and filtering

    // Geographic coordinates for map display and location-based searches
    @Column(precision = 10, scale = 8)
    private BigDecimal latitude; // Latitude coordinate (-90 to +90)

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude; // Longitude coordinate (-180 to +180)

    // Hotel classification - Star rating (1-5 stars)
    @Column(name = "star_rating")
    private Integer starRating;

    // Review metrics - Updated by review-service via events
    @Column(name = "average_rating", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal averageRating = BigDecimal.ZERO; // Average rating from reviews (0.00 to 5.00)

    @Column(name = "review_count")
    @Builder.Default
    private Integer reviewCount = 0; // Total number of reviews

    // Hotel amenities stored as PostgreSQL array
    // Examples: ["WiFi", "Pool", "Gym", "Restaurant", "Parking"]
    @Column(name = "amenities", columnDefinition = "TEXT[]")
    private String[] amenities;

    // Check-in and check-out times
    @Column(name = "check_in_time")
    @Builder.Default
    private LocalTime checkInTime = LocalTime.of(14, 0); // Default: 2:00 PM

    @Column(name = "check_out_time")
    @Builder.Default
    private LocalTime checkOutTime = LocalTime.of(11, 0); // Default: 11:00 AM

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    // Status flag - Inactive hotels won't appear in searches
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // Audit timestamps
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now(); // When the hotel was added to the system

    @Column(name = "updated_at")
    @Builder.Default
    private Instant updatedAt = Instant.now(); // Last modification timestamp

    // One-to-Many relationship with Room entity
    // CascadeType.ALL: All operations (persist, merge, remove) cascade to rooms
    // orphanRemoval: Automatically delete rooms when removed from this collection
    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Room> rooms = new ArrayList<>();

    /**
     * JPA lifecycle callback - Automatically updates the 'updated_at' timestamp
     * before updating the entity in the database
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
