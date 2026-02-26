package com.travelplan.itinerary.controller;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateItineraryRequest {
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
}
