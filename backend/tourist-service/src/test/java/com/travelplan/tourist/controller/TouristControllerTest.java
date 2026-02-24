package com.travelplan.tourist.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplan.common.exception.GlobalExceptionHandler;
import com.travelplan.common.exception.ResourceNotFoundException;
import com.travelplan.tourist.dto.*;
import com.travelplan.tourist.service.TouristService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TouristControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private TouristService touristService;

    @InjectMocks
    private TouristController touristController;

    private UsernamePasswordAuthenticationToken auth;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(touristController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        auth = new UsernamePasswordAuthenticationToken(
                "user-123", "token",
                List.of(new SimpleGrantedAuthority("ROLE_TOURIST"))
        );
    }

    @Test
    void register_shouldReturn201_whenValid() throws Exception {
        TouristRegistrationRequest request = TouristRegistrationRequest.builder()
                .userId("user-123")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        TouristResponse response = TouristResponse.builder()
                .id(1L)
                .userId("user-123")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .build();

        when(touristService.register(any(TouristRegistrationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/tourists/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.firstName").value("John"));
    }

    @Test
    void register_shouldReturn400_whenMissingRequiredFields() throws Exception {
        TouristRegistrationRequest request = TouristRegistrationRequest.builder()
                .email("")
                .build();

        mockMvc.perform(post("/api/tourists/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCurrentTourist_shouldReturn200_whenAuthenticated() throws Exception {
        TouristResponse response = TouristResponse.builder()
                .id(1L)
                .userId("user-123")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        when(touristService.getByUserId("user-123")).thenReturn(response);

        mockMvc.perform(get("/api/tourists/me")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value("user-123"));
    }

    @Test
    void updateProfile_shouldReturn200() throws Exception {
        TouristUpdateRequest request = TouristUpdateRequest.builder()
                .firstName("Jane")
                .build();
        TouristResponse response = TouristResponse.builder()
                .id(1L)
                .userId("user-123")
                .firstName("Jane")
                .lastName("Doe")
                .build();

        when(touristService.updateProfile(eq("user-123"), any(TouristUpdateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/tourists/me")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstName").value("Jane"));
    }

    @Test
    void getPreferences_shouldReturn200() throws Exception {
        PreferenceResponse response = PreferenceResponse.builder()
                .preferredBudget("MODERATE")
                .interests(List.of("Adventure"))
                .build();

        when(touristService.getPreferences("user-123")).thenReturn(response);

        mockMvc.perform(get("/api/tourists/me/preferences")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.preferredBudget").value("MODERATE"));
    }

    @Test
    void updatePreferences_shouldReturn200() throws Exception {
        PreferenceRequest request = PreferenceRequest.builder()
                .preferredBudget("LUXURY")
                .interests(List.of("Food", "Culture"))
                .build();
        PreferenceResponse response = PreferenceResponse.builder()
                .preferredBudget("LUXURY")
                .interests(List.of("Food", "Culture"))
                .build();

        when(touristService.updatePreferences(eq("user-123"), any(PreferenceRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/tourists/me/preferences")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.preferredBudget").value("LUXURY"));
    }

    @Test
    void getWallet_shouldReturn200() throws Exception {
        WalletResponse response = WalletResponse.builder()
                .balance(new BigDecimal("50.00"))
                .transactions(List.of())
                .build();

        when(touristService.getWallet("user-123")).thenReturn(response);

        mockMvc.perform(get("/api/tourists/me/wallet")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balance").value(50.00));
    }

    @Test
    void getTouristById_shouldReturn200() throws Exception {
        TouristResponse response = TouristResponse.builder()
                .id(1L)
                .userId("user-123")
                .firstName("John")
                .build();

        when(touristService.getById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/tourists/1")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void getTouristById_shouldReturn404_whenNotFound() throws Exception {
        when(touristService.getById(999L))
                .thenThrow(new ResourceNotFoundException("Tourist", "id", 999L));

        mockMvc.perform(get("/api/tourists/999")
                        .principal(auth))
                .andExpect(status().isNotFound());
    }
}
