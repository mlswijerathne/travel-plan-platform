package com.travelplan.tourist.mapper;

import com.travelplan.tourist.dto.*;
import com.travelplan.tourist.entity.Tourist;
import com.travelplan.tourist.entity.TouristPreference;
import com.travelplan.tourist.entity.WalletTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TouristMapperTest {

    private TouristMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TouristMapper();
    }

    @Test
    void toEntity_shouldMapAllFields() {
        TouristRegistrationRequest request = TouristRegistrationRequest.builder()
                .userId("user-123")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("+94771234567")
                .nationality("British")
                .build();

        Tourist tourist = mapper.toEntity(request);

        assertThat(tourist.getUserId()).isEqualTo("user-123");
        assertThat(tourist.getEmail()).isEqualTo("test@example.com");
        assertThat(tourist.getFirstName()).isEqualTo("John");
        assertThat(tourist.getLastName()).isEqualTo("Doe");
        assertThat(tourist.getPhoneNumber()).isEqualTo("+94771234567");
        assertThat(tourist.getNationality()).isEqualTo("British");
    }

    @Test
    void toResponse_shouldMapTouristWithPreferences() {
        TouristPreference preference = TouristPreference.builder()
                .preferredBudget("MODERATE")
                .interests(List.of("Adventure"))
                .build();
        Tourist tourist = Tourist.builder()
                .id(1L)
                .userId("user-123")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .preference(preference)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        TouristResponse response = mapper.toResponse(tourist);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getPreferences()).isNotNull();
        assertThat(response.getPreferences().getPreferredBudget()).isEqualTo("MODERATE");
    }

    @Test
    void toResponse_shouldHandleNullPreferences() {
        Tourist tourist = Tourist.builder()
                .id(1L)
                .userId("user-123")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .build();

        TouristResponse response = mapper.toResponse(tourist);

        assertThat(response.getPreferences()).isNull();
    }

    @Test
    void toWalletTransactionResponse_shouldMapCorrectly() {
        WalletTransaction tx = WalletTransaction.builder()
                .id(1L)
                .amount(new BigDecimal("25.50"))
                .type(WalletTransaction.TransactionType.REFUND)
                .description("Refund for booking #123")
                .referenceId("booking-123")
                .createdAt(Instant.now())
                .build();

        WalletTransactionResponse response = mapper.toWalletTransactionResponse(tx);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("25.50"));
        assertThat(response.getType()).isEqualTo("REFUND");
        assertThat(response.getReferenceId()).isEqualTo("booking-123");
    }

    @Test
    void updateEntity_shouldOnlyUpdateNonNullFields() {
        Tourist tourist = Tourist.builder()
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("+1234")
                .nationality("British")
                .build();

        TouristUpdateRequest request = TouristUpdateRequest.builder()
                .firstName("Jane")
                .build();

        mapper.updateEntity(tourist, request);

        assertThat(tourist.getFirstName()).isEqualTo("Jane");
        assertThat(tourist.getLastName()).isEqualTo("Doe"); // unchanged
        assertThat(tourist.getPhoneNumber()).isEqualTo("+1234"); // unchanged
    }

    @Test
    void updatePreference_shouldOnlyUpdateNonNullFields() {
        TouristPreference pref = TouristPreference.builder()
                .preferredBudget("BUDGET")
                .travelStyle("Solo")
                .build();

        PreferenceRequest request = PreferenceRequest.builder()
                .preferredBudget("LUXURY")
                .interests(List.of("Food"))
                .build();

        mapper.updatePreference(pref, request);

        assertThat(pref.getPreferredBudget()).isEqualTo("LUXURY");
        assertThat(pref.getTravelStyle()).isEqualTo("Solo"); // unchanged
        assertThat(pref.getInterests()).containsExactly("Food");
    }
}
