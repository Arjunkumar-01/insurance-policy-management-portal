package com.insurance.portal.report.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CustomerReportResponse {
    private long activePolicies;
    private long expiredPolicies;
    private long claimsSubmitted;
}

