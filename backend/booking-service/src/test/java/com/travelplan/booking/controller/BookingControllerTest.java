package com.travelplan.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.travelplan.booking.dto.*;
import com.travelplan.booking.service.BookingService;
import com.travelplan.common.dto.PaginatedResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private BookingController bookingController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Authentication authentication;

    private static final String TOURIST_ID = "tourist-123";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bookingController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        authentication = new UsernamePasswordAuthenticationToken(
                TOURIST_ID, null, List.of(new SimpleGrantedAuthority("ROLE_TOURIST")));
    }

    @Test
    void createBooking_returns201() throws Exception {
        CreateBookingRequest request = CreateBookingRequest.builder()
                .startDate(LocalDate.now().plusDays(5))
                .endDate(LocalDate.now().plusDays(10))
                .items(List.of(BookingItemRequest.builder()
                        .providerType("HOTEL")
                        .providerId(10L)
                        .itemName("Room")
                        .quantity(1)
                        .unitPrice(new BigDecimal("150.00"))
                        .build()))
                .build();

        BookingResponse response = BookingResponse.builder()
                .id(1L)
                .touristId(TOURIST_ID)
                .bookingReference("TRP-20260209-ABC123")
                .status("PENDING")
                .totalAmount(new BigDecimal("150.00"))
                .startDate(LocalDate.now().plusDays(5))
                .endDate(LocalDate.now().plusDays(10))
                .items(List.of(BookingItemResponse.builder()
                        .id(1L).providerType("HOTEL").providerId(10L)
                        .itemName("Room").quantity(1)
                        .unitPrice(new BigDecimal("150.00"))
                        .subtotal(new BigDecimal("150.00"))
                        .status("PENDING").build()))
                .build();

        when(bookingService.createBooking(eq(TOURIST_ID), any(CreateBookingRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(authentication))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.bookingReference").value("TRP-20260209-ABC123"));
    }

    @Test
    void getBookingById_returns200() throws Exception {
        BookingResponse response = BookingResponse.builder()
                .id(1L).touristId(TOURIST_ID).status("PENDING").build();

        when(bookingService.getBookingById(1L, TOURIST_ID)).thenReturn(response);

        mockMvc.perform(get("/api/bookings/1")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void getTouristBookings_returnsPaginatedResponse() throws Exception {
        PaginatedResponse<BookingResponse> paginated = PaginatedResponse.of(
                List.of(BookingResponse.builder().id(1L).status("PENDING").build()),
                0, 20, 1);

        when(bookingService.getTouristBookings(eq(TOURIST_ID), isNull(), eq(0), eq(20)))
                .thenReturn(paginated);

        mockMvc.perform(get("/api/bookings")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.pagination.totalItems").value(1));
    }

    @Test
    void cancelBooking_returns200() throws Exception {
        CancelBookingRequest cancelRequest = CancelBookingRequest.builder()
                .reason("Change of plans").build();

        BookingResponse response = BookingResponse.builder()
                .id(1L).status("CANCELLED").cancellationReason("Change of plans").build();

        when(bookingService.cancelBooking(eq(1L), eq(TOURIST_ID), any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/bookings/1/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelRequest))
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    void checkAvailability_returns200() throws Exception {
        AvailabilityCheckRequest request = AvailabilityCheckRequest.builder()
                .items(List.of(AvailabilityItemRequest.builder()
                        .providerType("HOTEL").providerId(10L)
                        .startDate(LocalDate.now().plusDays(5))
                        .endDate(LocalDate.now().plusDays(10))
                        .quantity(1).build()))
                .build();

        AvailabilityCheckResponse response = AvailabilityCheckResponse.builder()
                .available(true)
                .items(List.of(AvailabilityItemResponse.builder()
                        .providerType("HOTEL").providerId(10L)
                        .available(true).message("Available").build()))
                .build();

        when(bookingService.checkAvailability(any())).thenReturn(response);

        mockMvc.perform(post("/api/bookings/availability-check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.available").value(true));
    }

    @Test
    void updateBookingItemStatus_returns200() throws Exception {
        UpdateBookingItemStatusRequest request = UpdateBookingItemStatusRequest.builder()
                .status("CONFIRMED").build();

        BookingResponse response = BookingResponse.builder()
                .id(1L).status("CONFIRMED").build();

        when(bookingService.updateBookingItemStatus(eq(1L), eq(1L), any()))
                .thenReturn(response);

        mockMvc.perform(put("/api/bookings/1/items/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
    }

    @Test
    void getProviderBookings_returns200() throws Exception {
        PaginatedResponse<BookingResponse> paginated = PaginatedResponse.of(
                List.of(BookingResponse.builder().id(1L).status("PENDING").build()),
                0, 20, 1);

        when(bookingService.getProviderBookings(eq("HOTEL"), eq(10L), isNull(), eq(0), eq(20)))
                .thenReturn(paginated);

        mockMvc.perform(get("/api/bookings/provider/HOTEL/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    void getBookingByReference_returns200() throws Exception {
        BookingResponse response = BookingResponse.builder()
                .id(1L).bookingReference("TRP-20260209-ABC123").build();

        when(bookingService.getBookingByReference("TRP-20260209-ABC123")).thenReturn(response);

        mockMvc.perform(get("/api/bookings/reference/TRP-20260209-ABC123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bookingReference").value("TRP-20260209-ABC123"));
    }
}
