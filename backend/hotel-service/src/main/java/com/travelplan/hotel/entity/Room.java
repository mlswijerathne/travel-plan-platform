package com.travelplan.hotel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Room Entity - Represents a room type in a hotel
 * Each hotel can have multiple room types (e.g., Standard, Deluxe, Suite)
 * Mapped to the "rooms" table in the PostgreSQL database
 */
@Entity
@Table(name = "rooms")
@Data // Lombok: Generates getters, setters, toString, equals, and hashCode
@Builder // Lombok: Implements builder pattern for object creation
@NoArgsConstructor // Lombok: Generates no-args constructor (required by JPA)
@AllArgsConstructor // Lombok: Generates constructor with all fields
public class Room {

    // Primary key - Auto-generated unique identifier for each room type
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many-to-One relationship with Hotel entity
    // FetchType.LAZY: Hotel data is loaded only when explicitly accessed (performance optimization)
    // A room must belong to exactly one hotel
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    // Room classification and details
    @Column(name = "room_type", nullable = false, length = 50)
    private String roomType; // e.g., "Standard", "Deluxe", "Suite", "Family Room"

    @Column(nullable = false, length = 100)
    private String name; // Room name (e.g., "Deluxe Ocean View")

    @Column(columnDefinition = "TEXT")
    private String description; // Detailed room description

    // Pricing information
    @Column(name = "price_per_night", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerNight; // Price per night in the local currency

    // Capacity information
    @Column(name = "max_occupancy", nullable = false)
    @Builder.Default
    private Integer maxOccupancy = 2; // Maximum number of guests (default: 2)

    // Room amenities stored as PostgreSQL array
    // Examples: ["TV", "Mini Bar", "Balcony", "Air Conditioning"]
    @Column(name = "amenities", columnDefinition = "TEXT[]")
    private String[] amenities;

    // Inventory tracking
    @Column(name = "total_rooms", nullable = false)
    @Builder.Default
    private Integer totalRooms = 1; // Total number of rooms of this type available

    // Status flag - Inactive rooms won't be available for booking
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // Audit timestamps
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now(); // When the room type was created

    @Column(name = "updated_at")
    @Builder.Default
    private Instant updatedAt = Instant.now(); // Last modification timestamp

    /**
     * JPA lifecycle callback - Automatically updates the 'updated_at' timestamp
     * before updating the entity in the database
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
