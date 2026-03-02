package com.travelplan.guide.dto;

import java.util.List;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class GuideResponse {
    private Long id;
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String bio;
    private List<String> languages;
    private List<String> specializations;
    private Integer experienceYears;
    private java.math.BigDecimal hourlyRate;
    private java.math.BigDecimal dailyRate;
    private java.math.BigDecimal averageRating;
    private Integer reviewCount;
    private String profileImageUrl;
    private Boolean isVerified;
    private Boolean isActive;
    private java.time.OffsetDateTime createdAt;
    private java.time.OffsetDateTime updatedAt;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    public List<String> getSpecializations() {
        return specializations;
    }

    public void setSpecializations(List<String> specializations) {
        this.specializations = specializations;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public java.math.BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(java.math.BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public java.math.BigDecimal getDailyRate() {
        return dailyRate;
    }

    public void setDailyRate(java.math.BigDecimal dailyRate) {
        this.dailyRate = dailyRate;
    }

    public java.math.BigDecimal getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(java.math.BigDecimal averageRating) {
        this.averageRating = averageRating;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public Boolean getVerified() {
        return isVerified;
    }

    public void setVerified(Boolean verified) {
        isVerified = verified;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public java.time.OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.time.OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public java.time.OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(java.time.OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
