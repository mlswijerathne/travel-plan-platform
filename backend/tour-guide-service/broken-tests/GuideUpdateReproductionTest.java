package com.travelplan.guide.service;

import com.travelplan.guide.domain.Guide;
import com.travelplan.guide.domain.GuideLanguage;
import com.travelplan.guide.dto.GuideRequest;
import com.travelplan.guide.repository.GuideRepository;
import com.travelplan.guide.repository.GuideBookingRequestRepository;
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
public class GuideUpdateReproductionTest {

    @Autowired
    private GuideService guideService;

    @Autowired
    private GuideRepository guideRepository;

    @Test
    public void reproduceUpdateIssue() {
        // 1. Register a guide
        GuideRequest request = new GuideRequest();
        UUID userId = UUID.randomUUID();
        request.setUserId(userId);
        request.setName("Initial Name");
        request.setCity("City");
        request.setCountry("Country");
        request.setHourlyRate(50L);
        request.setProfilePhotoUrl("http://photo.com");

        List<GuideRequest.LanguageDto> languages = new ArrayList<>();
        GuideRequest.LanguageDto lang1 = new GuideRequest.LanguageDto();
        lang1.setLanguage("English");
        lang1.setLevel("Native");
        languages.add(lang1);
        request.setLanguages(languages);

        Guide savedGuide = guideService.registerGuide(request);
        UUID guideId = savedGuide.getId();
        assertNotNull(guideId);

        // 2. Update the guide
        request.setName("Updated Name");

        // Change languages
        List<GuideRequest.LanguageDto> updatedLanguages = new ArrayList<>();
        GuideRequest.LanguageDto lang2 = new GuideRequest.LanguageDto();
        lang2.setLanguage("French");
        lang2.setLevel("Fluent");
        updatedLanguages.add(lang2);
        request.setLanguages(updatedLanguages);

        // This is the call that the user says fails
        assertDoesNotThrow(() -> {
            guideService.updateGuide(guideId, request);
        });

        Guide updatedGuide = guideService.getGuide(guideId);
        assertEquals("Updated Name", updatedGuide.getName());
        assertEquals(1, updatedGuide.getLanguages().size());
        assertEquals("French", updatedGuide.getLanguages().get(0).getLanguage());
    }
}
