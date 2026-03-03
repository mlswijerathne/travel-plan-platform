package com.travelplan.tourist.service.impl;

import com.travelplan.common.exception.ConflictException;
import com.travelplan.common.exception.ResourceNotFoundException;
import com.travelplan.tourist.dto.*;
import com.travelplan.tourist.entity.Tourist;
import com.travelplan.tourist.entity.TouristPreference;
import com.travelplan.tourist.mapper.TouristMapper;
import com.travelplan.tourist.repository.TouristPreferenceRepository;
import com.travelplan.tourist.repository.TouristRepository;
import com.travelplan.tourist.repository.WalletTransactionRepository;
import com.travelplan.tourist.service.TouristService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TouristServiceImpl implements TouristService {

    private final TouristRepository touristRepository;
    private final TouristPreferenceRepository preferenceRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final TouristMapper touristMapper;

    @Override
    @Transactional
    public TouristResponse register(TouristRegistrationRequest request) {
        if (touristRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Tourist", "email", request.getEmail());
        }
        if (touristRepository.existsByUserId(request.getUserId())) {
            throw new ConflictException("Tourist", "userId", request.getUserId());
        }

        Tourist tourist = touristMapper.toEntity(request);
        tourist = touristRepository.save(tourist);

        // Create default preferences if budget level provided
        if (request.getPreferredBudget() != null) {
            TouristPreference preference = TouristPreference.builder()
                    .tourist(tourist)
                    .preferredBudget(request.getPreferredBudget())
                    .build();
            preferenceRepository.save(preference);
            tourist.setPreference(preference);
        }

        log.info("Tourist registered: userId={}, email={}", tourist.getUserId(), tourist.getEmail());
        return touristMapper.toResponse(tourist);
    }

    @Override
    public TouristResponse getByUserId(String userId) {
        Tourist tourist = findTouristByUserId(userId);
        return touristMapper.toResponse(tourist);
    }

    @Override
    public TouristResponse getById(Long id) {
        Tourist tourist = touristRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tourist", "id", id));
        return touristMapper.toResponse(tourist);
    }

    @Override
    @Transactional
    public TouristResponse updateProfile(String userId, TouristUpdateRequest request) {
        Tourist tourist = findTouristByUserId(userId);
        touristMapper.updateEntity(tourist, request);
        tourist = touristRepository.save(tourist);
        log.info("Tourist profile updated: userId={}", userId);
        return touristMapper.toResponse(tourist);
    }

    @Override
    public PreferenceResponse getPreferences(String userId) {
        Tourist tourist = findTouristByUserId(userId);
        TouristPreference preference = preferenceRepository.findByTouristId(tourist.getId())
                .orElse(TouristPreference.builder().build());
        return touristMapper.toPreferenceResponse(preference);
    }

    @Override
    @Transactional
    public PreferenceResponse updatePreferences(String userId, PreferenceRequest request) {
        Tourist tourist = findTouristByUserId(userId);
        TouristPreference preference = preferenceRepository.findByTouristId(tourist.getId())
                .orElseGet(() -> TouristPreference.builder().tourist(tourist).build());

        touristMapper.updatePreference(preference, request);
        preference = preferenceRepository.save(preference);
        log.info("Tourist preferences updated: userId={}", userId);
        return touristMapper.toPreferenceResponse(preference);
    }

    @Override
    public WalletResponse getWallet(String userId) {
        Tourist tourist = findTouristByUserId(userId);
        BigDecimal balance = walletTransactionRepository.calculateBalance(tourist.getId());
        var transactions = walletTransactionRepository
                .findByTouristIdOrderByCreatedAtDesc(tourist.getId())
                .stream()
                .map(touristMapper::toWalletTransactionResponse)
                .collect(Collectors.toList());

        return WalletResponse.builder()
                .balance(balance)
                .transactions(transactions)
                .build();
    }

    @Override
    @Transactional
    public void creditWallet(String userId, BigDecimal amount, String description, String referenceId) {
        Tourist tourist = findTouristByUserId(userId);
        com.travelplan.tourist.entity.WalletTransaction transaction =
                com.travelplan.tourist.entity.WalletTransaction.builder()
                        .tourist(tourist)
                        .amount(amount)
                        .type(com.travelplan.tourist.entity.WalletTransaction.TransactionType.REFUND)
                        .description(description)
                        .referenceId(referenceId)
                        .build();
        walletTransactionRepository.save(transaction);
        log.info("Wallet credited: userId={}, amount={}, ref={}", userId, amount, referenceId);
    }

    private Tourist findTouristByUserId(String userId) {
        return touristRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tourist", "userId", userId));
    }
}
