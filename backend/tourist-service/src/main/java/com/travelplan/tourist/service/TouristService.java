package com.travelplan.tourist.service;

import com.travelplan.tourist.dto.*;

public interface TouristService {

    TouristResponse register(TouristRegistrationRequest request);

    TouristResponse getByUserId(String userId);

    TouristResponse getById(Long id);

    TouristResponse updateProfile(String userId, TouristUpdateRequest request);

    PreferenceResponse getPreferences(String userId);

    PreferenceResponse updatePreferences(String userId, PreferenceRequest request);

    WalletResponse getWallet(String userId);
}
