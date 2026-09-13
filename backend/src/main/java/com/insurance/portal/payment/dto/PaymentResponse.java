package com.insurance.portal.payment.dto;

import com.insurance.portal.common.enums.PaymentMethod;
import com.insurance.portal.common.enums.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentResponse {
    private Long id;
    private String paymentNumber;
    private String paymentReference;
    private Long policyId;
    private Long customerId;
    private String policyNumber;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private PaymentStatus status;
    private String transactionReference;
    private String invoiceNumber;
    private String receiptNumber;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

