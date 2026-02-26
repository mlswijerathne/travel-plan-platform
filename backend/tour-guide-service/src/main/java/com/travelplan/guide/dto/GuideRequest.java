package com.travelplan.guide.dto;

import jakarta.validation.constraints.*;
import java.util.List;
import java.math.BigDecimal;

public class GuideRequest {
    @NotBlank(message = "First name is required")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email
    private String email;

    @Size(max = 20)
    private String phoneNumber;

    private String bio;

    private List<String> languages;

    private List<String> specializations;

    @Min(0)
    private Integer experienceYears;

    @NotNull(message = "Hourly rate is required")
    @Positive
    private java.math.BigDecimal hourlyRate;

    @NotNull(message = "Daily rate is required")
    @Positive
    private java.math.BigDecimal dailyRate;

    private String profileImageUrl;

    public GuideRequest() {
    }

    // Getters and Setters
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

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}