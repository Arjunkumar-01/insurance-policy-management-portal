package com.insurance.portal.admin.service;

import com.insurance.portal.admin.dto.AdminOverviewResponse;
import com.insurance.portal.claim.repository.ClaimRepository;
import com.insurance.portal.customer.repository.CustomerRepository;
import com.insurance.portal.payment.repository.PaymentRepository;
import com.insurance.portal.policy.repository.PolicyRepository;
import com.insurance.portal.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final PolicyRepository policyRepository;
    private final ClaimRepository claimRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public AdminOverviewResponse getOverview() {
        return AdminOverviewResponse.builder()
                .totalCustomers(customerRepository.count())
                .totalProducts(productRepository.count())
                .totalPolicies(policyRepository.count())
                .totalClaims(claimRepository.count())
                .totalPayments(paymentRepository.count())
                .build();
    }
}

