package com.insurance.portal.policy.dto;

import com.insurance.portal.common.enums.CancellationStatus;
import com.insurance.portal.common.enums.PolicyStatus;
import com.insurance.portal.common.enums.RenewalStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class UpdatePolicyRequest {

    @Size(max = 100)
    private String nomineeName;

    @Size(max = 60)
    private String nomineeRelation;

    @DecimalMin("0.01")
    private BigDecimal coverageAmount;

    @DecimalMin("0.01")
    private BigDecimal premiumAmount;

    private LocalDate startDate;

    private LocalDate endDate;

    private PolicyStatus policyStatus;

    private RenewalStatus renewalStatus;

    private CancellationStatus cancellationStatus;
}

