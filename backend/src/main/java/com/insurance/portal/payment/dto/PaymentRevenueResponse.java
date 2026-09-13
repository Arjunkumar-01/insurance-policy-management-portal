package com.insurance.portal.payment.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class PaymentRevenueResponse {
    private BigDecimal totalPremiumCollected;
    private List<PaymentRevenueItem> monthlyRevenue;
    private List<PaymentRevenueItem> policyWiseRevenue;
    private List<PaymentRevenueItem> productWiseRevenue;
}

