package com.travelplan.guide.controller;

import com.travelplan.guide.domain.Guide;
import com.travelplan.guide.domain.GuideBookingRequest;
import com.travelplan.guide.domain.BookingStatus;
import com.travelplan.guide.dto.*;
import com.travelplan.guide.service.GuideService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import jakarta.transaction.Transactional;

import java.util.Collections;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class GuideCompleteApiIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private GuideService guideService;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        public void testComprehensiveFlow() throws Exception {
                UUID userId = UUID.randomUUID();
                GuideRequest request = new GuideRequest();
                request.setUserId(userId);
                request.setName("John Doe");
                request.setCity("London");
                request.setCountry("UK");
                request.setHourlyRate(50L);
                request.setProfilePhotoUrl("http://example.com/photo.jpg");

                GuideRequest.SkillDto skillDto = new GuideRequest.SkillDto();
                skillDto.setSkillName("History");
                request.setSkills(Collections.singletonList(skillDto));

                GuideRequest.LanguageDto langDto = new GuideRequest.LanguageDto();
                langDto.setLanguage("English");
                langDto.setLevel("Native");
                request.setLanguages(Collections.singletonList(langDto));

                GuideRequest.SpecializationDto specDto = new GuideRequest.SpecializationDto();
                specDto.setName("Historical Walks");
                request.setSpecializations(Collections.singletonList(specDto));

                // 1. FR17: Register
                mockMvc.perform(post("/api/guides/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.name").value("John Doe"));

                Guide guide = guideService.getGuide(userId);
                UUID guideId = guide.getId();

                // 2. FR18: Skills & Languages
                GuideRequest.SkillDto skill = new GuideRequest.SkillDto();
                skill.setSkillName("History");
                mockMvc.perform(post("/api/guides/" + guideId + "/skills")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(skill)))
                                .andExpect(status().isOk());

                mockMvc.perform(get("/api/guides/" + guideId + "/skills"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].skillName").value("History"));

                // 3. FR19: Availability
                AvailabilityRequest availReq = new AvailabilityRequest();
                availReq.setAvailableDate(java.time.LocalDate.now());
                availReq.setStartTime(java.time.LocalTime.of(10, 0));
                availReq.setEndTime(java.time.LocalTime.of(12, 0));
                availReq.setAvailable(true);

                mockMvc.perform(post("/api/guides/" + guideId + "/availability")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(availReq)))
                                .andExpect(status().isOk());

                // 4. FR20: Ratings
                mockMvc.perform(get("/api/guides/" + guideId + "/ratings"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.rating").value(0.0));

                // 5. FR21: Bookings (Manual check: handleBookingAction fallback/alias)
                mockMvc.perform(get("/api/guides/" + guideId + "/booking-requests"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray());

                // 6. FR22: Search
                mockMvc.perform(get("/api/guides/search?location=London"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].name").value("John Doe"));
        }
}
