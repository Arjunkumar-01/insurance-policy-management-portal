package com.insurance.portal.admin.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminOverviewResponse {

    private long totalCustomers;
    private long totalProducts;
    private long totalPolicies;
    private long totalClaims;
    private long totalPayments;
}

