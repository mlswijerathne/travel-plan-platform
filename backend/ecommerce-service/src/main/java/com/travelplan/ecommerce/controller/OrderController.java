package com.travelplan.ecommerce.controller;

import com.travelplan.ecommerce.dto.OrderRequestDTO;
import com.travelplan.ecommerce.entity.Order;
import com.travelplan.ecommerce.enums.OrderStatus;
import com.travelplan.ecommerce.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:3000")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> createOrder(
            @RequestBody OrderRequestDTO request, 
            @RequestHeader(value = "X-Tourist-Id", defaultValue = "test-tourist-123") String touristId) {
        
        Order order = orderService.createOrder(request, touristId);
        return ResponseEntity.ok(order);
    }

    @GetMapping
    public ResponseEntity<Page<Order>> getTouristOrders(
            @RequestHeader(value = "X-Tourist-Id", defaultValue = "test-tourist-123") String touristId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<Order> orders = orderService.getTouristOrders(touristId, PageRequest.of(page, size));
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        
        OrderStatus status = OrderStatus.valueOf(request.get("status").toUpperCase());
        Order updatedOrder = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(updatedOrder);
    }
}