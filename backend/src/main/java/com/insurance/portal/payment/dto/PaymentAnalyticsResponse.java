package com.insurance.portal.payment.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PaymentAnalyticsResponse {
    private long totalPayments;
    private long pendingPayments;
    private long processingPayments;
    private long successfulPayments;
    private long failedPayments;
    private long refundedPayments;
    private BigDecimal totalPremiumCollected;
    private BigDecimal successRate;
    private BigDecimal failureRate;
}

