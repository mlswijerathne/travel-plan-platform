package com.travelplan.booking.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingItemRequest {

    @NotBlank(message = "Provider type is required")
    private String providerType;

    @NotNull(message = "Provider ID is required")
    private Long providerId;

    @NotBlank(message = "Item name is required")
    private String itemName;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity = 1;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.01", message = "Unit price must be greater than 0")
    private BigDecimal unitPrice;

    private LocalDate startDate;

    private LocalDate endDate;
}
