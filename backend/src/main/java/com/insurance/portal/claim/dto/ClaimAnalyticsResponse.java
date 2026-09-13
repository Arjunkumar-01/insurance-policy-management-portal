package com.insurance.portal.claim.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ClaimAnalyticsResponse {
    private long totalClaims;
    private long submittedClaims;
    private long underReviewClaims;
    private long approvedClaims;
    private long rejectedClaims;
    private long settledClaims;
    private BigDecimal approvalRatio;
    private BigDecimal settlementRatio;
}

