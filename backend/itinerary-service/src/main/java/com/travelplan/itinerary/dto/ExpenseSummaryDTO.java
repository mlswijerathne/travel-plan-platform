package com.travelplan.itinerary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseSummaryDTO {
    private BigDecimal totalSpent;
    private BigDecimal remainingBudget;
    private BigDecimal totalBudget;
    private java.util.Map<String, BigDecimal> categoryBreakdown;
}
