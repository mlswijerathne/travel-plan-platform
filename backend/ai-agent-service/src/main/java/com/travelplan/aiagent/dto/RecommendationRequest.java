package com.travelplan.aiagent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationRequest {
    @NotBlank(message = "Destination is required")
    private String destination;
    private Integer duration;
    private Double budget;
    private List<String> interests;
    private String travelStyle;
}
