package com.travelplan.hotel.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityResponse {

    private Long hotelId;
    private String hotelName;
    private boolean available;
    private Integer availableRooms;
    private String message;
}
