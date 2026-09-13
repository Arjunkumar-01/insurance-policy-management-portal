package com.insurance.portal.claim.dto;

import com.insurance.portal.common.enums.ClaimStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class ClaimResponse {
    private Long id;
    private String claimNumber;
    private Long policyId;
    private String policyNumber;
    private Long customerId;
    private String submittedBy;
    private LocalDate incidentDate;
    private BigDecimal claimAmount;
    private String description;
    private String supportingDocuments;
    private String decisionReason;
    private ClaimStatus claimStatus;
    private ClaimStatus status;
    private LocalDateTime submittedDate;
    private LocalDateTime reviewedDate;
    private LocalDateTime settledDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

