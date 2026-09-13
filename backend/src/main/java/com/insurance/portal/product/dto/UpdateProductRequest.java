package com.insurance.portal.product.dto;

import com.insurance.portal.common.enums.ProductCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateProductRequest {

    @NotBlank
    @Size(max = 40)
    private String productCode;

    @NotBlank
    @Size(max = 100)
    private String productName;

    @NotNull
    private ProductCategory productCategory;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal coverageAmount;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal premiumAmount;

    @NotBlank
    @Size(max = 500)
    private String description;

    @NotNull
    @Positive
    private Integer policyTenureMonths;
}

