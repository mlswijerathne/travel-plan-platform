package com.travelplan.guide.service;

import com.travelplan.guide.domain.Guide;
import com.travelplan.guide.dto.GuideRequest;
import com.travelplan.guide.repository.GuideBookingRequestRepository;
import com.travelplan.guide.repository.GuideRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GuideServiceTest {

    @Mock
    private GuideRepository guideRepository;

    @Mock
    private GuideBookingRequestRepository bookingRepository;

    private GuideServiceImpl guideService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        guideService = new GuideServiceImpl(guideRepository, bookingRepository);
    }

    @Test
    void registerGuide_ShouldSaveGuide() {
        GuideRequest request = new GuideRequest();
        request.setName("John Doe");
        request.setCity("Paris");

        Guide savedGuide = new Guide();
        savedGuide.setId(UUID.randomUUID());
        savedGuide.setName("John Doe");

        when(guideRepository.save(any(Guide.class))).thenReturn(savedGuide);

        Guide result = guideService.registerGuide(request);

        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        verify(guideRepository).save(any(Guide.class));
    }

    @Test
    void searchGuides_ShouldFilterByAvailability() {
        // Mock Guide with Schedule (Monday 09:00-17:00)
        Guide guide = new Guide();
        guide.setId(UUID.randomUUID());
        guide.setName("Available Guide");
        // JSON for {"schedule": {"MONDAY": [{"start": "09:00", "end": "17:00"}]}}
        String jsonSchedule = "{\"schedule\": {\"MONDAY\": [{\"start\": \"09:00\", \"end\": \"17:00\"}]}}";
        guide.setWeeklyScheduleJson(jsonSchedule);

        when(guideRepository.findAll(any(Specification.class))).thenReturn(List.of(guide));

        // Test Date: Next Monday at 10:00 (Available)
        // Need to pick a specific Monday date. e.g. 2024-01-01 was Monday.
        LocalDateTime queryDate = LocalDateTime.of(2024, 1, 1, 10, 0);

        Page<Guide> result = guideService.searchGuides(null, queryDate, null, null, null,
                org.springframework.data.domain.PageRequest.of(0, 20));

        assertEquals(1, result.getContent().size());
        assertEquals("Available Guide", result.getContent().get(0).getName());
    }

    @Test
    void searchGuides_ShouldExcludeUnavailable() {
        // Mock Guide with Schedule (Monday 09:00-17:00)
        Guide guide = new Guide();
        guide.setName("Busy Guide");
        String jsonSchedule = "{\"schedule\": {\"MONDAY\": [{\"start\": \"09:00\", \"end\": \"17:00\"}]}}";
        guide.setWeeklyScheduleJson(jsonSchedule);

        when(guideRepository.findAll(any(Specification.class))).thenReturn(List.of(guide));

        // Test Date: Next Monday at 18:00 (Unavailable)
        LocalDateTime queryDate = LocalDateTime.of(2024, 1, 1, 18, 0);

        Page<Guide> result = guideService.searchGuides(null, queryDate, null, null, null,
                org.springframework.data.domain.PageRequest.of(0, 20));

        assertTrue(result.getContent().isEmpty());
    }
}
