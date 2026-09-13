package com.insurance.portal.report.service;

import com.insurance.portal.claim.repository.ClaimRepository;
import com.insurance.portal.common.enums.ClaimStatus;
import com.insurance.portal.common.enums.PaymentStatus;
import com.insurance.portal.common.enums.PolicyStatus;
import com.insurance.portal.customer.entity.Customer;
import com.insurance.portal.customer.repository.CustomerRepository;
import com.insurance.portal.exception.BadRequestException;
import com.insurance.portal.exception.ResourceNotFoundException;
import com.insurance.portal.payment.repository.PaymentRepository;
import com.insurance.portal.policy.repository.PolicyRepository;
import com.insurance.portal.report.dto.AdminReportResponse;
import com.insurance.portal.report.dto.CustomerReportResponse;
import com.insurance.portal.report.dto.MonthlyRevenueItem;
import com.insurance.portal.report.dto.ProductPerformanceItem;
import com.insurance.portal.security.CustomUserDetails;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportServiceImpl implements ReportService {

    private final PolicyRepository policyRepository;
    private final ClaimRepository claimRepository;
    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;

    @Override
    public CustomerReportResponse getCustomerReport() {
        Customer customer = getAuthenticatedCustomer();

        return CustomerReportResponse.builder()
                .activePolicies(policyRepository.countByCustomerUsernameAndStatus(customer.getUsername(), PolicyStatus.ACTIVE))
                .expiredPolicies(policyRepository.countByCustomerUsernameAndStatus(customer.getUsername(), PolicyStatus.EXPIRED))
                .claimsSubmitted(claimRepository.countBySubmittedByUsername(customer.getUsername()))
                .build();
    }

    @Override
    public AdminReportResponse getAdminReport() {
        BigDecimal premiumCollection = paymentRepository.sumByStatus(PaymentStatus.SUCCESSFUL);

        long approved = claimRepository.countByStatus(ClaimStatus.APPROVED);
        long rejected = claimRepository.countByStatus(ClaimStatus.REJECTED);
        long totalClosed = approved + rejected;

        BigDecimal claimsRatio = totalClosed == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(approved)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalClosed), 2, RoundingMode.HALF_UP);

        List<ProductPerformanceItem> productPerformance = policyRepository.countPoliciesByProduct()
                .stream()
                .map(row -> ProductPerformanceItem.builder()
                        .productName(String.valueOf(row[0]))
                        .policyCount(((Number) row[1]).longValue())
                        .build())
                .toList();

        List<MonthlyRevenueItem> monthlyRevenue = paymentRepository.monthlyRevenue()
                .stream()
                .map(row -> MonthlyRevenueItem.builder()
                        .month(String.valueOf(row[0]))
                        .revenue((BigDecimal) row[1])
                        .build())
                .toList();

        return AdminReportResponse.builder()
                .premiumCollection(premiumCollection)
                .claimsRatio(claimsRatio)
                .productPerformance(productPerformance)
                .monthlyRevenue(monthlyRevenue)
                .build();
    }

    private Customer getAuthenticatedCustomer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails principal)) {
            throw new BadRequestException("Authenticated user context is not available.");
        }

        return customerRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated customer not found."));
    }
}

