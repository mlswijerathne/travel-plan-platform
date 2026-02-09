package com.travelplan.booking.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingItemResponse {
    private Long id;
    private String providerType;
    private Long providerId;
    private String itemName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Instant createdAt;
}
