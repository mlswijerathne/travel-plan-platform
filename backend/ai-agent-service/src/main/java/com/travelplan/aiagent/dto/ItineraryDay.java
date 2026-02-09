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
public class ItineraryDay {
    private int dayNumber;
    private String date;
    private String title;
    private String description;
    private List<Activity> activities;
    private AccommodationInfo accommodation;
    private TransportInfo transport;
    private Double estimatedCost;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Activity {
        private String name;
        private String description;
        private String time;
        private String location;
        private Double estimatedCost;
        private String type;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccommodationInfo {
        private String name;
        private String type;
        private String location;
        private Double pricePerNight;
        private String source;
        private String providerId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransportInfo {
        private String type;
        private String from;
        private String to;
        private Double estimatedCost;
        private String vehicleType;
        private String source;
        private String providerId;
    }
}
