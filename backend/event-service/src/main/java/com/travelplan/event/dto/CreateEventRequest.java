package com.travelplan.event.dto;

import com.travelplan.event.model.enums.EventCategory;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEventRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255)
    private String title;

    private String description;

    @NotNull(message = "Category is required")
    private EventCategory category;

    @NotBlank(message = "Location is required")
    private String location;

    private BigDecimal latitude;
    private BigDecimal longitude;

    @NotNull(message = "Start date time is required")
    @Future(message = "Start date must be in the future")
    private OffsetDateTime startDateTime;

    @NotNull(message = "End date time is required")
    private OffsetDateTime endDateTime;

    @NotNull(message = "Total capacity is required")
    @Min(value = 1, message = "Total capacity must be at least 1")
    private Integer totalCapacity;

    @NotNull(message = "Ticket price is required")
    @DecimalMin(value = "0.0", message = "Ticket price cannot be negative")
    private BigDecimal ticketPrice;

    @NotBlank
    @Builder.Default
    private String currency = "LKR";

    private String imageUrl;
    private String tags;

    @AssertTrue(message = "End date must be after start date")
    private boolean isDateRangeValid() {
        return startDateTime != null && endDateTime != null && endDateTime.isAfter(startDateTime);
    }
}
