package com.travelplan.tourist.service;

import com.travelplan.common.exception.ConflictException;
import com.travelplan.common.exception.ResourceNotFoundException;
import com.travelplan.tourist.dto.*;
import com.travelplan.tourist.entity.Tourist;
import com.travelplan.tourist.entity.TouristPreference;
import com.travelplan.tourist.entity.WalletTransaction;
import com.travelplan.tourist.mapper.TouristMapper;
import com.travelplan.tourist.repository.TouristPreferenceRepository;
import com.travelplan.tourist.repository.TouristRepository;
import com.travelplan.tourist.repository.WalletTransactionRepository;
import com.travelplan.tourist.service.impl.TouristServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TouristServiceImplTest {

    @Mock
    private TouristRepository touristRepository;
    @Mock
    private TouristPreferenceRepository preferenceRepository;
    @Mock
    private WalletTransactionRepository walletTransactionRepository;
    @Mock
    private TouristMapper touristMapper;

    @InjectMocks
    private TouristServiceImpl touristService;

    private Tourist tourist;
    private TouristResponse touristResponse;

    @BeforeEach
    void setUp() {
        tourist = Tourist.builder()
                .id(1L)
                .userId("user-123")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .nationality("British")
                .isActive(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        touristResponse = TouristResponse.builder()
                .id(1L)
                .userId("user-123")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .nationality("British")
                .isActive(true)
                .build();
    }

    @Test
    void register_shouldCreateTourist_whenValidRequest() {
        TouristRegistrationRequest request = TouristRegistrationRequest.builder()
                .userId("user-123")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .nationality("British")
                .preferredBudget("MODERATE")
                .build();

        when(touristRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(touristRepository.existsByUserId("user-123")).thenReturn(false);
        when(touristMapper.toEntity(request)).thenReturn(tourist);
        when(touristRepository.save(any(Tourist.class))).thenReturn(tourist);
        when(preferenceRepository.save(any(TouristPreference.class)))
                .thenReturn(TouristPreference.builder().id(1L).tourist(tourist).preferredBudget("MODERATE").build());
        when(touristMapper.toResponse(any(Tourist.class))).thenReturn(touristResponse);

        TouristResponse result = touristService.register(request);

        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(touristRepository).save(any(Tourist.class));
        verify(preferenceRepository).save(any(TouristPreference.class));
    }

    @Test
    void register_shouldThrowConflict_whenEmailExists() {
        TouristRegistrationRequest request = TouristRegistrationRequest.builder()
                .userId("user-123")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        when(touristRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> touristService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("email");
    }

    @Test
    void register_shouldThrowConflict_whenUserIdExists() {
        TouristRegistrationRequest request = TouristRegistrationRequest.builder()
                .userId("user-123")
                .email("new@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        when(touristRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(touristRepository.existsByUserId("user-123")).thenReturn(true);

        assertThatThrownBy(() -> touristService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("userId");
    }

    @Test
    void getByUserId_shouldReturnTourist_whenExists() {
        when(touristRepository.findByUserId("user-123")).thenReturn(Optional.of(tourist));
        when(touristMapper.toResponse(tourist)).thenReturn(touristResponse);

        TouristResponse result = touristService.getByUserId("user-123");

        assertThat(result.getUserId()).isEqualTo("user-123");
    }

    @Test
    void getByUserId_shouldThrowNotFound_whenNotExists() {
        when(touristRepository.findByUserId("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> touristService.getByUserId("nonexistent"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getById_shouldReturnTourist_whenExists() {
        when(touristRepository.findById(1L)).thenReturn(Optional.of(tourist));
        when(touristMapper.toResponse(tourist)).thenReturn(touristResponse);

        TouristResponse result = touristService.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void updateProfile_shouldUpdateAndReturn() {
        TouristUpdateRequest request = TouristUpdateRequest.builder()
                .firstName("Jane")
                .build();

        when(touristRepository.findByUserId("user-123")).thenReturn(Optional.of(tourist));
        when(touristRepository.save(any(Tourist.class))).thenReturn(tourist);
        when(touristMapper.toResponse(any(Tourist.class))).thenReturn(touristResponse);

        TouristResponse result = touristService.updateProfile("user-123", request);

        assertThat(result).isNotNull();
        verify(touristMapper).updateEntity(tourist, request);
        verify(touristRepository).save(tourist);
    }

    @Test
    void getPreferences_shouldReturnDefaults_whenNoneExist() {
        when(touristRepository.findByUserId("user-123")).thenReturn(Optional.of(tourist));
        when(preferenceRepository.findByTouristId(1L)).thenReturn(Optional.empty());
        when(touristMapper.toPreferenceResponse(any(TouristPreference.class)))
                .thenReturn(PreferenceResponse.builder().build());

        PreferenceResponse result = touristService.getPreferences("user-123");

        assertThat(result).isNotNull();
    }

    @Test
    void updatePreferences_shouldCreateNew_whenNoneExist() {
        PreferenceRequest request = PreferenceRequest.builder()
                .preferredBudget("LUXURY")
                .interests(List.of("Adventure", "Food"))
                .build();
        TouristPreference savedPref = TouristPreference.builder()
                .id(1L)
                .tourist(tourist)
                .preferredBudget("LUXURY")
                .build();
        PreferenceResponse prefResponse = PreferenceResponse.builder()
                .preferredBudget("LUXURY")
                .interests(List.of("Adventure", "Food"))
                .build();

        when(touristRepository.findByUserId("user-123")).thenReturn(Optional.of(tourist));
        when(preferenceRepository.findByTouristId(1L)).thenReturn(Optional.empty());
        when(preferenceRepository.save(any(TouristPreference.class))).thenReturn(savedPref);
        when(touristMapper.toPreferenceResponse(savedPref)).thenReturn(prefResponse);

        PreferenceResponse result = touristService.updatePreferences("user-123", request);

        assertThat(result.getPreferredBudget()).isEqualTo("LUXURY");
        verify(preferenceRepository).save(any(TouristPreference.class));
    }

    @Test
    void getWallet_shouldReturnBalanceAndTransactions() {
        WalletTransaction tx = WalletTransaction.builder()
                .id(1L)
                .tourist(tourist)
                .amount(new BigDecimal("50.00"))
                .type(WalletTransaction.TransactionType.REFUND)
                .description("Booking refund")
                .createdAt(Instant.now())
                .build();
        WalletTransactionResponse txResponse = WalletTransactionResponse.builder()
                .id(1L)
                .amount(new BigDecimal("50.00"))
                .type("REFUND")
                .description("Booking refund")
                .build();

        when(touristRepository.findByUserId("user-123")).thenReturn(Optional.of(tourist));
        when(walletTransactionRepository.calculateBalance(1L)).thenReturn(new BigDecimal("50.00"));
        when(walletTransactionRepository.findByTouristIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(tx));
        when(touristMapper.toWalletTransactionResponse(tx)).thenReturn(txResponse);

        WalletResponse result = touristService.getWallet("user-123");

        assertThat(result.getBalance()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(result.getTransactions()).hasSize(1);
    }
}
