package com.insurance.portal.policy.dto;

import com.insurance.portal.common.enums.PolicyStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePolicyStatusRequest {

    @NotNull
    private PolicyStatus policyStatus;
}

