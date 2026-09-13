package com.insurance.portal.product.dto;

import com.insurance.portal.common.enums.ProductCategory;
import com.insurance.portal.common.enums.ProductStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class ProductResponse {
    private Long id;
    private String productCode;
    private String productName;
    private ProductCategory productCategory;
    private String description;
    private BigDecimal coverageAmount;
    private BigDecimal premiumAmount;
    private Integer policyTenureMonths;
    private ProductStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

