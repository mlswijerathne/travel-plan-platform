package com.travelplan.booking.service.impl;

import com.travelplan.booking.client.HotelServiceClient;
import com.travelplan.booking.client.TourGuideServiceClient;
import com.travelplan.booking.client.VehicleServiceClient;
import com.travelplan.booking.dto.*;
import com.travelplan.booking.entity.Booking;
import com.travelplan.booking.entity.BookingItem;
import com.travelplan.booking.mapper.BookingMapper;
import com.travelplan.booking.repository.BookingItemRepository;
import com.travelplan.booking.repository.BookingRepository;
import com.travelplan.booking.service.EventPublisher;
import com.travelplan.booking.service.RefundPolicyService;
import com.travelplan.booking.service.SagaOrchestrator;
import com.travelplan.common.dto.ApiResponse;
import com.travelplan.common.exception.ForbiddenException;
import com.travelplan.common.exception.ResourceNotFoundException;
import com.travelplan.common.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private BookingItemRepository bookingItemRepository;
    @Mock private BookingMapper bookingMapper;
    @Mock private HotelServiceClient hotelServiceClient;
    @Mock private TourGuideServiceClient tourGuideServiceClient;
    @Mock private VehicleServiceClient vehicleServiceClient;
    @Mock private SagaOrchestrator sagaOrchestrator;
    @Mock private EventPublisher eventPublisher;
    @Mock private RefundPolicyService refundPolicyService;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private static final String TOURIST_ID = "tourist-123";
    private Booking sampleBooking;
    private BookingItem sampleItem;
    private BookingResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleItem = BookingItem.builder()
                .id(1L).providerType("HOTEL").providerId(10L)
                .itemName("Ocean View Room").quantity(1)
                .unitPrice(new BigDecimal("150.00")).subtotal(new BigDecimal("150.00"))
                .startDate(LocalDate.now().plusDays(5)).endDate(LocalDate.now().plusDays(10))
                .status("PENDING").createdAt(Instant.now()).build();

        sampleBooking = Booking.builder()
                .id(1L).touristId(TOURIST_ID).bookingReference("TRP-20260209-ABC123")
                .status("PENDING").totalAmount(new BigDecimal("150.00"))
                .startDate(LocalDate.now().plusDays(5)).endDate(LocalDate.now().plusDays(10))
                .createdAt(Instant.now()).updatedAt(Instant.now())
                .items(new ArrayList<>(List.of(sampleItem))).build();
        sampleItem.setBooking(sampleBooking);

        sampleResponse = BookingResponse.builder()
                .id(1L).touristId(TOURIST_ID).bookingReference("TRP-20260209-ABC123")
                .status("CONFIRMED").totalAmount(new BigDecimal("150.00"))
                .startDate(LocalDate.now().plusDays(5)).endDate(LocalDate.now().plusDays(10))
                .items(List.of(BookingItemResponse.builder()
                        .id(1L).providerType("HOTEL").providerId(10L)
                        .itemName("Ocean View Room").quantity(1)
                        .unitPrice(new BigDecimal("150.00")).subtotal(new BigDecimal("150.00"))
                        .status("CONFIRMED").build()))
                .build();
    }

    // --- createBooking tests ---

    @Test
    void createBooking_sagaSucceeds_returnsConfirmed() {
        BookingItemRequest itemRequest = BookingItemRequest.builder()
                .providerType("HOTEL").providerId(10L).itemName("Room")
                .quantity(1).unitPrice(new BigDecimal("150.00")).build();

        CreateBookingRequest request = CreateBookingRequest.builder()
                .startDate(LocalDate.now().plusDays(5)).endDate(LocalDate.now().plusDays(10))
                .items(List.of(itemRequest)).build();

        when(bookingMapper.toBookingItem(any())).thenReturn(sampleItem);
        when(bookingRepository.save(any(Booking.class))).thenReturn(sampleBooking);
        when(sagaOrchestrator.execute(any(Booking.class))).thenReturn(SagaOrchestrator.SagaResult.success());
        when(bookingMapper.toResponse(any(Booking.class))).thenReturn(sampleResponse);

        BookingResponse result = bookingService.createBooking(TOURIST_ID, request);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("CONFIRMED");
        verify(sagaOrchestrator).execute(any(Booking.class));
        verify(eventPublisher).publishBookingCreated(any());
        verify(eventPublisher).publishBookingConfirmed(any());
    }

    @Test
    void createBooking_sagaFails_throwsAndPublishesCancelled() {
        BookingItemRequest itemRequest = BookingItemRequest.builder()
                .providerType("HOTEL").providerId(10L).itemName("Room")
                .quantity(1).unitPrice(new BigDecimal("150.00")).build();

        CreateBookingRequest request = CreateBookingRequest.builder()
                .startDate(LocalDate.now().plusDays(5)).endDate(LocalDate.now().plusDays(10))
                .items(List.of(itemRequest)).build();

        when(bookingMapper.toBookingItem(any())).thenReturn(sampleItem);
        when(bookingRepository.save(any(Booking.class))).thenReturn(sampleBooking);
        when(sagaOrchestrator.execute(any(Booking.class)))
                .thenReturn(SagaOrchestrator.SagaResult.failed("Hotel unavailable"));
        when(bookingMapper.toResponse(any(Booking.class))).thenReturn(sampleResponse);

        assertThatThrownBy(() -> bookingService.createBooking(TOURIST_ID, request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Hotel unavailable");

        verify(eventPublisher).publishBookingCancelled(any());
    }

    @Test
    void createBooking_sagaRolledBack_throwsAndPublishesCancelled() {
        BookingItemRequest itemRequest = BookingItemRequest.builder()
                .providerType("HOTEL").providerId(10L).itemName("Room")
                .quantity(1).unitPrice(new BigDecimal("150.00")).build();

        CreateBookingRequest request = CreateBookingRequest.builder()
                .startDate(LocalDate.now().plusDays(5)).endDate(LocalDate.now().plusDays(10))
                .items(List.of(itemRequest)).build();

        when(bookingMapper.toBookingItem(any())).thenReturn(sampleItem);
        when(bookingRepository.save(any(Booking.class))).thenReturn(sampleBooking);
        when(sagaOrchestrator.execute(any(Booking.class)))
                .thenReturn(SagaOrchestrator.SagaResult.rolledBack("Vehicle failed after hotel confirmed"));
        when(bookingMapper.toResponse(any(Booking.class))).thenReturn(sampleResponse);

        assertThatThrownBy(() -> bookingService.createBooking(TOURIST_ID, request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Vehicle failed");

        verify(eventPublisher).publishBookingCancelled(any());
    }

    @Test
    void createBooking_invalidDates_throwsValidation() {
        CreateBookingRequest request = CreateBookingRequest.builder()
                .startDate(LocalDate.now().plusDays(10)).endDate(LocalDate.now().plusDays(5))
                .items(List.of(BookingItemRequest.builder().providerType("HOTEL").providerId(10L)
                        .itemName("Room").unitPrice(new BigDecimal("100.00")).build()))
                .build();

        assertThatThrownBy(() -> bookingService.createBooking(TOURIST_ID, request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("End date must be after start date");
    }

    @Test
    void createBooking_invalidProviderType_throwsValidation() {
        CreateBookingRequest request = CreateBookingRequest.builder()
                .startDate(LocalDate.now().plusDays(5)).endDate(LocalDate.now().plusDays(10))
                .items(List.of(BookingItemRequest.builder().providerType("INVALID").providerId(10L)
                        .itemName("Thing").unitPrice(new BigDecimal("100.00")).build()))
                .build();

        assertThatThrownBy(() -> bookingService.createBooking(TOURIST_ID, request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid provider type");
    }

    // --- getBookingById tests ---

    @Test
    void getBookingById_success() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(sampleBooking));
        when(bookingMapper.toResponse(sampleBooking)).thenReturn(sampleResponse);

        BookingResponse result = bookingService.getBookingById(1L, TOURIST_ID);
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getBookingById_notFound() {
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> bookingService.getBookingById(999L, TOURIST_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getBookingById_differentTourist_throwsForbidden() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(sampleBooking));
        assertThatThrownBy(() -> bookingService.getBookingById(1L, "other-tourist"))
                .isInstanceOf(ForbiddenException.class);
    }

    // --- getTouristBookings tests ---

    @Test
    void getTouristBookings_withStatus() {
        Page<Booking> page = new PageImpl<>(List.of(sampleBooking));
        when(bookingRepository.findByTouristIdAndStatus(eq(TOURIST_ID), eq("PENDING"), any(PageRequest.class)))
                .thenReturn(page);
        when(bookingMapper.toResponse(any(Booking.class))).thenReturn(sampleResponse);

        var result = bookingService.getTouristBookings(TOURIST_ID, "PENDING", 0, 20);
        assertThat(result.getData()).hasSize(1);
    }

    @Test
    void getTouristBookings_noStatus() {
        Page<Booking> page = new PageImpl<>(List.of(sampleBooking));
        when(bookingRepository.findByTouristId(eq(TOURIST_ID), any(PageRequest.class))).thenReturn(page);
        when(bookingMapper.toResponse(any(Booking.class))).thenReturn(sampleResponse);

        var result = bookingService.getTouristBookings(TOURIST_ID, null, 0, 20);
        assertThat(result.getData()).hasSize(1);
    }

    // --- cancelBooking tests ---

    @Test
    void cancelBooking_calculatesRefund_publishesEvents() {
        CancelBookingRequest cancelRequest = CancelBookingRequest.builder().reason("Change of plans").build();

        RefundPolicyService.RefundResult refundResult = RefundPolicyService.RefundResult.builder()
                .refundAmount(new BigDecimal("150.00")).refundPolicy("FULL_REFUND")
                .refundPercentage(100).message("Full refund").build();

        BookingResponse cancelledResponse = BookingResponse.builder()
                .id(1L).status("CANCELLED").cancellationReason("Change of plans")
                .refundAmount(new BigDecimal("150.00")).refundPolicy("FULL_REFUND").build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(sampleBooking));
        when(refundPolicyService.calculateRefund(any(), any())).thenReturn(refundResult);
        when(bookingRepository.save(any(Booking.class))).thenReturn(sampleBooking);
        when(bookingMapper.toResponse(any(Booking.class))).thenReturn(cancelledResponse);

        BookingResponse result = bookingService.cancelBooking(1L, TOURIST_ID, cancelRequest);

        assertThat(result.getStatus()).isEqualTo("CANCELLED");
        assertThat(result.getRefundAmount()).isEqualByComparingTo("150.00");
        assertThat(result.getRefundPolicy()).isEqualTo("FULL_REFUND");
        verify(refundPolicyService).calculateRefund(any(), any());
        verify(eventPublisher).publishBookingCancelled(any());
        verify(eventPublisher).publishRefundProcessed(any());
    }

    @Test
    void cancelBooking_noRefund_doesNotPublishRefundEvent() {
        RefundPolicyService.RefundResult refundResult = RefundPolicyService.RefundResult.builder()
                .refundAmount(BigDecimal.ZERO).refundPolicy("NO_REFUND")
                .refundPercentage(0).message("No refund").build();

        BookingResponse cancelledResponse = BookingResponse.builder()
                .id(1L).status("CANCELLED").refundAmount(BigDecimal.ZERO).refundPolicy("NO_REFUND").build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(sampleBooking));
        when(refundPolicyService.calculateRefund(any(), any())).thenReturn(refundResult);
        when(bookingRepository.save(any(Booking.class))).thenReturn(sampleBooking);
        when(bookingMapper.toResponse(any(Booking.class))).thenReturn(cancelledResponse);

        bookingService.cancelBooking(1L, TOURIST_ID, null);

        verify(eventPublisher).publishBookingCancelled(any());
        verify(eventPublisher, never()).publishRefundProcessed(any());
    }

    @Test
    void cancelBooking_alreadyCancelled_throwsValidation() {
        sampleBooking.setStatus("CANCELLED");
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(sampleBooking));

        assertThatThrownBy(() -> bookingService.cancelBooking(1L, TOURIST_ID, null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("already cancelled");
    }

    @Test
    void cancelBooking_completed_throwsValidation() {
        sampleBooking.setStatus("COMPLETED");
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(sampleBooking));

        assertThatThrownBy(() -> bookingService.cancelBooking(1L, TOURIST_ID, null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("completed booking");
    }

    @Test
    void cancelBooking_differentTourist_throwsForbidden() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(sampleBooking));
        assertThatThrownBy(() -> bookingService.cancelBooking(1L, "other-tourist", null))
                .isInstanceOf(ForbiddenException.class);
    }

    // --- checkAvailability tests ---

    @Test
    void checkAvailability_allAvailable() {
        AvailabilityCheckRequest request = AvailabilityCheckRequest.builder()
                .items(List.of(AvailabilityItemRequest.builder()
                        .providerType("HOTEL").providerId(10L)
                        .startDate(LocalDate.now().plusDays(5)).endDate(LocalDate.now().plusDays(10))
                        .quantity(1).build()))
                .build();

        when(hotelServiceClient.checkAvailability(eq(10L), anyString(), anyString()))
                .thenReturn(ApiResponse.success(new Object()));

        AvailabilityCheckResponse result = bookingService.checkAvailability(request);
        assertThat(result.isAvailable()).isTrue();
        assertThat(result.getItems().get(0).isAvailable()).isTrue();
    }

    @Test
    void checkAvailability_providerUnavailable() {
        AvailabilityCheckRequest request = AvailabilityCheckRequest.builder()
                .items(List.of(AvailabilityItemRequest.builder()
                        .providerType("HOTEL").providerId(10L)
                        .startDate(LocalDate.now().plusDays(5)).endDate(LocalDate.now().plusDays(10))
                        .quantity(1).build()))
                .build();

        when(hotelServiceClient.checkAvailability(eq(10L), anyString(), anyString()))
                .thenThrow(new RuntimeException("Connection refused"));

        AvailabilityCheckResponse result = bookingService.checkAvailability(request);
        assertThat(result.isAvailable()).isFalse();
    }

    // --- updateBookingItemStatus tests ---

    @Test
    void updateBookingItemStatus_confirmItem_publishesEvent() {
        sampleBooking.setStatus("PENDING");
        UpdateBookingItemStatusRequest request = UpdateBookingItemStatusRequest.builder().status("CONFIRMED").build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(sampleBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(sampleBooking);
        when(bookingMapper.toResponse(any(Booking.class))).thenReturn(sampleResponse);

        bookingService.updateBookingItemStatus(1L, 1L, request);

        verify(eventPublisher).publishBookingConfirmed(any());
    }

    @Test
    void updateBookingItemStatus_invalidTransition() {
        sampleItem.setStatus("CANCELLED");
        UpdateBookingItemStatusRequest request = UpdateBookingItemStatusRequest.builder().status("CONFIRMED").build();
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(sampleBooking));

        assertThatThrownBy(() -> bookingService.updateBookingItemStatus(1L, 1L, request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid status transition");
    }

    @Test
    void updateBookingItemStatus_itemNotFound() {
        UpdateBookingItemStatusRequest request = UpdateBookingItemStatusRequest.builder().status("CONFIRMED").build();
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(sampleBooking));

        assertThatThrownBy(() -> bookingService.updateBookingItemStatus(1L, 999L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- getBookingByReference tests ---

    @Test
    void getBookingByReference_success() {
        when(bookingRepository.findByBookingReference("TRP-20260209-ABC123"))
                .thenReturn(Optional.of(sampleBooking));
        when(bookingMapper.toResponse(sampleBooking)).thenReturn(sampleResponse);

        BookingResponse result = bookingService.getBookingByReference("TRP-20260209-ABC123");
        assertThat(result.getBookingReference()).isEqualTo("TRP-20260209-ABC123");
    }

    @Test
    void getBookingByReference_notFound() {
        when(bookingRepository.findByBookingReference("INVALID")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> bookingService.getBookingByReference("INVALID"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- getProviderBookings tests ---

    @Test
    void getProviderBookings_returnsResults() {
        sampleItem.setBooking(sampleBooking);
        when(bookingItemRepository.findByProviderTypeAndProviderId("HOTEL", 10L))
                .thenReturn(List.of(sampleItem));
        when(bookingRepository.findAllById(anyList())).thenReturn(List.of(sampleBooking));
        when(bookingMapper.toResponse(any(Booking.class))).thenReturn(sampleResponse);

        var result = bookingService.getProviderBookings("HOTEL", 10L, null, 0, 20);
        assertThat(result.getData()).hasSize(1);
    }
}
