package com.travelplan.guide.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplan.guide.Exception.ResourceNotFoundException;
import com.travelplan.guide.domain.Guide;
import com.travelplan.guide.dto.GuideRequest;
import com.travelplan.guide.service.GuideService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class GuideControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GuideService guideService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void registerGuide_ShouldReturn201() throws Exception {
        GuideRequest request = new GuideRequest();
        request.setUserId(UUID.randomUUID());
        request.setName("John Doe");
        request.setCity("Colombo");
        request.setCountry("Sri Lanka");
        request.setHourlyRate(50L);
        request.setProfilePhotoUrl("http://example.com/photo.jpg");

        Guide guide = new Guide();
        guide.setId(UUID.randomUUID());
        guide.setName("John Doe");

        when(guideService.registerGuide(any(GuideRequest.class))).thenReturn(guide);

        mockMvc.perform(post("/api/guides")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    public void getGuide_NotFound_ShouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        when(guideService.getGuide(id)).thenThrow(new ResourceNotFoundException("Guide not found"));

        mockMvc.perform(get("/api/guides/" + id))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Guide not found"));
    }

    @Test
    public void registerGuide_InvalidRequest_ShouldReturn400() throws Exception {
        GuideRequest request = new GuideRequest(); // Missing name and userId

        mockMvc.perform(post("/api/guides")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.userId").exists());
    }
}
