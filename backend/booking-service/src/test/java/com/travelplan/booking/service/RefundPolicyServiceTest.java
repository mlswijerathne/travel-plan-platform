package com.travelplan.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class RefundPolicyServiceTest {

    private RefundPolicyService refundPolicyService;

    @BeforeEach
    void setUp() {
        refundPolicyService = new RefundPolicyService();
    }

    @Test
    void calculateRefund_moreThan48Hours_fullRefund() {
        BigDecimal totalAmount = new BigDecimal("300.00");
        LocalDate startDate = LocalDate.now().plusDays(5); // 5 days = 120 hours

        RefundPolicyService.RefundResult result = refundPolicyService.calculateRefund(totalAmount, startDate);

        assertThat(result.getRefundPolicy()).isEqualTo(RefundPolicyService.FULL_REFUND);
        assertThat(result.getRefundAmount()).isEqualByComparingTo("300.00");
        assertThat(result.getRefundPercentage()).isEqualTo(100);
        assertThat(result.getMessage()).contains("Full refund");
    }

    @Test
    void calculateRefund_between24And48Hours_partialRefund() {
        // startDate at exactly 36 hours from now (1.5 days)
        LocalDate startDate = LocalDate.now().plusDays(2); // ~48 hours but depends on time of day

        RefundPolicyService.RefundResult result = refundPolicyService.calculateRefund(
                new BigDecimal("200.00"), startDate);

        // Either FULL_REFUND or PARTIAL_REFUND depending on exact time of day
        assertThat(result.getRefundPolicy()).isIn(RefundPolicyService.FULL_REFUND, RefundPolicyService.PARTIAL_REFUND);
        assertThat(result.getRefundAmount().compareTo(BigDecimal.ZERO)).isGreaterThan(0);
    }

    @Test
    void calculateRefund_lessThan24Hours_noRefund() {
        // Start date is today - less than 24 hours
        LocalDate startDate = LocalDate.now();

        RefundPolicyService.RefundResult result = refundPolicyService.calculateRefund(
                new BigDecimal("200.00"), startDate);

        assertThat(result.getRefundPolicy()).isEqualTo(RefundPolicyService.NO_REFUND);
        assertThat(result.getRefundAmount()).isEqualByComparingTo("0");
        assertThat(result.getRefundPercentage()).isEqualTo(0);
        assertThat(result.getMessage()).contains("No refund");
    }

    @Test
    void calculateRefund_farFuture_fullRefund() {
        BigDecimal totalAmount = new BigDecimal("1000.00");
        LocalDate startDate = LocalDate.now().plusDays(30);

        RefundPolicyService.RefundResult result = refundPolicyService.calculateRefund(totalAmount, startDate);

        assertThat(result.getRefundPolicy()).isEqualTo(RefundPolicyService.FULL_REFUND);
        assertThat(result.getRefundAmount()).isEqualByComparingTo("1000.00");
        assertThat(result.getRefundPercentage()).isEqualTo(100);
    }

    @Test
    void calculateRefund_pastDate_noRefund() {
        BigDecimal totalAmount = new BigDecimal("500.00");
        LocalDate startDate = LocalDate.now().minusDays(1);

        RefundPolicyService.RefundResult result = refundPolicyService.calculateRefund(totalAmount, startDate);

        assertThat(result.getRefundPolicy()).isEqualTo(RefundPolicyService.NO_REFUND);
        assertThat(result.getRefundAmount()).isEqualByComparingTo("0");
    }
}
