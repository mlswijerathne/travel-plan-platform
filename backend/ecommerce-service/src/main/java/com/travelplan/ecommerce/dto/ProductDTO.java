package com.travelplan.ecommerce.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductDTO(
        Long id,
        String name,
        String category,
        String description,
        BigDecimal price,
        Integer stockQuantity,
        String imageUrl,
        BigDecimal averageRating,
        Integer reviewCount,
        Boolean isActive) {
}
