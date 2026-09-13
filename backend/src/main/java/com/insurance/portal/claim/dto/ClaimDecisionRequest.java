package com.insurance.portal.claim.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClaimDecisionRequest {

    @Size(max = 500, message = "Decision reason cannot exceed 500 characters")
    private String decisionReason;
}

