package com.insurance.portal.policy.service;

import com.insurance.portal.policy.dto.PolicyResponse;
import com.insurance.portal.policy.dto.PolicyCoverageResponse;
import com.insurance.portal.policy.dto.PurchasePolicyRequest;
import com.insurance.portal.policy.dto.UpdatePolicyRequest;
import com.insurance.portal.policy.dto.UpdatePolicyStatusRequest;
import com.insurance.portal.common.enums.PolicyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PolicyService {

    PolicyResponse createPolicy(PurchasePolicyRequest request);

    PolicyResponse purchasePolicy(PurchasePolicyRequest request);

    PolicyResponse getPolicy(Long id);

    Page<PolicyResponse> getPolicies(String query, PolicyStatus status, Long customerId, Long productId, Pageable pageable);

    PolicyResponse renewPolicy(Long id);

    PolicyResponse cancelPolicy(Long id);

    PolicyResponse approveCancellation(Long id);

    PolicyResponse updatePolicy(Long id, UpdatePolicyRequest request);

    PolicyResponse updatePolicyStatus(Long id, UpdatePolicyStatusRequest request);

    Page<PolicyResponse> getCustomerPolicies(Long customerId, Pageable pageable);

    PolicyCoverageResponse getPolicyCoverage(Long id);

    PolicyResponse renewPolicy(String policyNumber);

    PolicyResponse requestPolicyCancellation(String policyNumber);

    PolicyResponse approvePolicyCancellation(String policyNumber);

    List<PolicyResponse> getMyPolicies();

    Page<PolicyResponse> getPoliciesForAdmin(String query, PolicyStatus status, Pageable pageable);

    Page<PolicyResponse> getPendingCancellationRequests(Pageable pageable);
}

