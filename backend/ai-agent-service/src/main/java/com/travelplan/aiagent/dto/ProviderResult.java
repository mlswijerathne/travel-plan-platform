package com.travelplan.aiagent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderResult {
    private String id;
    private String name;
    private String type;
    private String description;
    private Double price;
    private Double rating;
    private String location;
    private String imageUrl;
    private String source;
}
