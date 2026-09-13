package com.insurance.portal.claim.controller;

import com.insurance.portal.claim.dto.ClaimAnalyticsResponse;
import com.insurance.portal.claim.dto.ClaimResponse;
import com.insurance.portal.claim.dto.ClaimDecisionRequest;
import com.insurance.portal.claim.dto.ClaimStatusResponse;
import com.insurance.portal.claim.dto.ClaimStatusUpdateRequest;
import com.insurance.portal.claim.dto.SubmitClaimRequest;
import com.insurance.portal.claim.service.ClaimService;
import com.insurance.portal.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<ClaimResponse>> submitClaim(@Valid @RequestBody SubmitClaimRequest request) {
        ClaimResponse response = claimService.submitClaim(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<ClaimResponse>builder()
                .success(true).status(HttpStatus.CREATED.value()).message("Claim submitted successfully").data(response).build());
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Page<ClaimResponse>>> getMyClaims(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<ClaimResponse> response = claimService.getMyClaims(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "submittedDate"))
        );

        return ResponseEntity.ok(ApiResponse.<Page<ClaimResponse>>builder()
                .success(true).status(HttpStatus.OK.value()).message("Customer claims fetched successfully").data(response).build());
    }

    @GetMapping("/{claimId}")
    @PreAuthorize("hasAnyRole('CUSTOMER','CLAIMS_OFFICER','ADMIN','AGENT')")
    public ResponseEntity<ApiResponse<ClaimResponse>> getClaim(@PathVariable Long claimId) {
        ClaimResponse response = claimService.getClaim(claimId);
        return ResponseEntity.ok(ApiResponse.<ClaimResponse>builder()
                .success(true).status(HttpStatus.OK.value()).message("Claim fetched successfully").data(response).build());
    }

    @GetMapping("/{claimId}/status")
    @PreAuthorize("hasAnyRole('CUSTOMER','CLAIMS_OFFICER','ADMIN','AGENT')")
    public ResponseEntity<ApiResponse<ClaimStatusResponse>> getClaimStatus(@PathVariable Long claimId) {
        ClaimStatusResponse response = claimService.getClaimStatus(claimId);
        return ResponseEntity.ok(ApiResponse.<ClaimStatusResponse>builder()
                .success(true).status(HttpStatus.OK.value()).message("Claim status fetched successfully").data(response).build());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CLAIMS_OFFICER','ADMIN','AGENT')")
    public ResponseEntity<ApiResponse<Page<ClaimResponse>>> getClaims(
            @RequestParam(required = false) Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<ClaimResponse> response = customerId == null
                ? claimService.getClaims(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "submittedDate")))
                : claimService.getClaimsByCustomer(customerId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "submittedDate")));

        return ResponseEntity.ok(ApiResponse.<Page<ClaimResponse>>builder()
                .success(true).status(HttpStatus.OK.value()).message("Claims fetched successfully").data(response).build());
    }

    @PatchMapping("/{claimId}/review")
    @PreAuthorize("hasAnyRole('CLAIMS_OFFICER','ADMIN')")
    public ResponseEntity<ApiResponse<ClaimResponse>> reviewClaim(
            @PathVariable Long claimId,
            @Valid @RequestBody(required = false) ClaimDecisionRequest request) {

        ClaimResponse response = claimService.reviewClaim(claimId, request == null ? null : request.getDecisionReason());

        return ResponseEntity.ok(ApiResponse.<ClaimResponse>builder()
                .success(true).status(HttpStatus.OK.value()).message("Claim moved to review successfully").data(response).build());
    }

    @PatchMapping("/{claimId}/approve")
    @PreAuthorize("hasAnyRole('CLAIMS_OFFICER','ADMIN')")
    public ResponseEntity<ApiResponse<ClaimResponse>> approveClaim(
            @PathVariable Long claimId,
            @Valid @RequestBody(required = false) ClaimDecisionRequest request) {

        ClaimResponse response = claimService.approveClaim(claimId, request == null ? null : request.getDecisionReason());

        return ResponseEntity.ok(ApiResponse.<ClaimResponse>builder()
                .success(true).status(HttpStatus.OK.value()).message("Claim approved successfully").data(response).build());
    }

    @PatchMapping("/{claimId}/reject")
    @PreAuthorize("hasAnyRole('CLAIMS_OFFICER','ADMIN')")
    public ResponseEntity<ApiResponse<ClaimResponse>> rejectClaim(
            @PathVariable Long claimId,
            @Valid @RequestBody(required = false) ClaimDecisionRequest request) {

        ClaimResponse response = claimService.rejectClaim(claimId, request == null ? null : request.getDecisionReason());

        return ResponseEntity.ok(ApiResponse.<ClaimResponse>builder()
                .success(true).status(HttpStatus.OK.value()).message("Claim rejected successfully").data(response).build());
    }

    @PatchMapping("/{claimId}/settle")
    @PreAuthorize("hasAnyRole('CLAIMS_OFFICER','ADMIN')")
    public ResponseEntity<ApiResponse<ClaimResponse>> settleClaim(
            @PathVariable Long claimId,
            @Valid @RequestBody(required = false) ClaimDecisionRequest request) {

        ClaimResponse response = claimService.settleClaim(claimId, request == null ? null : request.getDecisionReason());

        return ResponseEntity.ok(ApiResponse.<ClaimResponse>builder()
                .success(true).status(HttpStatus.OK.value()).message("Claim settled successfully").data(response).build());
    }

    @PatchMapping("/{claimId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ClaimResponse>> updateClaimStatus(
            @PathVariable Long claimId,
            @Valid @RequestBody ClaimStatusUpdateRequest request) {

        ClaimResponse response = claimService.updateClaimStatus(claimId, request);

        return ResponseEntity.ok(ApiResponse.<ClaimResponse>builder()
                .success(true).status(HttpStatus.OK.value()).message("Claim status updated successfully").data(response).build());
    }

    @GetMapping("/analytics")
    @PreAuthorize("hasAnyRole('ADMIN','CLAIMS_OFFICER')")
    public ResponseEntity<ApiResponse<ClaimAnalyticsResponse>> getClaimsAnalytics() {
        ClaimAnalyticsResponse response = claimService.getClaimsAnalytics();

        return ResponseEntity.ok(ApiResponse.<ClaimAnalyticsResponse>builder()
                .success(true).status(HttpStatus.OK.value()).message("Claims analytics fetched successfully").data(response).build());
    }

    @GetMapping("/queue")
    @PreAuthorize("hasAnyRole('CLAIMS_OFFICER','ADMIN')")
    public ResponseEntity<ApiResponse<Page<ClaimResponse>>> getClaimsQueue(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<ClaimResponse> response = claimService.getClaimsQueue(
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "submittedDate"))
        );

        return ResponseEntity.ok(ApiResponse.<Page<ClaimResponse>>builder()
                .success(true).status(HttpStatus.OK.value()).message("Claims queue fetched successfully").data(response).build());
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<ClaimResponse>>> getMyClaimsLegacy() {
        List<ClaimResponse> response = claimService.getMyClaims();

        return ResponseEntity.ok(ApiResponse.<List<ClaimResponse>>builder()
                .success(true).status(HttpStatus.OK.value()).message("Customer claims fetched successfully").data(response).build());
    }

    @PatchMapping("/by-number/{claimNumber}/approve")
    @PreAuthorize("hasAnyRole('CLAIMS_OFFICER','ADMIN')")
    public ResponseEntity<ApiResponse<ClaimResponse>> approveClaimByNumber(
            @PathVariable String claimNumber,
            @Valid @RequestBody(required = false) ClaimDecisionRequest request) {

        ClaimResponse response = request == null
                ? claimService.approveClaim(claimNumber)
                : claimService.approveClaim(claimNumber, request.getDecisionReason());

        return ResponseEntity.ok(ApiResponse.<ClaimResponse>builder()
                .success(true).status(HttpStatus.OK.value()).message("Claim approved successfully").data(response).build());
    }

    @PatchMapping("/by-number/{claimNumber}/reject")
    @PreAuthorize("hasAnyRole('CLAIMS_OFFICER','ADMIN')")
    public ResponseEntity<ApiResponse<ClaimResponse>> rejectClaimByNumber(
            @PathVariable String claimNumber,
            @Valid @RequestBody(required = false) ClaimDecisionRequest request) {

        ClaimResponse response = request == null
                ? claimService.rejectClaim(claimNumber)
                : claimService.rejectClaim(claimNumber, request.getDecisionReason());

        return ResponseEntity.ok(ApiResponse.<ClaimResponse>builder()
                .success(true).status(HttpStatus.OK.value()).message("Claim rejected successfully").data(response).build());
    }
}

