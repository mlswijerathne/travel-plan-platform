package com.travelplan.ecommerce.mapper;

import com.travelplan.ecommerce.dto.ProductDTO;
import com.travelplan.ecommerce.entity.Product;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductMapper {

    public ProductDTO toDTO(Product product) {
        if (product == null) {
            return null;
        }

        // 1. Safely convert Enum to String
        String categoryStr = product.getCategory() != null ? product.getCategory().name() : null;
        
        // 2. Safely grab the first image from the List for the frontend
        String imageUrl = null;
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            imageUrl = product.getImages().get(0);
        }

        // 3. Create the Record using its constructor (no setters allowed!)
        return new ProductDTO(
            product.getId(),
            product.getName(),
            categoryStr,
            product.getDescription(),
            product.getPrice(),
            product.getStockQuantity(),
            imageUrl,
            product.getAverageRating(),
            product.getReviewCount(),
            product.getIsActive()
        );
    }

    public List<ProductDTO> toDTOList(List<Product> products) {
        if (products == null) {
            return null;
        }
        return products.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}