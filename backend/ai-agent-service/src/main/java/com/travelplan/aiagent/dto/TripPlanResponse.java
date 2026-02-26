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
public class TripPlanResponse {
    private String summary;
    private List<ItineraryDay> itinerary;
    private CostBreakdown costBreakdown;
    private List<ProviderResult> recommendedProviders;
    private List<QuickReplyChip> quickReplies;
}
