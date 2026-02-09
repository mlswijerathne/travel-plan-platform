package com.travelplan.booking.mapper;

import com.travelplan.booking.dto.*;
import com.travelplan.booking.entity.Booking;
import com.travelplan.booking.entity.BookingItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class BookingMapper {

    public BookingResponse toResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .touristId(booking.getTouristId())
                .bookingReference(booking.getBookingReference())
                .itineraryId(booking.getItineraryId())
                .status(booking.getStatus())
                .totalAmount(booking.getTotalAmount())
                .bookingDate(booking.getBookingDate())
                .startDate(booking.getStartDate())
                .endDate(booking.getEndDate())
                .notes(booking.getNotes())
                .cancellationReason(booking.getCancellationReason())
                .refundAmount(booking.getRefundAmount())
                .refundPolicy(booking.getRefundPolicy())
                .items(booking.getItems().stream()
                        .map(this::toItemResponse)
                        .toList())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }

    public BookingItemResponse toItemResponse(BookingItem item) {
        return BookingItemResponse.builder()
                .id(item.getId())
                .providerType(item.getProviderType())
                .providerId(item.getProviderId())
                .itemName(item.getItemName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .startDate(item.getStartDate())
                .endDate(item.getEndDate())
                .status(item.getStatus())
                .createdAt(item.getCreatedAt())
                .build();
    }

    public BookingItem toBookingItem(BookingItemRequest request) {
        BigDecimal subtotal = request.getUnitPrice()
                .multiply(BigDecimal.valueOf(request.getQuantity()));

        return BookingItem.builder()
                .providerType(request.getProviderType().toUpperCase())
                .providerId(request.getProviderId())
                .itemName(request.getItemName())
                .quantity(request.getQuantity())
                .unitPrice(request.getUnitPrice())
                .subtotal(subtotal)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status("PENDING")
                .build();
    }

    public List<BookingResponse> toResponseList(List<Booking> bookings) {
        return bookings.stream()
                .map(this::toResponse)
                .toList();
    }
}
