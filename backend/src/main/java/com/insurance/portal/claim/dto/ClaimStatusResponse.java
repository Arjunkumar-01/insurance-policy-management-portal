package com.insurance.portal.claim.dto;

import com.insurance.portal.common.enums.ClaimStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ClaimStatusResponse {
    private Long claimId;
    private String claimNumber;
    private ClaimStatus claimStatus;
    private LocalDateTime submittedDate;
    private LocalDateTime reviewedDate;
    private LocalDateTime settledDate;
    private LocalDateTime updatedAt;
}

