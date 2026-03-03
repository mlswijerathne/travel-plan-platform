package com.travelplan.ecommerce.repository;

import com.travelplan.ecommerce.entity.Order;
import com.travelplan.ecommerce.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByTouristId(String touristId, Pageable pageable);

    Optional<Order> findByIdAndTouristId(Long id, String touristId);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
}