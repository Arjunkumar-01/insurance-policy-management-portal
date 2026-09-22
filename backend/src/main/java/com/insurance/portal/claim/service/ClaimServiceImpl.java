package com.insurance.portal.claim.service;

import com.insurance.portal.claim.dto.ClaimAnalyticsResponse;
import com.insurance.portal.claim.dto.ClaimResponse;
import com.insurance.portal.claim.dto.ClaimStatusResponse;
import com.insurance.portal.claim.dto.ClaimStatusUpdateRequest;
import com.insurance.portal.claim.dto.SubmitClaimRequest;
import com.insurance.portal.claim.entity.Claim;
import com.insurance.portal.claim.repository.ClaimRepository;
import com.insurance.portal.common.enums.ClaimStatus;
import com.insurance.portal.common.enums.PolicyStatus;
import com.insurance.portal.common.enums.Role;
import com.insurance.portal.customer.entity.Customer;
import com.insurance.portal.customer.repository.CustomerRepository;
import com.insurance.portal.exception.BadRequestException;
import com.insurance.portal.exception.ResourceNotFoundException;
import com.insurance.portal.observability.service.AuditLogService;
import com.insurance.portal.policy.entity.Policy;
import com.insurance.portal.policy.repository.PolicyRepository;
import com.insurance.portal.security.CustomUserDetails;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ClaimServiceImpl implements ClaimService {

    private final ClaimRepository claimRepository;
    private final PolicyRepository policyRepository;
    private final CustomerRepository customerRepository;

    private final AuditLogService auditLogService;

    @Override
    public ClaimResponse submitClaim(SubmitClaimRequest request) {
        Customer customer = getAuthenticatedCustomer();
        if (customer.getRole() != Role.CUSTOMER) {
            throw new AccessDeniedException("Only customers can submit claims.");
        }

        Policy policy = policyRepository.findByPolicyNumber(request.getPolicyNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found: " + request.getPolicyNumber()));

        if (!policy.getCustomer().getId().equals(customer.getId())) {
            throw new BadRequestException("Policy does not belong to the current customer.");
        }

        boolean activeForClaims = policy.getStatus() == PolicyStatus.ACTIVE || policy.getStatus() == PolicyStatus.RENEWED;
        LocalDate today = LocalDate.now();
        if (!activeForClaims || today.isBefore(policy.getStartDate()) || today.isAfter(policy.getEndDate())) {
            throw new BadRequestException("Claims are allowed only for active policy coverage periods.");
        }

        validateSupportingDocumentUrl(request.getSupportingDocuments());

        Claim claim = Claim.builder()
                .claimNumber("CLM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .policy(policy)
                .submittedBy(customer)
                .incidentDate(request.getIncidentDate())
                .claimAmount(request.getClaimAmount())
                .description(request.getDescription())
                .supportingDocuments(request.getSupportingDocuments())
                .status(ClaimStatus.SUBMITTED)
                .submittedDate(LocalDateTime.now())
                .build();

        Claim savedClaim = claimRepository.save(claim);
        log.info("event=claim_submitted claimId={} claimNumber={}", savedClaim.getId(), savedClaim.getClaimNumber());
        if (auditLogService != null) {
            auditLogService.record("SUBMIT_CLAIM", "CLAIM", savedClaim.getId(), "Claim submitted");
        }
        return toResponse(savedClaim);
    }

    @Override
    public ClaimResponse getClaim(Long claimId) {
        Claim claim = findById(claimId);
        authorizeRead(claim);
        return toResponse(claim);
    }

    @Override
    public ClaimStatusResponse getClaimStatus(Long claimId) {
        Claim claim = findById(claimId);
        authorizeRead(claim);
        return ClaimStatusResponse.builder()
                .claimId(claim.getId())
                .claimNumber(claim.getClaimNumber())
                .claimStatus(claim.getStatus())
                .submittedDate(claim.getSubmittedDate())
                .reviewedDate(claim.getReviewedDate())
                .settledDate(claim.getSettledDate())
                .updatedAt(claim.getUpdatedAt())
                .build();
    }

    @Override
    public Page<ClaimResponse> getClaims(Pageable pageable) {
        Customer actor = getAuthenticatedCustomer();
        if (actor.getRole() == Role.CUSTOMER) {
            return claimRepository.findBySubmittedById(actor.getId(), pageable).map(this::toResponse);
        }
        if (actor.getRole() == Role.CLAIMS_OFFICER || actor.getRole() == Role.ADMIN || actor.getRole() == Role.AGENT) {
            return claimRepository.findAll(pageable).map(this::toResponse);
        }
        throw new AccessDeniedException("You do not have permission to view claims.");
    }

    @Override
    public Page<ClaimResponse> getMyClaims(Pageable pageable) {
        Customer customer = getAuthenticatedCustomer();
        return claimRepository.findBySubmittedById(customer.getId(), pageable).map(this::toResponse);
    }

    @Override
    public Page<ClaimResponse> getClaimsByCustomer(Long customerId, Pageable pageable) {
        Customer actor = getAuthenticatedCustomer();
        if (actor.getRole() == Role.CUSTOMER && !actor.getId().equals(customerId)) {
            throw new AccessDeniedException("Customers can access only their own claims.");
        }
        if (actor.getRole() != Role.CUSTOMER && actor.getRole() != Role.AGENT && actor.getRole() != Role.CLAIMS_OFFICER && actor.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("You do not have permission to access customer claims.");
        }
        return claimRepository.findBySubmittedById(customerId, pageable).map(this::toResponse);
    }

    @Override
    public Page<ClaimResponse> getClaimsQueue(Pageable pageable) {
        ensureOfficerOrAdmin();
        return claimRepository.findByStatus(ClaimStatus.SUBMITTED, pageable).map(this::toResponse);
    }

    @Override
    public ClaimResponse reviewClaim(Long claimId, String decisionReason) {
        ensureOfficerOrAdmin();
        Claim claim = findById(claimId);
        validateTransition(claim.getStatus(), ClaimStatus.UNDER_REVIEW);
        claim.setStatus(ClaimStatus.UNDER_REVIEW);
        claim.setReviewedDate(LocalDateTime.now());
        claim.setDecisionReason(sanitizeReason(decisionReason));
        Claim savedClaim = claimRepository.save(claim);
        log.info("event=claim_approved claimId={} claimNumber={}", savedClaim.getId(), savedClaim.getClaimNumber());
        if (auditLogService != null) {
            auditLogService.record("APPROVE_CLAIM", "CLAIM", savedClaim.getId(), "Claim approved");
        }
        return toResponse(savedClaim);
    }

    @Override
    public ClaimResponse approveClaim(Long claimId, String decisionReason) {
        ensureOfficerOrAdmin();
        Claim claim = findById(claimId);
        validateTransition(claim.getStatus(), ClaimStatus.APPROVED);
        claim.setStatus(ClaimStatus.APPROVED);
        if (claim.getReviewedDate() == null) {
            claim.setReviewedDate(LocalDateTime.now());
        }
        claim.setDecisionReason(sanitizeReason(decisionReason));
        Claim savedClaim = claimRepository.save(claim);
        log.info("event=claim_rejected claimId={} claimNumber={}", savedClaim.getId(), savedClaim.getClaimNumber());
        if (auditLogService != null) {
            auditLogService.record("REJECT_CLAIM", "CLAIM", savedClaim.getId(), "Claim rejected");
        }
        return toResponse(savedClaim);
    }

    @Override
    public ClaimResponse rejectClaim(Long claimId, String decisionReason) {
        ensureOfficerOrAdmin();
        Claim claim = findById(claimId);
        validateTransition(claim.getStatus(), ClaimStatus.REJECTED);
        claim.setStatus(ClaimStatus.REJECTED);
        if (claim.getReviewedDate() == null) {
            claim.setReviewedDate(LocalDateTime.now());
        }
        if (decisionReason == null || decisionReason.isBlank()) {
            throw new BadRequestException("Decision reason is required for rejected claims.");
        }
        claim.setDecisionReason(sanitizeReason(decisionReason));
        return toResponse(claimRepository.save(claim));
    }

    @Override
    public ClaimResponse settleClaim(Long claimId, String decisionReason) {
        ensureOfficerOrAdmin();
        Claim claim = findById(claimId);
        validateTransition(claim.getStatus(), ClaimStatus.SETTLED);
        claim.setStatus(ClaimStatus.SETTLED);
        claim.setSettledDate(LocalDateTime.now());
        claim.setDecisionReason(sanitizeReason(decisionReason));
        return toResponse(claimRepository.save(claim));
    }

    @Override
    public ClaimResponse updateClaimStatus(Long claimId, ClaimStatusUpdateRequest request) {
        ensureAdmin();
        Claim claim = findById(claimId);
        ClaimStatus target = request.getClaimStatus();
        validateTransition(claim.getStatus(), target);

        if (target == ClaimStatus.UNDER_REVIEW) {
            claim.setReviewedDate(LocalDateTime.now());
        }
        if (target == ClaimStatus.REJECTED && (request.getDecisionReason() == null || request.getDecisionReason().isBlank())) {
            throw new BadRequestException("Decision reason is required for rejected claims.");
        }
        if (target == ClaimStatus.SETTLED) {
            claim.setSettledDate(LocalDateTime.now());
        }

        claim.setDecisionReason(sanitizeReason(request.getDecisionReason()));
        claim.setStatus(target);
        return toResponse(claimRepository.save(claim));
    }

    @Override
    public ClaimAnalyticsResponse getClaimsAnalytics() {
        ensureOfficerOrAdmin();

        long submitted = claimRepository.countByStatus(ClaimStatus.SUBMITTED);
        long underReview = claimRepository.countByStatus(ClaimStatus.UNDER_REVIEW);
        long approved = claimRepository.countByStatus(ClaimStatus.APPROVED);
        long rejected = claimRepository.countByStatus(ClaimStatus.REJECTED);
        long settled = claimRepository.countByStatus(ClaimStatus.SETTLED);
        long total = submitted + underReview + approved + rejected + settled;

        long reviewedPool = approved + rejected;
        BigDecimal approvalRatio = reviewedPool == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(approved)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(reviewedPool), 2, RoundingMode.HALF_UP);

        BigDecimal settlementRatio = approved == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(settled)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(approved), 2, RoundingMode.HALF_UP);

        return ClaimAnalyticsResponse.builder()
                .totalClaims(total)
                .submittedClaims(submitted)
                .underReviewClaims(underReview)
                .approvedClaims(approved)
                .rejectedClaims(rejected)
                .settledClaims(settled)
                .approvalRatio(approvalRatio)
                .settlementRatio(settlementRatio)
                .build();
    }

    @Override
    public ClaimResponse approveClaim(String claimNumber) {
        return approveClaim(claimNumber, "Approved after review.");
    }

    @Override
    public ClaimResponse approveClaim(String claimNumber, String decisionReason) {
        return approveClaim(findByClaimNumber(claimNumber).getId(), decisionReason);
    }

    @Override
    public ClaimResponse rejectClaim(String claimNumber) {
        return rejectClaim(claimNumber, "Rejected after review.");
    }

    @Override
    public ClaimResponse rejectClaim(String claimNumber, String decisionReason) {
        return rejectClaim(findByClaimNumber(claimNumber).getId(), decisionReason);
    }

    @Override
    public List<ClaimResponse> getClaimsQueue() {
        ensureOfficerOrAdmin();
        return claimRepository.findByStatus(ClaimStatus.SUBMITTED)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<ClaimResponse> getMyClaims() {
        Customer customer = getAuthenticatedCustomer();

        return claimRepository.findBySubmittedByUsername(customer.getUsername())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private Claim findByClaimNumber(String claimNumber) {
        return claimRepository.findByClaimNumber(claimNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found: " + claimNumber));
    }

    private Claim findById(Long id) {
        return claimRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + id));
    }

    private Customer getAuthenticatedCustomer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails principal)) {
            throw new BadRequestException("Authenticated user context is not available.");
        }

        return customerRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated customer not found."));
    }

    private ClaimResponse toResponse(Claim claim) {
        return ClaimResponse.builder()
                .id(claim.getId())
                .claimNumber(claim.getClaimNumber())
                .policyId(claim.getPolicy().getId())
                .policyNumber(claim.getPolicy().getPolicyNumber())
                .customerId(claim.getSubmittedBy().getId())
                .submittedBy(claim.getSubmittedBy().getUsername())
                .incidentDate(claim.getIncidentDate())
                .claimAmount(claim.getClaimAmount())
                .description(claim.getDescription())
                .supportingDocuments(claim.getSupportingDocuments())
                .decisionReason(claim.getDecisionReason())
                .claimStatus(claim.getStatus())
                .status(claim.getStatus())
                .submittedDate(claim.getSubmittedDate())
                .reviewedDate(claim.getReviewedDate())
                .settledDate(claim.getSettledDate())
                .createdAt(claim.getCreatedAt())
                .updatedAt(claim.getUpdatedAt())
                .build();
    }

    private void validateTransition(ClaimStatus current, ClaimStatus target) {
        if (current == target) {
            return;
        }
        boolean valid = switch (current) {
            case SUBMITTED -> target == ClaimStatus.UNDER_REVIEW;
            case UNDER_REVIEW -> target == ClaimStatus.APPROVED || target == ClaimStatus.REJECTED;
            case APPROVED -> target == ClaimStatus.SETTLED;
            case REJECTED, SETTLED -> false;
        };
        if (!valid) {
            throw new BadRequestException("Invalid claim status transition from " + current + " to " + target + ".");
        }
    }

    private void authorizeRead(Claim claim) {
        Customer actor = getAuthenticatedCustomer();
        if (actor.getRole() == Role.CUSTOMER && !claim.getSubmittedBy().getId().equals(actor.getId())) {
            throw new AccessDeniedException("Customers can access only their own claims.");
        }
        if (actor.getRole() != Role.CUSTOMER && actor.getRole() != Role.CLAIMS_OFFICER && actor.getRole() != Role.ADMIN && actor.getRole() != Role.AGENT) {
            throw new AccessDeniedException("You do not have permission to view claim details.");
        }
    }

    private void ensureOfficerOrAdmin() {
        Role role = getAuthenticatedCustomer().getRole();
        if (role != Role.CLAIMS_OFFICER && role != Role.ADMIN) {
            throw new AccessDeniedException("Only claims officers and admins can process claims.");
        }
    }

    private void ensureAdmin() {
        if (getAuthenticatedCustomer().getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only admins can perform this operation.");
        }
    }

    private String sanitizeReason(String decisionReason) {
        if (decisionReason == null || decisionReason.isBlank()) {
            return "Reason not provided.";
        }

        if (decisionReason.length() > 500) {
            throw new BadRequestException("Decision reason cannot exceed 500 characters.");
        }

        return decisionReason.trim();
    }

    private void validateSupportingDocumentUrl(String supportingDocumentUrl) {
        if (supportingDocumentUrl == null || supportingDocumentUrl.isBlank()) {
            return;
        }

        try {
            URI uri = new URI(supportingDocumentUrl.trim());
            String scheme = uri.getScheme();
            if (!("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))) {
                throw new BadRequestException("Supporting document must be a valid http/https URL.");
            }
        } catch (URISyntaxException ex) {
            throw new BadRequestException("Supporting document must be a valid URL.");
        }
    }
}

