package com.insurance.portal.policy.service;

import com.insurance.portal.common.enums.CancellationStatus;
import com.insurance.portal.common.enums.PolicyStatus;
import com.insurance.portal.common.enums.ProductStatus;
import com.insurance.portal.common.enums.RenewalStatus;
import com.insurance.portal.common.enums.Role;
import com.insurance.portal.customer.entity.Customer;
import com.insurance.portal.customer.repository.CustomerRepository;
import com.insurance.portal.exception.BadRequestException;
import com.insurance.portal.exception.ResourceNotFoundException;
import com.insurance.portal.policy.dto.PolicyCoverageResponse;
import com.insurance.portal.policy.dto.PolicyResponse;
import com.insurance.portal.policy.dto.PurchasePolicyRequest;
import com.insurance.portal.policy.dto.UpdatePolicyRequest;
import com.insurance.portal.policy.dto.UpdatePolicyStatusRequest;
import com.insurance.portal.policy.entity.Policy;
import com.insurance.portal.policy.repository.PolicyRepository;
import com.insurance.portal.product.entity.Product;
import com.insurance.portal.product.repository.ProductRepository;
import com.insurance.portal.security.CustomUserDetails;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PolicyServiceImpl implements PolicyService {

    private final PolicyRepository policyRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    @Override
    public PolicyResponse createPolicy(PurchasePolicyRequest request) {
        Customer actor = getAuthenticatedCustomer();
        Role role = actor.getRole();

        Customer targetCustomer;
        if (role == Role.CUSTOMER) {
            targetCustomer = actor;
        } else if (role == Role.AGENT || role == Role.ADMIN) {
            if (request.getCustomerId() == null) {
                throw new BadRequestException("customerId is required for assisted policy creation.");
            }
            targetCustomer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));
        } else {
            throw new AccessDeniedException("You do not have permission to create a policy.");
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new BadRequestException("Inactive products cannot be purchased.");
        }

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(product.getPolicyTenureMonths());

        Policy policy = Policy.builder()
                .policyNumber("POL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .customer(targetCustomer)
                .product(product)
                .nomineeName(request.getNomineeName())
                .nomineeRelation(request.getNomineeRelation())
                .startDate(startDate)
                .endDate(endDate)
                .coverageAmount(product.getCoverageAmount())
                .renewalPremium(product.getPremiumAmount())
                .status(PolicyStatus.ACTIVE)
                .renewalStatus(RenewalStatus.NOT_DUE)
                .cancellationStatus(CancellationStatus.NONE)
                .build();

        return toResponse(policyRepository.save(policy));
    }

    @Override
    public PolicyResponse purchasePolicy(PurchasePolicyRequest request) {
        return createPolicy(request);
    }

    @Override
    public PolicyResponse getPolicy(Long id) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with id: " + id));

        authorizeRead(policy);
        return toResponse(policy);
    }

    @Override
    public Page<PolicyResponse> getPolicies(String query, PolicyStatus status, Long customerId, Long productId, Pageable pageable) {
        Customer actor = getAuthenticatedCustomer();

        if (actor.getRole() == Role.CUSTOMER) {
            return policyRepository.findByCustomerId(actor.getId(), pageable).map(this::toResponse);
        }

        Page<Policy> page;
        if (customerId != null) {
            page = policyRepository.findByCustomerId(customerId, pageable);
        } else if (productId != null) {
            page = policyRepository.findByProductId(productId, pageable);
        } else if (status != null) {
            page = policyRepository.findByStatus(status, pageable);
        } else if (query != null && !query.isBlank()) {
            Page<Policy> byPolicyNumber = policyRepository.findByPolicyNumberContainingIgnoreCase(query.trim(), pageable);
            page = byPolicyNumber.isEmpty()
                    ? policyRepository.findByCustomerUsernameContainingIgnoreCase(query.trim(), pageable)
                    : byPolicyNumber;
        } else {
            page = policyRepository.findAll(pageable);
        }

        return page.map(this::toResponse);
    }

    @Override
    public PolicyResponse renewPolicy(Long id) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with id: " + id));

        authorizeRenew(policy);

        if (policy.getStatus() != PolicyStatus.ACTIVE && policy.getStatus() != PolicyStatus.EXPIRED && policy.getStatus() != PolicyStatus.RENEWED) {
            throw new BadRequestException("Only active or expired policies can be renewed.");
        }

        LocalDate startDate = LocalDate.now();
        policy.setStartDate(startDate);
        policy.setEndDate(startDate.plusMonths(policy.getProduct().getPolicyTenureMonths()));
        policy.setStatus(PolicyStatus.RENEWED);
        policy.setRenewalStatus(RenewalStatus.RENEWED);
        policy.setCancellationStatus(CancellationStatus.NONE);

        return toResponse(policyRepository.save(policy));
    }

    @Override
    public PolicyResponse cancelPolicy(Long id) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with id: " + id));

        authorizeCancelRequest(policy);

        if (policy.getStatus() != PolicyStatus.ACTIVE && policy.getStatus() != PolicyStatus.RENEWED) {
            throw new BadRequestException("Only active policies can be requested for cancellation.");
        }

        policy.setStatus(PolicyStatus.CANCEL_REQUESTED);
        policy.setCancellationStatus(CancellationStatus.REQUESTED);
        return toResponse(policyRepository.save(policy));
    }

    @Override
    public PolicyResponse approveCancellation(Long id) {
        ensureAdmin();

        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with id: " + id));

        if (policy.getCancellationStatus() != CancellationStatus.REQUESTED && policy.getStatus() != PolicyStatus.CANCEL_REQUESTED) {
            throw new BadRequestException("Policy is not awaiting cancellation approval.");
        }

        policy.setStatus(PolicyStatus.CANCELLED);
        policy.setCancellationStatus(CancellationStatus.APPROVED);
        return toResponse(policyRepository.save(policy));
    }

    @Override
    public PolicyResponse updatePolicy(Long id, UpdatePolicyRequest request) {
        ensureAdmin();

        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with id: " + id));

        if (request.getNomineeName() != null) {
            policy.setNomineeName(request.getNomineeName().trim());
        }
        if (request.getNomineeRelation() != null) {
            policy.setNomineeRelation(request.getNomineeRelation().trim());
        }
        if (request.getCoverageAmount() != null) {
            policy.setCoverageAmount(request.getCoverageAmount());
        }
        if (request.getPremiumAmount() != null) {
            policy.setRenewalPremium(request.getPremiumAmount());
        }
        if (request.getStartDate() != null) {
            policy.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            policy.setEndDate(request.getEndDate());
        }
        if (policy.getStartDate() != null && policy.getEndDate() != null && policy.getEndDate().isBefore(policy.getStartDate())) {
            throw new BadRequestException("endDate cannot be before startDate.");
        }
        if (request.getPolicyStatus() != null) {
            policy.setStatus(request.getPolicyStatus());
        }
        if (request.getRenewalStatus() != null) {
            policy.setRenewalStatus(request.getRenewalStatus());
        }
        if (request.getCancellationStatus() != null) {
            policy.setCancellationStatus(request.getCancellationStatus());
        }

        return toResponse(policyRepository.save(policy));
    }

    @Override
    public PolicyResponse updatePolicyStatus(Long id, UpdatePolicyStatusRequest request) {
        ensureAdmin();

        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with id: " + id));

        PolicyStatus nextStatus = request.getPolicyStatus();
        policy.setStatus(nextStatus);
        if (nextStatus == PolicyStatus.CANCELLED) {
            policy.setCancellationStatus(CancellationStatus.APPROVED);
        } else if (nextStatus == PolicyStatus.RENEWED) {
            policy.setRenewalStatus(RenewalStatus.RENEWED);
        }

        return toResponse(policyRepository.save(policy));
    }

    @Override
    public Page<PolicyResponse> getCustomerPolicies(Long customerId, Pageable pageable) {
        Customer actor = getAuthenticatedCustomer();
        if (actor.getRole() == Role.CUSTOMER && !actor.getId().equals(customerId)) {
            throw new AccessDeniedException("Customers can access only their own policies.");
        }
        return policyRepository.findByCustomerId(customerId, pageable).map(this::toResponse);
    }

    @Override
    public PolicyCoverageResponse getPolicyCoverage(Long id) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with id: " + id));

        authorizeRead(policy);
        boolean eligibleStatus = policy.getStatus() == PolicyStatus.ACTIVE || policy.getStatus() == PolicyStatus.RENEWED;
        LocalDate today = LocalDate.now();
        boolean claimEligible = eligibleStatus && !today.isBefore(policy.getStartDate()) && !today.isAfter(policy.getEndDate());

        return PolicyCoverageResponse.builder()
                .policyId(policy.getId())
                .policyNumber(policy.getPolicyNumber())
                .coverageAmount(policy.getCoverageAmount())
                .policyStatus(policy.getStatus())
                .startDate(policy.getStartDate())
                .endDate(policy.getEndDate())
                .claimEligible(claimEligible)
                .build();
    }

    @Override
    public PolicyResponse renewPolicy(String policyNumber) {
        return renewPolicy(findPolicyByNumber(policyNumber).getId());
    }

    @Override
    public PolicyResponse requestPolicyCancellation(String policyNumber) {
        return cancelPolicy(findPolicyByNumber(policyNumber).getId());
    }

    @Override
    public PolicyResponse approvePolicyCancellation(String policyNumber) {
        return approveCancellation(findPolicyByNumber(policyNumber).getId());
    }

    @Override
    public List<PolicyResponse> getMyPolicies() {
        Customer customer = getAuthenticatedCustomer();
        return policyRepository.findByCustomerId(customer.getId(), Pageable.unpaged())
                .stream().map(this::toResponse).toList();
    }

    @Override
    public Page<PolicyResponse> getPoliciesForAdmin(String query, PolicyStatus status, Pageable pageable) {
        return getPolicies(query, status, null, null, pageable);
    }

    @Override
    public Page<PolicyResponse> getPendingCancellationRequests(Pageable pageable) {
        ensureAdmin();
        return policyRepository.findPoliciesPendingCancellation(pageable).map(this::toResponse);
    }

    private Policy getPolicyForCurrentCustomer(String policyNumber) {
        Customer customer = getAuthenticatedCustomer();

        Policy policy = findPolicyByNumber(policyNumber);

        if (!policy.getCustomer().getId().equals(customer.getId())) {
            throw new BadRequestException("Policy does not belong to the current customer.");
        }

        return policy;
    }

    private Customer getAuthenticatedCustomer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails principal)) {
            throw new BadRequestException("Authenticated user context is not available.");
        }

        return customerRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated customer not found."));
    }

    private Policy findPolicyByNumber(String policyNumber) {
        return policyRepository.findByPolicyNumber(policyNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found: " + policyNumber));
    }

    private void authorizeRead(Policy policy) {
        Customer actor = getAuthenticatedCustomer();
        if (actor.getRole() == Role.CUSTOMER && !policy.getCustomer().getId().equals(actor.getId())) {
            throw new AccessDeniedException("Customers can access only their own policies.");
        }
    }

    private void authorizeRenew(Policy policy) {
        Customer actor = getAuthenticatedCustomer();
        if (actor.getRole() == Role.CLAIMS_OFFICER) {
            throw new AccessDeniedException("Claims officers have read-only policy access.");
        }
        if (actor.getRole() == Role.CUSTOMER && !policy.getCustomer().getId().equals(actor.getId())) {
            throw new AccessDeniedException("Customers can renew only their own policies.");
        }
    }

    private void authorizeCancelRequest(Policy policy) {
        Customer actor = getAuthenticatedCustomer();
        if (actor.getRole() != Role.CUSTOMER && actor.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only customers and admins can request cancellation.");
        }
        if (actor.getRole() == Role.CUSTOMER && !policy.getCustomer().getId().equals(actor.getId())) {
            throw new AccessDeniedException("Customers can cancel only their own policies.");
        }
    }

    private void ensureAdmin() {
        if (getAuthenticatedCustomer().getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only admins can perform this operation.");
        }
    }

    private PolicyResponse toResponse(Policy policy) {
        BigDecimal premiumAmount = policy.getRenewalPremium();
        return PolicyResponse.builder()
                .id(policy.getId())
                .policyNumber(policy.getPolicyNumber())
                .customerId(policy.getCustomer().getId())
                .customerUsername(policy.getCustomer().getUsername())
                .productId(policy.getProduct().getId())
                .productName(policy.getProduct().getProductName())
                .nomineeName(policy.getNomineeName())
                .nomineeRelation(policy.getNomineeRelation())
                .startDate(policy.getStartDate())
                .endDate(policy.getEndDate())
                .coverageAmount(policy.getCoverageAmount())
                .premiumAmount(premiumAmount)
                .renewalPremium(premiumAmount)
                .policyStatus(policy.getStatus())
                .status(policy.getStatus())
                .renewalStatus(policy.getRenewalStatus())
                .cancellationStatus(policy.getCancellationStatus())
                .createdAt(policy.getCreatedAt())
                .updatedAt(policy.getUpdatedAt())
                .build();
    }
}

