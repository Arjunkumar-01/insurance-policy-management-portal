package com.insurance.portal.policy.repository;

import com.insurance.portal.common.enums.CancellationStatus;
import com.insurance.portal.common.enums.PolicyStatus;
import com.insurance.portal.policy.entity.Policy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PolicyRepository extends JpaRepository<Policy, Long> {

    Optional<Policy> findByPolicyNumber(String policyNumber);

    List<Policy> findByCustomerUsername(String username);

    Page<Policy> findByStatus(PolicyStatus status, Pageable pageable);

    Page<Policy> findByCustomerId(Long customerId, Pageable pageable);

    Page<Policy> findByProductId(Long productId, Pageable pageable);

    Page<Policy> findByCancellationStatus(CancellationStatus cancellationStatus, Pageable pageable);

    Page<Policy> findByCustomerUsernameContainingIgnoreCase(String username, Pageable pageable);

    Page<Policy> findByPolicyNumberContainingIgnoreCase(String policyNumber, Pageable pageable);

    long countByCustomerUsernameAndStatus(String username, PolicyStatus status);

    @Query("select p from Policy p where p.endDate between :today and :thresholdDate")
    List<Policy> findExpiringPolicies(LocalDate today, LocalDate thresholdDate);

    @Query("select p from Policy p where p.endDate <= :asOfDate and p.status in (com.insurance.portal.common.enums.PolicyStatus.ACTIVE, com.insurance.portal.common.enums.PolicyStatus.EXPIRED)")
    List<Policy> findPoliciesEligibleForRenewal(LocalDate asOfDate);

    @Query("select p from Policy p where p.cancellationStatus = com.insurance.portal.common.enums.CancellationStatus.REQUESTED or p.status = com.insurance.portal.common.enums.PolicyStatus.CANCEL_REQUESTED")
    Page<Policy> findPoliciesPendingCancellation(Pageable pageable);

    @Query("select p from Policy p where exists (select 1 from Claim c where c.policy = p)")
    Page<Policy> findPoliciesWithClaims(Pageable pageable);

    @Query("select p.product.productName, count(p) from Policy p group by p.product.productName order by count(p) desc")
    List<Object[]> countPoliciesByProduct();
}

