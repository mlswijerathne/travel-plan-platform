package com.travelplan.ecommerce.repository;

import com.travelplan.ecommerce.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // Required to get order history for a specific tourist
    Page<Order> findByTouristId(String touristId, Pageable pageable);
}