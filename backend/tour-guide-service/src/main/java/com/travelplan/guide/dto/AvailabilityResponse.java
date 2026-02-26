package com.travelplan.guide.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class AvailabilityResponse {
    private Long guideId;
    private Boolean available;
    private BigDecimal dailyRate;
    private List<LocalDate> bookedDates;

    public AvailabilityResponse() {
    }

    public AvailabilityResponse(Long guideId, Boolean available, BigDecimal dailyRate, List<LocalDate> bookedDates) {
        this.guideId = guideId;
        this.available = available;
        this.dailyRate = dailyRate;
        this.bookedDates = bookedDates;
    }

    public Long getGuideId() {
        return guideId;
    }

    public void setGuideId(Long guideId) {
        this.guideId = guideId;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public BigDecimal getDailyRate() {
        return dailyRate;
    }

    public void setDailyRate(BigDecimal dailyRate) {
        this.dailyRate = dailyRate;
    }

    public List<LocalDate> getBookedDates() {
        return bookedDates;
    }

    public void setBookedDates(List<LocalDate> bookedDates) {
        this.bookedDates = bookedDates;
    }
}
