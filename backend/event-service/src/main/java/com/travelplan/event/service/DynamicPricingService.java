package com.travelplan.event.service;

import com.travelplan.event.model.entity.Event;
import com.travelplan.event.model.entity.TicketTier;

import java.math.BigDecimal;

public interface DynamicPricingService {

    /**
     * Calculates the dynamic price for an event ticket based on the Hybrid
     * Scarcity-Time Algorithm (HSTA).
     * 
     * @param event      The event being booked.
     * @param ticketTier The specific ticket tier (if any) being booked. Null if
     *                   general admission.
     * @return The dynamically calculated price.
     */
    BigDecimal calculatePrice(Event event, TicketTier ticketTier);
}
