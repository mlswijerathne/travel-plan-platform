package com.travelplan.guide.service;

import com.travelplan.guide.domain.*;
import com.travelplan.guide.dto.BookingActionRequest;
import com.travelplan.guide.dto.GuideRequest;
import com.travelplan.guide.repository.GuideBookingRequestRepository;
import com.travelplan.guide.repository.GuideRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class BookingValidationIntegrationTest {

    @Autowired
    private GuideService guideService;

    @Autowired
    private GuideRepository guideRepository;

    @Autowired
    private GuideBookingRequestRepository bookingRepository;

    private Guide guide;

    @BeforeEach
    void setUp() {
        GuideRequest request = new GuideRequest();
        request.setUserId(UUID.randomUUID());
        request.setName("John Guide");
        request.setCity("Paris");
        request.setCountry("France");
        request.setHourlyRate(50L);
        // Available on Monday 09:00-17:00
        request.setWeeklyScheduleJson("{\"schedule\": {\"MONDAY\": [{\"start\": \"09:00\", \"end\": \"17:00\"}]}}");
        guide = guideService.registerGuide(request);
    }

    @Test
    void testValidTransition_PendingToAccepted() {
        GuideBookingRequest booking = createBooking(LocalDateTime.of(2024, 1, 1, 10, 0)); // Monday 10:00

        GuideBookingRequest updated = guideService.handleBookingAction(booking.getId(),
                new BookingActionRequest(BookingStatus.ACCEPTED));

        assertEquals(BookingStatus.ACCEPTED, updated.getStatus());
    }

    @Test
    void testInvalidTransition_AcceptedToDeclined() {
        GuideBookingRequest booking = createBooking(LocalDateTime.of(2024, 1, 1, 10, 0));
        guideService.handleBookingAction(booking.getId(), new BookingActionRequest(BookingStatus.ACCEPTED));

        assertThrows(IllegalStateException.class, () -> {
            guideService.handleBookingAction(booking.getId(), new BookingActionRequest(BookingStatus.DECLINED));
        });
    }

    @Test
    void testInvalidTransition_PendingToCancelled() {
        GuideBookingRequest booking = createBooking(LocalDateTime.of(2024, 1, 1, 10, 0));

        assertThrows(IllegalStateException.class, () -> {
            guideService.handleBookingAction(booking.getId(), new BookingActionRequest(BookingStatus.CANCELLED));
        });
    }

    @Test
    void testAvailabilityValidation_GuideUnavailable() {
        // Monday 18:00 (Outside schedule)
        GuideBookingRequest booking = createBooking(LocalDateTime.of(2024, 1, 1, 18, 0));

        assertThrows(IllegalStateException.class, () -> {
            guideService.handleBookingAction(booking.getId(), new BookingActionRequest(BookingStatus.ACCEPTED));
        });
    }

    private GuideBookingRequest createBooking(LocalDateTime startTime) {
        GuideBookingRequest booking = new GuideBookingRequest();
        booking.setGuide(guide);
        booking.setTouristId(UUID.randomUUID());
        booking.setStartTime(startTime);
        booking.setEndTime(startTime.plusHours(2));
        booking.setStatus(BookingStatus.PENDING);
        return bookingRepository.save(booking);
    }
}
