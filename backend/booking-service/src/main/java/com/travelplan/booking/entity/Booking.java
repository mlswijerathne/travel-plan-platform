package com.travelplan.booking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tourist_id", nullable = false)
    private String touristId;

    @Column(name = "itinerary_id")
    private Long itineraryId;

    @Column(name = "booking_reference", unique = true)
    private String bookingReference;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "booking_date")
    private Instant bookingDate;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "notes")
    private String notes;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "refund_amount", precision = 12, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "refund_policy", length = 50)
    private String refundPolicy;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BookingItem> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (bookingDate == null) {
            bookingDate = Instant.now();
        }
        if (status == null) {
            status = "PENDING";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public void addItem(BookingItem item) {
        items.add(item);
        item.setBooking(this);
    }
}
