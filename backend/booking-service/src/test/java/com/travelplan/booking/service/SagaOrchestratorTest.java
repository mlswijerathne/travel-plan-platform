package com.travelplan.booking.service;

import com.travelplan.booking.client.HotelServiceClient;
import com.travelplan.booking.client.TourGuideServiceClient;
import com.travelplan.booking.client.VehicleServiceClient;
import com.travelplan.booking.entity.Booking;
import com.travelplan.booking.entity.BookingItem;
import com.travelplan.booking.entity.SagaOrchestration;
import com.travelplan.booking.repository.SagaOrchestrationRepository;
import com.travelplan.common.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SagaOrchestratorTest {

    @Mock private SagaOrchestrationRepository sagaRepository;
    @Mock private HotelServiceClient hotelServiceClient;
    @Mock private TourGuideServiceClient tourGuideServiceClient;
    @Mock private VehicleServiceClient vehicleServiceClient;

    @InjectMocks
    private SagaOrchestrator sagaOrchestrator;

    private Booking sampleBooking;

    @BeforeEach
    void setUp() {
        sampleBooking = Booking.builder()
                .id(1L).touristId("tourist-123").status("PENDING")
                .totalAmount(new BigDecimal("500.00"))
                .startDate(LocalDate.now().plusDays(5)).endDate(LocalDate.now().plusDays(10))
                .items(new ArrayList<>()).build();
    }

    @Test
    void execute_emptyItems_returnsSuccess() {
        SagaOrchestrator.SagaResult result = sagaOrchestrator.execute(sampleBooking);

        assertThat(result.successful()).isTrue();
        verifyNoInteractions(sagaRepository);
    }

    @Test
    void execute_singleHotel_allAvailable_success() {
        BookingItem hotelItem = buildItem("HOTEL", 10L);
        sampleBooking.getItems().add(hotelItem);

        when(hotelServiceClient.checkAvailability(eq(10L), anyString(), anyString()))
                .thenReturn(ApiResponse.success(new Object()));
        when(hotelServiceClient.getHotelById(10L)).thenReturn(ApiResponse.success(new Object()));
        when(sagaRepository.save(any(SagaOrchestration.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        SagaOrchestrator.SagaResult result = sagaOrchestrator.execute(sampleBooking);

        assertThat(result.successful()).isTrue();
        assertThat(hotelItem.getStatus()).isEqualTo("CONFIRMED");
        verify(sagaRepository, atLeast(2)).save(any(SagaOrchestration.class));
    }

    @Test
    void execute_multiProvider_allSucceed() {
        BookingItem hotelItem = buildItem("HOTEL", 10L);
        BookingItem guideItem = buildItem("TOUR_GUIDE", 20L);
        BookingItem vehicleItem = buildItem("VEHICLE", 30L);
        sampleBooking.getItems().addAll(List.of(hotelItem, guideItem, vehicleItem));

        // All availability checks pass
        when(hotelServiceClient.checkAvailability(eq(10L), anyString(), anyString()))
                .thenReturn(ApiResponse.success(new Object()));
        when(tourGuideServiceClient.checkAvailability(eq(20L), anyString(), anyString()))
                .thenReturn(ApiResponse.success(new Object()));
        when(vehicleServiceClient.checkAvailability(eq(30L), anyString(), anyString()))
                .thenReturn(ApiResponse.success(new Object()));

        // All confirmations pass
        when(hotelServiceClient.getHotelById(10L)).thenReturn(ApiResponse.success(new Object()));
        when(tourGuideServiceClient.getTourGuideById(20L)).thenReturn(ApiResponse.success(new Object()));
        when(vehicleServiceClient.getVehicleById(30L)).thenReturn(ApiResponse.success(new Object()));

        when(sagaRepository.save(any(SagaOrchestration.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        SagaOrchestrator.SagaResult result = sagaOrchestrator.execute(sampleBooking);

        assertThat(result.successful()).isTrue();
        assertThat(hotelItem.getStatus()).isEqualTo("CONFIRMED");
        assertThat(guideItem.getStatus()).isEqualTo("CONFIRMED");
        assertThat(vehicleItem.getStatus()).isEqualTo("CONFIRMED");
    }

    @Test
    void execute_availabilityCheckFails_returnsFailedWithoutSaga() {
        BookingItem hotelItem = buildItem("HOTEL", 10L);
        sampleBooking.getItems().add(hotelItem);

        when(hotelServiceClient.checkAvailability(eq(10L), anyString(), anyString()))
                .thenThrow(new RuntimeException("Connection refused"));

        SagaOrchestrator.SagaResult result = sagaOrchestrator.execute(sampleBooking);

        assertThat(result.successful()).isFalse();
        assertThat(result.rolledBack()).isFalse();
        assertThat(result.message()).contains("Availability check failed");
        verifyNoInteractions(sagaRepository); // No saga created if pre-check fails
    }

    @Test
    void execute_secondStepFails_rollsBackFirstStep() {
        BookingItem hotelItem = buildItem("HOTEL", 10L);
        BookingItem guideItem = buildItem("TOUR_GUIDE", 20L);
        sampleBooking.getItems().addAll(List.of(hotelItem, guideItem));

        // Both availability checks pass
        when(hotelServiceClient.checkAvailability(eq(10L), anyString(), anyString()))
                .thenReturn(ApiResponse.success(new Object()));
        when(tourGuideServiceClient.checkAvailability(eq(20L), anyString(), anyString()))
                .thenReturn(ApiResponse.success(new Object()));

        // Hotel confirmation succeeds
        when(hotelServiceClient.getHotelById(10L)).thenReturn(ApiResponse.success(new Object()));
        // Guide confirmation fails
        when(tourGuideServiceClient.getTourGuideById(20L)).thenThrow(new RuntimeException("Guide unavailable"));

        when(sagaRepository.save(any(SagaOrchestration.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        SagaOrchestrator.SagaResult result = sagaOrchestrator.execute(sampleBooking);

        assertThat(result.successful()).isFalse();
        assertThat(result.rolledBack()).isTrue();
        assertThat(result.message()).contains("TOUR_GUIDE");
        // Hotel item should be rolled back (cancelled)
        assertThat(hotelItem.getStatus()).isEqualTo("CANCELLED");
        assertThat(guideItem.getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    void execute_thirdStepFails_rollsBackFirstTwoSteps() {
        BookingItem hotelItem = buildItem("HOTEL", 10L);
        BookingItem guideItem = buildItem("TOUR_GUIDE", 20L);
        BookingItem vehicleItem = buildItem("VEHICLE", 30L);
        sampleBooking.getItems().addAll(List.of(hotelItem, guideItem, vehicleItem));

        // All availability checks pass
        when(hotelServiceClient.checkAvailability(eq(10L), anyString(), anyString()))
                .thenReturn(ApiResponse.success(new Object()));
        when(tourGuideServiceClient.checkAvailability(eq(20L), anyString(), anyString()))
                .thenReturn(ApiResponse.success(new Object()));
        when(vehicleServiceClient.checkAvailability(eq(30L), anyString(), anyString()))
                .thenReturn(ApiResponse.success(new Object()));

        // Hotel and guide succeed, vehicle fails
        when(hotelServiceClient.getHotelById(10L)).thenReturn(ApiResponse.success(new Object()));
        when(tourGuideServiceClient.getTourGuideById(20L)).thenReturn(ApiResponse.success(new Object()));
        when(vehicleServiceClient.getVehicleById(30L)).thenThrow(new RuntimeException("No vehicles"));

        when(sagaRepository.save(any(SagaOrchestration.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        SagaOrchestrator.SagaResult result = sagaOrchestrator.execute(sampleBooking);

        assertThat(result.successful()).isFalse();
        assertThat(result.rolledBack()).isTrue();
        assertThat(hotelItem.getStatus()).isEqualTo("CANCELLED");
        assertThat(guideItem.getStatus()).isEqualTo("CANCELLED");
        assertThat(vehicleItem.getStatus()).isEqualTo("CANCELLED");
    }

    private BookingItem buildItem(String providerType, Long providerId) {
        return BookingItem.builder()
                .providerType(providerType).providerId(providerId)
                .itemName("Test " + providerType).quantity(1)
                .unitPrice(new BigDecimal("100.00")).subtotal(new BigDecimal("100.00"))
                .startDate(LocalDate.now().plusDays(5)).endDate(LocalDate.now().plusDays(10))
                .status("PENDING").build();
    }
}
