package com.travelplan.tourist.mapper;

import com.travelplan.tourist.dto.*;
import com.travelplan.tourist.entity.Tourist;
import com.travelplan.tourist.entity.TouristPreference;
import com.travelplan.tourist.entity.WalletTransaction;
import org.springframework.stereotype.Component;

@Component
public class TouristMapper {

    public Tourist toEntity(TouristRegistrationRequest request) {
        return Tourist.builder()
                .userId(request.getUserId())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .nationality(request.getNationality())
                .profileImageUrl(request.getProfileImageUrl())
                .build();
    }

    public TouristResponse toResponse(Tourist tourist) {
        TouristResponse.TouristResponseBuilder builder = TouristResponse.builder()
                .id(tourist.getId())
                .userId(tourist.getUserId())
                .email(tourist.getEmail())
                .firstName(tourist.getFirstName())
                .lastName(tourist.getLastName())
                .phoneNumber(tourist.getPhoneNumber())
                .nationality(tourist.getNationality())
                .profileImageUrl(tourist.getProfileImageUrl())
                .isActive(tourist.getIsActive())
                .createdAt(tourist.getCreatedAt())
                .updatedAt(tourist.getUpdatedAt());

        if (tourist.getPreference() != null) {
            builder.preferences(toPreferenceResponse(tourist.getPreference()));
        }

        return builder.build();
    }

    public PreferenceResponse toPreferenceResponse(TouristPreference pref) {
        return PreferenceResponse.builder()
                .preferredBudget(pref.getPreferredBudget())
                .travelStyle(pref.getTravelStyle())
                .dietaryRestrictions(pref.getDietaryRestrictions())
                .interests(pref.getInterests())
                .preferredLanguages(pref.getPreferredLanguages())
                .accessibilityNeeds(pref.getAccessibilityNeeds())
                .build();
    }

    public WalletTransactionResponse toWalletTransactionResponse(WalletTransaction tx) {
        return WalletTransactionResponse.builder()
                .id(tx.getId())
                .amount(tx.getAmount())
                .type(tx.getType().name())
                .description(tx.getDescription())
                .referenceId(tx.getReferenceId())
                .createdAt(tx.getCreatedAt())
                .build();
    }

    public void updateEntity(Tourist tourist, TouristUpdateRequest request) {
        if (request.getFirstName() != null) {
            tourist.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            tourist.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            tourist.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getNationality() != null) {
            tourist.setNationality(request.getNationality());
        }
        if (request.getProfileImageUrl() != null) {
            tourist.setProfileImageUrl(request.getProfileImageUrl());
        }
    }

    public void updatePreference(TouristPreference pref, PreferenceRequest request) {
        if (request.getPreferredBudget() != null) {
            pref.setPreferredBudget(request.getPreferredBudget());
        }
        if (request.getTravelStyle() != null) {
            pref.setTravelStyle(request.getTravelStyle());
        }
        if (request.getDietaryRestrictions() != null) {
            pref.setDietaryRestrictions(request.getDietaryRestrictions());
        }
        if (request.getInterests() != null) {
            pref.setInterests(request.getInterests());
        }
        if (request.getPreferredLanguages() != null) {
            pref.setPreferredLanguages(request.getPreferredLanguages());
        }
        if (request.getAccessibilityNeeds() != null) {
            pref.setAccessibilityNeeds(request.getAccessibilityNeeds());
        }
    }
}
