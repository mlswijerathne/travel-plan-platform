package com.travelplan.ecommerce.dto;

import com.travelplan.ecommerce.enums.DeliveryType;
import lombok.Data;
import java.util.List;

@Data
public class OrderRequestDTO {
    private DeliveryType deliveryType;
    private String deliveryLocationId; // Hotel ID if dropping off
    private String itineraryId;        // Context for their trip
    private List<OrderItemRequestDTO> items;
}