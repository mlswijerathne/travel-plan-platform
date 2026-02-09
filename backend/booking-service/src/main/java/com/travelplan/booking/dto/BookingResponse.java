package com.travelplan.booking.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private Long id;
    private String touristId;
    private String bookingReference;
    private Long itineraryId;
    private String status;
    private BigDecimal totalAmount;
    private Instant bookingDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private String notes;
    private String cancellationReason;
    private BigDecimal refundAmount;
    private String refundPolicy;
    private List<BookingItemResponse> items;
    private Instant createdAt;
    private Instant updatedAt;
}
