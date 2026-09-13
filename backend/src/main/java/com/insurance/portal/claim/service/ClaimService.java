package com.insurance.portal.claim.service;

import com.insurance.portal.claim.dto.ClaimAnalyticsResponse;
import com.insurance.portal.claim.dto.ClaimResponse;
import com.insurance.portal.claim.dto.ClaimStatusResponse;
import com.insurance.portal.claim.dto.ClaimStatusUpdateRequest;
import com.insurance.portal.claim.dto.SubmitClaimRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ClaimService {

    ClaimResponse submitClaim(SubmitClaimRequest request);

    ClaimResponse getClaim(Long claimId);

    ClaimStatusResponse getClaimStatus(Long claimId);

    Page<ClaimResponse> getClaims(Pageable pageable);

    Page<ClaimResponse> getMyClaims(Pageable pageable);

    Page<ClaimResponse> getClaimsByCustomer(Long customerId, Pageable pageable);

    Page<ClaimResponse> getClaimsQueue(Pageable pageable);

    ClaimResponse reviewClaim(Long claimId, String decisionReason);

    ClaimResponse approveClaim(Long claimId, String decisionReason);

    ClaimResponse rejectClaim(Long claimId, String decisionReason);

    ClaimResponse settleClaim(Long claimId, String decisionReason);

    ClaimResponse updateClaimStatus(Long claimId, ClaimStatusUpdateRequest request);

    ClaimAnalyticsResponse getClaimsAnalytics();

    ClaimResponse approveClaim(String claimNumber);

    ClaimResponse approveClaim(String claimNumber, String decisionReason);

    ClaimResponse rejectClaim(String claimNumber);

    ClaimResponse rejectClaim(String claimNumber, String decisionReason);

    List<ClaimResponse> getClaimsQueue();

    List<ClaimResponse> getMyClaims();
}

