package com.insurance.portal.claim.repository;

import com.insurance.portal.claim.entity.Claim;
import com.insurance.portal.common.enums.ClaimStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ClaimRepository extends JpaRepository<Claim, Long> {

    Optional<Claim> findByClaimNumber(String claimNumber);

    List<Claim> findBySubmittedByUsername(String username);

    Page<Claim> findBySubmittedById(Long customerId, Pageable pageable);

    List<Claim> findByPolicyId(Long policyId);

    Page<Claim> findByStatus(ClaimStatus status, Pageable pageable);

    List<Claim> findByStatus(ClaimStatus status);

    @Query("select c from Claim c where c.status = com.insurance.portal.common.enums.ClaimStatus.UNDER_REVIEW")
    Page<Claim> findClaimsUnderReview(Pageable pageable);

    @Query("select c from Claim c where c.status = com.insurance.portal.common.enums.ClaimStatus.APPROVED")
    Page<Claim> findClaimsPendingSettlement(Pageable pageable);

    @Query("select c from Claim c where c.status = com.insurance.portal.common.enums.ClaimStatus.APPROVED")
    List<Claim> findApprovedClaims();

    @Query("select c from Claim c where c.status = com.insurance.portal.common.enums.ClaimStatus.REJECTED")
    List<Claim> findRejectedClaims();

    @Query("select c from Claim c where c.status = com.insurance.portal.common.enums.ClaimStatus.SETTLED")
    List<Claim> findSettledClaims();

    long countBySubmittedByUsername(String username);

    long countBySubmittedById(Long customerId);

    long countByStatus(ClaimStatus status);
}

