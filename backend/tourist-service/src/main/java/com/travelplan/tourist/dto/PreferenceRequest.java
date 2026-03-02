package com.travelplan.tourist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreferenceRequest {
    private String preferredBudget;
    private String travelStyle;
    private List<String> dietaryRestrictions;
    private List<String> interests;
    private List<String> preferredLanguages;
    private String accessibilityNeeds;
}
