package com.travelplan.booking.mapper;

import com.travelplan.booking.dto.BookingItemRequest;
import com.travelplan.booking.dto.BookingItemResponse;
import com.travelplan.booking.dto.BookingResponse;
import com.travelplan.booking.entity.Booking;
import com.travelplan.booking.entity.BookingItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BookingMapperTest {

    private BookingMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new BookingMapper();
    }

    @Test
    void toResponse_mapsAllFields() {
        Booking booking = Booking.builder()
                .id(1L)
                .touristId("tourist-123")
                .bookingReference("TRP-20260209-ABC123")
                .itineraryId(5L)
                .status("CONFIRMED")
                .totalAmount(new BigDecimal("300.00"))
                .bookingDate(Instant.now())
                .startDate(LocalDate.of(2026, 3, 1))
                .endDate(LocalDate.of(2026, 3, 5))
                .notes("Special request")
                .cancellationReason(null)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .items(List.of())
                .build();

        BookingResponse response = mapper.toResponse(booking);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTouristId()).isEqualTo("tourist-123");
        assertThat(response.getBookingReference()).isEqualTo("TRP-20260209-ABC123");
        assertThat(response.getItineraryId()).isEqualTo(5L);
        assertThat(response.getStatus()).isEqualTo("CONFIRMED");
        assertThat(response.getTotalAmount()).isEqualByComparingTo("300.00");
        assertThat(response.getStartDate()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(response.getEndDate()).isEqualTo(LocalDate.of(2026, 3, 5));
        assertThat(response.getNotes()).isEqualTo("Special request");
        assertThat(response.getCancellationReason()).isNull();
    }

    @Test
    void toItemResponse_mapsAllFields() {
        BookingItem item = BookingItem.builder()
                .id(10L)
                .providerType("HOTEL")
                .providerId(5L)
                .itemName("Deluxe Room")
                .quantity(2)
                .unitPrice(new BigDecimal("100.00"))
                .subtotal(new BigDecimal("200.00"))
                .startDate(LocalDate.of(2026, 3, 1))
                .endDate(LocalDate.of(2026, 3, 5))
                .status("PENDING")
                .createdAt(Instant.now())
                .build();

        BookingItemResponse response = mapper.toItemResponse(item);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getProviderType()).isEqualTo("HOTEL");
        assertThat(response.getProviderId()).isEqualTo(5L);
        assertThat(response.getItemName()).isEqualTo("Deluxe Room");
        assertThat(response.getQuantity()).isEqualTo(2);
        assertThat(response.getUnitPrice()).isEqualByComparingTo("100.00");
        assertThat(response.getSubtotal()).isEqualByComparingTo("200.00");
        assertThat(response.getStatus()).isEqualTo("PENDING");
    }

    @Test
    void toBookingItem_calculatesSubtotal() {
        BookingItemRequest request = BookingItemRequest.builder()
                .providerType("hotel")
                .providerId(5L)
                .itemName("Suite")
                .quantity(3)
                .unitPrice(new BigDecimal("150.00"))
                .startDate(LocalDate.of(2026, 3, 1))
                .endDate(LocalDate.of(2026, 3, 5))
                .build();

        BookingItem item = mapper.toBookingItem(request);

        assertThat(item.getProviderType()).isEqualTo("HOTEL");
        assertThat(item.getProviderId()).isEqualTo(5L);
        assertThat(item.getItemName()).isEqualTo("Suite");
        assertThat(item.getQuantity()).isEqualTo(3);
        assertThat(item.getSubtotal()).isEqualByComparingTo("450.00");
        assertThat(item.getStatus()).isEqualTo("PENDING");
    }

    @Test
    void toResponseList_mapsAll() {
        Booking b1 = Booking.builder().id(1L).touristId("t1").status("PENDING")
                .totalAmount(BigDecimal.TEN).startDate(LocalDate.now()).endDate(LocalDate.now().plusDays(1))
                .items(List.of()).build();
        Booking b2 = Booking.builder().id(2L).touristId("t2").status("CONFIRMED")
                .totalAmount(BigDecimal.TEN).startDate(LocalDate.now()).endDate(LocalDate.now().plusDays(1))
                .items(List.of()).build();

        List<BookingResponse> responses = mapper.toResponseList(List.of(b1, b2));

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getId()).isEqualTo(1L);
        assertThat(responses.get(1).getId()).isEqualTo(2L);
    }

    @Test
    void toResponse_withItems_mapsItemsToo() {
        BookingItem item = BookingItem.builder()
                .id(1L).providerType("VEHICLE").providerId(3L)
                .itemName("SUV Rental").quantity(1)
                .unitPrice(new BigDecimal("80.00")).subtotal(new BigDecimal("80.00"))
                .status("CONFIRMED").createdAt(Instant.now()).build();

        Booking booking = Booking.builder()
                .id(1L).touristId("t1").status("CONFIRMED")
                .totalAmount(new BigDecimal("80.00"))
                .startDate(LocalDate.now()).endDate(LocalDate.now().plusDays(3))
                .items(List.of(item))
                .build();

        BookingResponse response = mapper.toResponse(booking);

        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getItemName()).isEqualTo("SUV Rental");
        assertThat(response.getItems().get(0).getProviderType()).isEqualTo("VEHICLE");
    }
}
