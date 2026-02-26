package com.travelplan.hotel.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotelResponse {

    private Long id;
    private String ownerId;
    private String name;
    private String description;
    private String address;
    private String city;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer starRating;
    private BigDecimal averageRating;
    private Integer reviewCount;
    private List<String> amenities;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
    private List<RoomResponse> rooms;
}
