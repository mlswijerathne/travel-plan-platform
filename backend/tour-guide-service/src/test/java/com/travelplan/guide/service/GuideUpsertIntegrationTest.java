package com.travelplan.guide.service;

import com.travelplan.guide.domain.Guide;
import com.travelplan.guide.dto.GuideRequest;
import com.travelplan.guide.repository.GuideRepository;
import com.travelplan.guide.Exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class GuideUpsertIntegrationTest {

    @Autowired
    private GuideService guideService;

    @Autowired
    private GuideRepository guideRepository;

    @Test
    public void testUpdateByUserIdFallback() {
        // 1. Register
        UUID userId = UUID.randomUUID();
        GuideRequest request = createRequest(userId, "Original Name");
        Guide saved = guideService.registerGuide(request);
        UUID guideId = saved.getId();

        // 2. Update using UserId as the identifier
        GuideRequest updateRequest = createRequest(userId, "Updated Name");
        Guide updated = guideService.updateGuide(userId, updateRequest);

        assertEquals(guideId, updated.getId());
        assertEquals("Updated Name", updated.getName());

        // Verify lookup by UserId also works
        Guide found = guideService.getGuide(userId);
        assertEquals(guideId, found.getId());
    }

    @Test
    public void testPreventDuplicateRegistration() {
        UUID userId = UUID.randomUUID();
        GuideRequest request = createRequest(userId, "Guide 1");
        guideService.registerGuide(request);

        assertThrows(IllegalStateException.class, () -> {
            guideService.registerGuide(request);
        });
    }

    @Test
    public void testUpdateCollections() {
        UUID userId = UUID.randomUUID();
        GuideRequest request = createRequest(userId, "Name");

        List<GuideRequest.LanguageDto> languages = new ArrayList<>();
        GuideRequest.LanguageDto l1 = new GuideRequest.LanguageDto();
        l1.setLanguage("English");
        l1.setLevel("Native");
        languages.add(l1);
        request.setLanguages(languages);

        Guide saved = guideService.registerGuide(request);
        assertEquals(1, saved.getLanguages().size());

        // Update: replace English with Spanish
        List<GuideRequest.LanguageDto> updatedLanguages = new ArrayList<>();
        GuideRequest.LanguageDto l2 = new GuideRequest.LanguageDto();
        l2.setLanguage("Spanish");
        l2.setLevel("Fluent");
        updatedLanguages.add(l2);
        request.setLanguages(updatedLanguages);

        Guide updated = guideService.updateGuide(saved.getId(), request);
        assertEquals(1, updated.getLanguages().size());
        assertEquals("Spanish", updated.getLanguages().get(0).getLanguage());
    }

    private GuideRequest createRequest(UUID userId, String name) {
        GuideRequest request = new GuideRequest();
        request.setUserId(userId);
        request.setName(name);
        request.setCity("Test City");
        request.setCountry("Test Country");
        request.setHourlyRate(100L);
        request.setProfilePhotoUrl("http://photo.url");
        return request;
    }
}
