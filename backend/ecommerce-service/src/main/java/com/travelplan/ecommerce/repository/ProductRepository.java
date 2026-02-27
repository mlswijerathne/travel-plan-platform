package com.travelplan.ecommerce.repository;

import com.travelplan.ecommerce.entity.Product;
import com.travelplan.ecommerce.enums.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Upgraded search to include the new contextual requirements (Event ID)
    @Query("SELECT p FROM Product p WHERE " +
           "(:category IS NULL OR p.category = :category) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:eventId IS NULL OR p.eventId = :eventId) AND " +
           "p.isActive = true")
    List<Product> searchProducts(@Param("category") ProductCategory category,
                                 @Param("minPrice") BigDecimal minPrice,
                                 @Param("maxPrice") BigDecimal maxPrice,
                                 @Param("eventId") String eventId);
}