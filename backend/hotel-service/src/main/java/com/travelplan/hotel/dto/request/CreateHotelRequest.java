package com.travelplan.hotel.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateHotelRequest {

    @NotBlank(message = "Hotel name is required")
    @Size(max = 255, message = "Hotel name cannot exceed 255 characters")
    private String name;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City name cannot exceed 100 characters")
    private String city;

    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private BigDecimal longitude;

    @Min(value = 1, message = "Star rating must be between 1 and 5")
    @Max(value = 5, message = "Star rating must be between 1 and 5")
    private Integer starRating;

    private List<String> amenities;

    private String imageUrl;

    private LocalTime checkInTime;

    private LocalTime checkOutTime;

    /** Optional: rooms to create together with the hotel in a single request. */
    @Valid
    private List<InlineRoomRequest> rooms;
}
