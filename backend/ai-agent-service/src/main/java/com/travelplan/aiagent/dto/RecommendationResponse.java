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
public class RecommendationResponse {
    private String summary;
    private List<ProviderResult> hotels;
    private List<ProviderResult> tourGuides;
    private List<ProviderResult> vehicles;
    private List<QuickReplyChip> quickReplies;
}
