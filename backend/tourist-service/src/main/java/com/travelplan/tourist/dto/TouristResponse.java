package com.travelplan.tourist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TouristResponse {
    private Long id;
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String nationality;
    private String profileImageUrl;
    private Boolean isActive;
    private PreferenceResponse preferences;
    private Instant createdAt;
    private Instant updatedAt;
}
