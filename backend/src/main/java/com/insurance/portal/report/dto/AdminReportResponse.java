package com.insurance.portal.report.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class AdminReportResponse {
    private BigDecimal premiumCollection;
    private BigDecimal claimsRatio;
    private List<ProductPerformanceItem> productPerformance;
    private List<MonthlyRevenueItem> monthlyRevenue;
}

