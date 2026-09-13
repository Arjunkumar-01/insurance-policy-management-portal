package com.insurance.portal.claim.service;

import com.insurance.portal.claim.dto.ClaimStatusUpdateRequest;
import com.insurance.portal.claim.entity.Claim;
import com.insurance.portal.claim.repository.ClaimRepository;
import com.insurance.portal.common.enums.ClaimStatus;
import com.insurance.portal.common.enums.PolicyStatus;
import com.insurance.portal.common.enums.Role;
import com.insurance.portal.customer.entity.Customer;
import com.insurance.portal.customer.repository.CustomerRepository;
import com.insurance.portal.exception.BadRequestException;
import com.insurance.portal.policy.entity.Policy;
import com.insurance.portal.policy.repository.PolicyRepository;
import com.insurance.portal.product.entity.Product;
import com.insurance.portal.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ClaimServiceImplTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    @SuppressWarnings("unused")
    private PolicyRepository policyRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private ClaimServiceImpl claimService;

    private Customer authenticatedCustomer;

    @BeforeEach
    void setUp() {
        authenticatedCustomer = new Customer();
        authenticatedCustomer.setId(10L);
        authenticatedCustomer.setUsername("officer_user");
        authenticatedCustomer.setRole(Role.CLAIMS_OFFICER);
        authenticatedCustomer.setEnabled(true);

        Mockito.when(customerRepository.findByUsername("officer_user"))
                .thenReturn(Optional.of(authenticatedCustomer));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new CustomUserDetails(authenticatedCustomer), null)
        );

    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void approveRequiresUnderReviewTransition() {
        Claim claim = buildClaim(ClaimStatus.SUBMITTED);
        Mockito.when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));

        BadRequestException ex = Assertions.assertThrows(BadRequestException.class,
                () -> claimService.approveClaim(1L, "approved"));

        Assertions.assertTrue(ex.getMessage().contains("Invalid claim status transition"));
    }

    @Test
    void rejectedCannotMoveToApprovedThroughAdminStatusUpdate() {
        authenticatedCustomer.setRole(Role.ADMIN);
        Mockito.when(customerRepository.findByUsername("officer_user"))
                .thenReturn(Optional.of(authenticatedCustomer));

        Claim claim = buildClaim(ClaimStatus.REJECTED);
        Mockito.when(claimRepository.findById(2L)).thenReturn(Optional.of(claim));

        ClaimStatusUpdateRequest request = new ClaimStatusUpdateRequest();
        request.setClaimStatus(ClaimStatus.APPROVED);
        request.setDecisionReason("force approve");

        BadRequestException ex = Assertions.assertThrows(BadRequestException.class,
                () -> claimService.updateClaimStatus(2L, request));

        Assertions.assertTrue(ex.getMessage().contains("Invalid claim status transition"));
    }

    @Test
    void validReviewApproveSettleFlowSucceeds() {
        Claim claim = buildClaim(ClaimStatus.SUBMITTED);
        Mockito.when(claimRepository.findById(3L)).thenReturn(Optional.of(claim));
        Mockito.when(claimRepository.save(ArgumentMatchers.any(Claim.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        claimService.reviewClaim(3L, "review started");
        Assertions.assertEquals(ClaimStatus.UNDER_REVIEW, claim.getStatus());
        Assertions.assertNotNull(claim.getReviewedDate());

        claimService.approveClaim(3L, "approved");
        Assertions.assertEquals(ClaimStatus.APPROVED, claim.getStatus());

        claimService.settleClaim(3L, "settled");
        Assertions.assertEquals(ClaimStatus.SETTLED, claim.getStatus());
        Assertions.assertNotNull(claim.getSettledDate());
    }

    @Test
    void claimsOfficerCanRetrieveAnalytics() {
        Mockito.when(claimRepository.countByStatus(ClaimStatus.SUBMITTED)).thenReturn(5L);
        Mockito.when(claimRepository.countByStatus(ClaimStatus.UNDER_REVIEW)).thenReturn(2L);
        Mockito.when(claimRepository.countByStatus(ClaimStatus.APPROVED)).thenReturn(3L);
        Mockito.when(claimRepository.countByStatus(ClaimStatus.REJECTED)).thenReturn(1L);
        Mockito.when(claimRepository.countByStatus(ClaimStatus.SETTLED)).thenReturn(2L);

        var analytics = claimService.getClaimsAnalytics();
        Assertions.assertEquals(13L, analytics.getTotalClaims());
        Assertions.assertEquals(5L, analytics.getSubmittedClaims());
        Assertions.assertEquals(2L, analytics.getUnderReviewClaims());
        Assertions.assertEquals(3L, analytics.getApprovedClaims());
        Assertions.assertEquals(1L, analytics.getRejectedClaims());
        Assertions.assertEquals(2L, analytics.getSettledClaims());
    }

    private Claim buildClaim(ClaimStatus status) {
        Customer customer = new Customer();
        customer.setId(11L);
        customer.setUsername("john_customer");
        customer.setRole(Role.CUSTOMER);
        customer.setEnabled(true);

        Product product = new Product();
        product.setId(22L);
        product.setProductName("Travel");

        Policy policy = new Policy();
        policy.setId(33L);
        policy.setPolicyNumber("POL-11111111");
        policy.setCustomer(customer);
        policy.setProduct(product);
        policy.setStartDate(LocalDate.now().minusDays(3));
        policy.setEndDate(LocalDate.now().plusDays(30));
        policy.setStatus(PolicyStatus.ACTIVE);

        Claim claim = new Claim();
        claim.setId(1L);
        claim.setClaimNumber("CLM-11111111");
        claim.setPolicy(policy);
        claim.setSubmittedBy(customer);
        claim.setIncidentDate(LocalDate.now().minusDays(1));
        claim.setClaimAmount(BigDecimal.valueOf(1000));
        claim.setDescription("desc");
        claim.setStatus(status);
        claim.setSubmittedDate(LocalDateTime.now().minusDays(1));
        return claim;
    }
}



