package com.insurance.portal.payment.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PaymentRevenueItem {
    private String key;
    private BigDecimal amount;
}

