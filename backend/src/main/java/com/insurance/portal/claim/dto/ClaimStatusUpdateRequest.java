package com.insurance.portal.claim.dto;

import com.insurance.portal.common.enums.ClaimStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClaimStatusUpdateRequest {

    @NotNull
    private ClaimStatus claimStatus;

    private String decisionReason;
}

