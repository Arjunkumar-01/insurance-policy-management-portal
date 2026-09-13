package com.insurance.portal.policy.dto;

import com.insurance.portal.common.enums.CancellationStatus;
import com.insurance.portal.common.enums.PolicyStatus;
import com.insurance.portal.common.enums.RenewalStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class PolicyResponse {
    private Long id;
    private String policyNumber;
    private Long customerId;
    private String customerUsername;
    private Long productId;
    private String productName;
    private String nomineeName;
    private String nomineeRelation;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal coverageAmount;
    private BigDecimal premiumAmount;
    private BigDecimal renewalPremium;
    private PolicyStatus policyStatus;
    private PolicyStatus status;
    private RenewalStatus renewalStatus;
    private CancellationStatus cancellationStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

