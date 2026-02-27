package com.travelplan.ecommerce.service;

import com.travelplan.ecommerce.client.HotelServiceClient;
import com.travelplan.ecommerce.dto.OrderItemRequestDTO;
import com.travelplan.ecommerce.dto.OrderRequestDTO;
import com.travelplan.ecommerce.entity.Order;
import com.travelplan.ecommerce.entity.OrderItem;
import com.travelplan.ecommerce.entity.Product;
import com.travelplan.ecommerce.enums.DeliveryType;
import com.travelplan.ecommerce.enums.OrderStatus;
import com.travelplan.ecommerce.repository.OrderRepository;
import com.travelplan.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private HotelServiceClient hotelServiceClient;

    @Transactional
    public Order createOrder(OrderRequestDTO request, String touristId) {
        // 1. Validate Hotel if dropping off
        if (request.getDeliveryType() == DeliveryType.HOTEL_DROP_OFF) {
            try {
                hotelServiceClient.verifyHotelExists(request.getDeliveryLocationId());
            } catch (Exception e) {
                throw new RuntimeException("Hotel verification failed. Please check your Hotel ID.");
            }
        }

        // 2. Setup Order details
        Order order = new Order();
        order.setTouristId(touristId);
        order.setDeliveryType(request.getDeliveryType());
        order.setDeliveryLocationId(request.getDeliveryLocationId());
        order.setItineraryId(request.getItineraryId());
        order.setStatus(OrderStatus.PENDING);

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();

        // 3. Process Shopping Cart Items
        for (OrderItemRequestDTO itemDto : request.getItems()) {
            Product p = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found ID: " + itemDto.getProductId()));

            // Check stock for physical items
            if (p.getIsPhysical() && p.getStockQuantity() < itemDto.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + p.getName());
            }

            // Deduct stock
            if (p.getIsPhysical()) {
                p.setStockQuantity(p.getStockQuantity() - itemDto.getQuantity());
                productRepository.save(p);
            }

            // Create Order Item
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductId(p.getId());
            orderItem.setQuantity(itemDto.getQuantity());
            orderItem.setUnitPrice(p.getPrice());

            BigDecimal subtotal = p.getPrice().multiply(new BigDecimal(itemDto.getQuantity()));
            orderItem.setSubtotal(subtotal);

            totalAmount = totalAmount.add(subtotal);
            items.add(orderItem);
        }

        order.setItems(items);
        order.setTotalAmount(totalAmount);

        return orderRepository.save(order);
    }

    public Page<Order> getTouristOrders(String touristId, Pageable pageable) {
        return orderRepository.findByTouristId(touristId, pageable);
    }

    public Order updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);
        return orderRepository.save(order);
    }
}