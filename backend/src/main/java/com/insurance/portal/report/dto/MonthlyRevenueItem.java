package com.insurance.portal.report.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class MonthlyRevenueItem {

    private String month;
    private BigDecimal revenue;
}

