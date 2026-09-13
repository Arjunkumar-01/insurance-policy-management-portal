package com.insurance.portal.report.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductPerformanceItem {

    private String productName;
    private long policyCount;
}

