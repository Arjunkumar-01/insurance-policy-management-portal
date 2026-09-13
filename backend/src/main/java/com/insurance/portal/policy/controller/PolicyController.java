package com.insurance.portal.policy.controller;

import com.insurance.portal.common.dto.ApiResponse;
import com.insurance.portal.common.enums.PolicyStatus;
import com.insurance.portal.policy.dto.PolicyCoverageResponse;
import com.insurance.portal.policy.dto.PolicyResponse;
import com.insurance.portal.policy.dto.PurchasePolicyRequest;
import com.insurance.portal.policy.dto.UpdatePolicyRequest;
import com.insurance.portal.policy.dto.UpdatePolicyStatusRequest;
import com.insurance.portal.policy.service.PolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER','AGENT','ADMIN')")
    public ResponseEntity<ApiResponse<PolicyResponse>> createPolicy(@Valid @RequestBody PurchasePolicyRequest request) {
        PolicyResponse response = policyService.createPolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<PolicyResponse>builder()
                .success(true).status(HttpStatus.CREATED.value()).message("Policy created successfully").data(response).build());
    }

    @PostMapping("/purchase")
    @PreAuthorize("hasAnyRole('CUSTOMER','AGENT','ADMIN')")
    public ResponseEntity<ApiResponse<PolicyResponse>> purchasePolicy(@Valid @RequestBody PurchasePolicyRequest request) {
        PolicyResponse response = policyService.purchasePolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<PolicyResponse>builder()
                .success(true).status(HttpStatus.CREATED.value()).message("Policy purchased successfully").data(response).build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER','AGENT','CLAIMS_OFFICER','ADMIN')")
    public ResponseEntity<ApiResponse<PolicyResponse>> getPolicy(@PathVariable Long id) {
        PolicyResponse response = policyService.getPolicy(id);
        return ResponseEntity.ok(ApiResponse.<PolicyResponse>builder()
                .success(true).status(HttpStatus.OK.value()).message("Policy fetched successfully").data(response).build());
    }

    @PostMapping("/{id}/renew")
    @PreAuthorize("hasAnyRole('CUSTOMER','AGENT','ADMIN')")
    public ResponseEntity<ApiResponse<PolicyResponse>> renewPolicy(@PathVariable Long id) {
        PolicyResponse response = policyService.renewPolicy(id);
        return ResponseEntity.ok(ApiResponse.<PolicyResponse>builder()
                .success(true).status(HttpStatus.OK.value()).message("Policy renewed successfully").data(response).build());
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public ResponseEntity<ApiResponse<PolicyResponse>> cancelPolicy(@PathVariable Long id) {
        PolicyResponse response = policyService.cancelPolicy(id);
        return ResponseEntity.ok(ApiResponse.<PolicyResponse>builder()
                .success(true).status(HttpStatus.OK.value()).message("Policy cancellation requested").data(response).build());
    }

    @PatchMapping("/{id}/approve-cancellation")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PolicyResponse>> approvePolicyCancellation(@PathVariable Long id) {
        PolicyResponse response = policyService.approveCancellation(id);
        return ResponseEntity.ok(ApiResponse.<PolicyResponse>builder()
                .success(true).status(HttpStatus.OK.value()).message("Policy cancellation approved").data(response).build());
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<PolicyResponse>>> getMyPolicies() {
        List<PolicyResponse> response = policyService.getMyPolicies();
        return ResponseEntity.ok(ApiResponse.<List<PolicyResponse>>builder()
                .success(true).status(HttpStatus.OK.value()).message("Policies fetched successfully").data(response).build());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER','AGENT','CLAIMS_OFFICER','ADMIN')")
    public ResponseEntity<ApiResponse<Page<PolicyResponse>>> getPolicies(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) PolicyStatus status,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<PolicyResponse> response = policyService.getPolicies(query, status, customerId, productId, pageable);

        return ResponseEntity.ok(ApiResponse.<Page<PolicyResponse>>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Policies fetched successfully")
                .data(response)
                .build());
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('AGENT','ADMIN','CUSTOMER')")
    public ResponseEntity<ApiResponse<Page<PolicyResponse>>> getPoliciesByCustomer(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<PolicyResponse> response = policyService.getCustomerPolicies(
                customerId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        return ResponseEntity.ok(ApiResponse.<Page<PolicyResponse>>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Customer policies fetched successfully")
                .data(response)
                .build());
    }

    @GetMapping("/{id}/coverage")
    @PreAuthorize("hasAnyRole('CUSTOMER','AGENT','CLAIMS_OFFICER','ADMIN')")
    public ResponseEntity<ApiResponse<PolicyCoverageResponse>> getPolicyCoverage(@PathVariable Long id) {
        PolicyCoverageResponse response = policyService.getPolicyCoverage(id);
        return ResponseEntity.ok(ApiResponse.<PolicyCoverageResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Policy coverage fetched successfully")
                .data(response)
                .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PolicyResponse>> updatePolicy(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePolicyRequest request) {

        PolicyResponse response = policyService.updatePolicy(id, request);
        return ResponseEntity.ok(ApiResponse.<PolicyResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Policy updated successfully")
                .data(response)
                .build());
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PolicyResponse>> updatePolicyStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePolicyStatusRequest request) {

        PolicyResponse response = policyService.updatePolicyStatus(id, request);
        return ResponseEntity.ok(ApiResponse.<PolicyResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Policy status updated successfully")
                .data(response)
                .build());
    }

    @PostMapping("/by-number/{policyNumber}/renew")
    @PreAuthorize("hasAnyRole('CUSTOMER','AGENT','ADMIN')")
    public ResponseEntity<ApiResponse<PolicyResponse>> renewPolicyByNumber(@PathVariable String policyNumber) {
        PolicyResponse response = policyService.renewPolicy(policyNumber);
        return ResponseEntity.ok(ApiResponse.<PolicyResponse>builder()
                .success(true).status(HttpStatus.OK.value()).message("Policy renewed successfully").data(response).build());
    }

    @PostMapping("/by-number/{policyNumber}/cancel")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public ResponseEntity<ApiResponse<PolicyResponse>> cancelPolicyByNumber(@PathVariable String policyNumber) {
        PolicyResponse response = policyService.requestPolicyCancellation(policyNumber);
        return ResponseEntity.ok(ApiResponse.<PolicyResponse>builder()
                .success(true).status(HttpStatus.OK.value()).message("Policy cancellation requested").data(response).build());
    }

    @PostMapping("/by-number/{policyNumber}/cancel/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PolicyResponse>> approvePolicyCancellationByNumber(@PathVariable String policyNumber) {
        PolicyResponse response = policyService.approvePolicyCancellation(policyNumber);
        return ResponseEntity.ok(ApiResponse.<PolicyResponse>builder()
                .success(true).status(HttpStatus.OK.value()).message("Policy cancellation approved").data(response).build());
    }

    @GetMapping("/pending-cancellations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<PolicyResponse>>> getPendingCancellations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<PolicyResponse> response = policyService.getPendingCancellationRequests(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"))
        );

        return ResponseEntity.ok(ApiResponse.<Page<PolicyResponse>>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Pending cancellation requests fetched successfully")
                .data(response)
                .build());
    }
}

