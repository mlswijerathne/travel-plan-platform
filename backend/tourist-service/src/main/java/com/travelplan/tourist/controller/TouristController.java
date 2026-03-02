package com.travelplan.tourist.controller;

import com.travelplan.common.dto.ApiResponse;
import com.travelplan.tourist.dto.*;
import com.travelplan.tourist.service.TouristService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tourists")
@RequiredArgsConstructor
public class TouristController {

    private final TouristService touristService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<TouristResponse>> register(
            @Valid @RequestBody TouristRegistrationRequest request) {
        TouristResponse response = touristService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<TouristResponse>> getCurrentTourist(Authentication authentication) {
        String userId = authentication.getName();
        TouristResponse response = touristService.getByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<TouristResponse>> updateProfile(
            Authentication authentication,
            @Valid @RequestBody TouristUpdateRequest request) {
        String userId = authentication.getName();
        TouristResponse response = touristService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/me/preferences")
    public ResponseEntity<ApiResponse<PreferenceResponse>> getPreferences(Authentication authentication) {
        String userId = authentication.getName();
        PreferenceResponse response = touristService.getPreferences(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/me/preferences")
    public ResponseEntity<ApiResponse<PreferenceResponse>> updatePreferences(
            Authentication authentication,
            @Valid @RequestBody PreferenceRequest request) {
        String userId = authentication.getName();
        PreferenceResponse response = touristService.updatePreferences(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/me/wallet")
    public ResponseEntity<ApiResponse<WalletResponse>> getWallet(Authentication authentication) {
        String userId = authentication.getName();
        WalletResponse response = touristService.getWallet(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TouristResponse>> getTouristById(@PathVariable Long id) {
        TouristResponse response = touristService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
