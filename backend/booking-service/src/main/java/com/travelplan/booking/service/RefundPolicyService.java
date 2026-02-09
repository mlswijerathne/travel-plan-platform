package com.travelplan.booking.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
public class RefundPolicyService {

    public static final String FULL_REFUND = "FULL_REFUND";
    public static final String PARTIAL_REFUND = "PARTIAL_REFUND";
    public static final String NO_REFUND = "NO_REFUND";

    /**
     * Calculates refund based on time-based policy from architecture doc:
     * - Cancellation > 48 hours before start date: 100% refund
     * - Cancellation 24-48 hours before start date: 50% refund
     * - Cancellation < 24 hours before start date: 0% refund
     */
    public RefundResult calculateRefund(BigDecimal totalAmount, LocalDate startDate) {
        long hoursUntilStart = ChronoUnit.HOURS.between(
                java.time.LocalDateTime.now(),
                startDate.atStartOfDay());

        if (hoursUntilStart > 48) {
            BigDecimal refundAmount = totalAmount;
            log.info("Full refund: {} hours before start, amount={}", hoursUntilStart, refundAmount);
            return RefundResult.builder()
                    .refundAmount(refundAmount)
                    .refundPolicy(FULL_REFUND)
                    .refundPercentage(100)
                    .message("Cancellation > 48 hours before start: Full refund processed")
                    .build();
        } else if (hoursUntilStart >= 24) {
            BigDecimal refundAmount = totalAmount
                    .multiply(BigDecimal.valueOf(0.5))
                    .setScale(2, RoundingMode.HALF_UP);
            log.info("Partial refund: {} hours before start, amount={}", hoursUntilStart, refundAmount);
            return RefundResult.builder()
                    .refundAmount(refundAmount)
                    .refundPolicy(PARTIAL_REFUND)
                    .refundPercentage(50)
                    .message("Cancellation 24-48 hours before start: 50% refund processed")
                    .build();
        } else {
            log.info("No refund: {} hours before start", hoursUntilStart);
            return RefundResult.builder()
                    .refundAmount(BigDecimal.ZERO)
                    .refundPolicy(NO_REFUND)
                    .refundPercentage(0)
                    .message("Cancellation < 24 hours before start: No refund")
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefundResult {
        private BigDecimal refundAmount;
        private String refundPolicy;
        private int refundPercentage;
        private String message;
    }
}
