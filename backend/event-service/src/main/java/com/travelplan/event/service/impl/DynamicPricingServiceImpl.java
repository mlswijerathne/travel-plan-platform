package com.travelplan.event.service.impl;

import com.travelplan.event.model.entity.Event;
import com.travelplan.event.model.entity.TicketTier;
import com.travelplan.event.service.DynamicPricingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
public class DynamicPricingServiceImpl implements DynamicPricingService {

    @Override
    public BigDecimal calculatePrice(Event event, TicketTier ticketTier) {
        BigDecimal basePrice;
        int totalCapacity;
        int availableSeats;

        if (ticketTier != null) {
            basePrice = ticketTier.getPrice();
            totalCapacity = ticketTier.getCapacity();
            availableSeats = ticketTier.getAvailableSeats();
        } else {
            basePrice = event.getTicketPrice();
            totalCapacity = event.getTotalCapacity();
            availableSeats = event.getAvailableSeats();
        }

        BigDecimal scarcityFactor = calculateScarcityFactor(totalCapacity, availableSeats);
        BigDecimal timeMultiplier = calculateTimeMultiplier(event.getStartDateTime());

        BigDecimal calculatedPrice = basePrice.multiply(scarcityFactor).multiply(timeMultiplier)
                .setScale(2, RoundingMode.HALF_UP);

        log.debug("HSTA Pricing - Base: {}, Scarcity: {}, Time: {}, Final: {}",
                basePrice, scarcityFactor, timeMultiplier, calculatedPrice);

        return calculatedPrice;
    }

    private BigDecimal calculateScarcityFactor(int totalCapacity, int availableSeats) {
        if (totalCapacity <= 0) {
            return BigDecimal.ONE;
        }

        double occupancyRate = (double) (totalCapacity - availableSeats) / totalCapacity;

        if (occupancyRate >= 0.90) {
            return new BigDecimal("1.30"); // 30% premium for high scarcity
        } else if (occupancyRate >= 0.80) {
            return new BigDecimal("1.15"); // 15% premium
        } else {
            return BigDecimal.ONE;
        }
    }

    private BigDecimal calculateTimeMultiplier(OffsetDateTime startDateTime) {
        if (startDateTime == null) {
            return BigDecimal.ONE;
        }

        long daysUntilEvent = ChronoUnit.DAYS.between(OffsetDateTime.now(), startDateTime);

        if (daysUntilEvent >= 60) {
            return new BigDecimal("0.85"); // 15% Early bird discount
        } else if (daysUntilEvent <= 3) {
            return new BigDecimal("1.25"); // 25% Last minute premium
        } else {
            return BigDecimal.ONE;
        }
    }
}
