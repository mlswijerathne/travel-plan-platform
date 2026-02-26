package com.travelplan.itinerary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItineraryDayDTO {
    private Long id;
    private Integer dayNumber;
    private LocalDate date;
    private String notes;
    private List<ItineraryActivityDTO> activities = new ArrayList<>();
}
