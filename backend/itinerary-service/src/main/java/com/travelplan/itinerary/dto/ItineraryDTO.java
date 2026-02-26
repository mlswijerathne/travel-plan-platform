package com.travelplan.itinerary.dto;

import com.travelplan.itinerary.model.TripStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItineraryDTO {
    private Long id;
    private String touristId;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private TripStatus status;
    private BigDecimal totalBudget;
    private BigDecimal actualSpent;
    private List<ItineraryDayDTO> days = new ArrayList<>();
    private List<ExpenseDTO> expenses = new ArrayList<>();
}
