package com.travelplan.guide.service;

import com.travelplan.guide.dto.GuideRequest;
import com.travelplan.guide.dto.GuideResponse;
import com.travelplan.guide.Exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.travelplan.guide.domain.Guide;
import com.travelplan.guide.repository.GuideRepository;
import com.travelplan.guide.repository.AvailabilityRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GuideUpsertIntegrationTest {

    @Mock
    private GuideRepository guideRepository;

    @Mock
    private AvailabilityRepository availabilityRepository;

    @InjectMocks
    private GuideServiceImpl guideService;

    @Test
    public void testRegisterGuide_shouldReturnResponse() {
        GuideRequest request = createRequest();

        Guide savedGuide = createGuideEntity(1L, "user-123");
        when(guideRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(guideRepository.save(any(Guide.class))).thenReturn(savedGuide);

        GuideResponse response = guideService.registerGuide("user-123", request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("user-123", response.getUserId());
        assertEquals("John", response.getFirstName());
        verify(guideRepository).save(any(Guide.class));
    }

    @Test
    public void testRegisterGuide_shouldThrowWhenDuplicate() {
        GuideRequest request = createRequest();
        Guide existing = createGuideEntity(1L, "user-existing");

        when(guideRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existing));

        assertThrows(IllegalStateException.class, () -> {
            guideService.registerGuide("user-456", request);
        });
    }

    @Test
    public void testUpdateGuide_shouldUpdateAndReturn() {
        GuideRequest request = createRequest();
        request.setFirstName("Updated");

        Guide existingGuide = createGuideEntity(1L, "user-123");
        Guide updatedGuide = createGuideEntity(1L, "user-123");
        updatedGuide.setFirstName("Updated");

        when(guideRepository.findById(1L)).thenReturn(Optional.of(existingGuide));
        when(guideRepository.save(any(Guide.class))).thenReturn(updatedGuide);

        GuideResponse response = guideService.updateGuide(1L, request);

        assertEquals("Updated", response.getFirstName());
        verify(guideRepository).save(any(Guide.class));
    }

    @Test
    public void testUpdateGuide_shouldThrowWhenNotFound() {
        GuideRequest request = createRequest();
        when(guideRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            guideService.updateGuide(999L, request);
        });
    }

    @Test
    public void testGetGuide_shouldReturnResponse() {
        Guide guide = createGuideEntity(1L, "user-123");
        when(guideRepository.findById(1L)).thenReturn(Optional.of(guide));

        GuideResponse response = guideService.getGuide(1L);

        assertEquals(1L, response.getId());
        assertEquals("John", response.getFirstName());
    }

    @Test
    public void testGetGuide_shouldThrowWhenNotFound() {
        when(guideRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            guideService.getGuide(999L);
        });
    }

    private GuideRequest createRequest() {
        GuideRequest request = new GuideRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("test@example.com");
        request.setPhoneNumber("+94771234567");
        request.setBio("Experienced guide");
        request.setLanguages(List.of("English", "Sinhala"));
        request.setSpecializations(List.of("Wildlife", "Culture"));
        request.setExperienceYears(5);
        request.setHourlyRate(new BigDecimal("25.00"));
        request.setDailyRate(new BigDecimal("150.00"));
        request.setProfileImageUrl("http://photo.url");
        return request;
    }

    private Guide createGuideEntity(Long id, String userId) {
        Guide guide = new Guide();
        guide.setId(id);
        guide.setUserId(userId);
        guide.setFirstName("John");
        guide.setLastName("Doe");
        guide.setEmail("test@example.com");
        guide.setPhoneNumber("+94771234567");
        guide.setBio("Experienced guide");
        guide.setLanguages(List.of("English", "Sinhala"));
        guide.setSpecializations(List.of("Wildlife", "Culture"));
        guide.setExperienceYears(5);
        guide.setHourlyRate(new BigDecimal("25.00"));
        guide.setDailyRate(new BigDecimal("150.00"));
        guide.setProfileImageUrl("http://photo.url");
        guide.setVerified(false);
        guide.setActive(true);
        guide.setAverageRating(BigDecimal.ZERO);
        guide.setReviewCount(0);
        return guide;
    }
}
