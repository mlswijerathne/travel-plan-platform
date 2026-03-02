package com.travelplan.ecommerce.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ProductCategory {
    SOUVENIRS, CLOTHING, FOOD, ACCESSORIES, BOOKS, CRAFTS, EVENT_MERCHANDISE, TRANSPORT;

    // This tells the JSON parser how to safely read the frontend's string
    @JsonCreator
    public static ProductCategory fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        // Convert strings like "Travel Gear" to "TRAVEL_GEAR"
        String normalizedValue = value.trim().toUpperCase().replace(" ", "_");
        
        for (ProductCategory category : ProductCategory.values()) {
            if (category.name().equals(normalizedValue)) {
                return category;
            }
        }
        
        // Safe fallback: if the frontend sends an unknown category, default to SOUVENIRS instead of crashing
        return SOUVENIRS; 
    }
}