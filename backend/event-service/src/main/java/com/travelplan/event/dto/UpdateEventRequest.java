package com.travelplan.event.dto;

import com.travelplan.event.model.enums.EventCategory;
import com.travelplan.event.model.enums.EventStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventRequest {

    @Size(max = 255)
    private String title;

    private String description;
    private EventCategory category;
    private String location;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private OffsetDateTime startDateTime;
    private OffsetDateTime endDateTime;

    @Min(value = 1)
    private Integer totalCapacity;

    @DecimalMin(value = "0.0")
    private BigDecimal ticketPrice;

    private String currency;
    private EventStatus status;
    private String imageUrl;
    private String tags;
}
