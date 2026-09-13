package com.insurance.portal.policy.dto;

import com.insurance.portal.common.enums.PolicyStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class PolicyCoverageResponse {
    private Long policyId;
    private String policyNumber;
    private BigDecimal coverageAmount;
    private PolicyStatus policyStatus;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean claimEligible;
}

