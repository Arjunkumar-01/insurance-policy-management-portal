package com.insurance.portal.payment.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentReceiptResponse {
    private Long paymentId;
    private String paymentNumber;
    private String policyNumber;
    private String customerUsername;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private String invoiceNumber;
    private String receiptNumber;
    private String transactionReference;
}

