package com.travelplan.aiagent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CostBreakdown {
    private Double totalEstimatedCost;
    private Double accommodationCost;
    private Double transportCost;
    private Double activityCost;
    private Double foodCost;
    private Double miscCost;
    private String currency;
    private List<SavingSuggestion> savingSuggestions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SavingSuggestion {
        private String category;
        private String suggestion;
        private Double potentialSaving;
    }
}
