package com.insurance.portal.agent.service;

import com.insurance.portal.common.enums.Role;
import com.insurance.portal.customer.dto.CustomerProfileResponse;
import com.insurance.portal.customer.service.CustomerService;
import com.insurance.portal.policy.dto.PolicyResponse;
import com.insurance.portal.policy.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AgentServiceImpl implements AgentService {

    private final CustomerService customerService;
    private final PolicyRepository policyRepository;

    @Override
    public Page<CustomerProfileResponse> searchCustomers(String query, Role role, Boolean enabled, Pageable pageable) {
        return customerService.searchCustomers(query, role, enabled, pageable);
    }

    @Override
    public List<PolicyResponse> getPoliciesByCustomerUsername(String username) {
        return policyRepository.findByCustomerUsername(username)
                .stream()
                .map(policy -> PolicyResponse.builder()
                        .id(policy.getId())
                        .policyNumber(policy.getPolicyNumber())
                        .customerUsername(policy.getCustomer().getUsername())
                        .productName(policy.getProduct().getProductName())
                        .nomineeName(policy.getNomineeName())
                        .nomineeRelation(policy.getNomineeRelation())
                        .startDate(policy.getStartDate())
                        .endDate(policy.getEndDate())
                        .renewalPremium(policy.getRenewalPremium())
                        .status(policy.getStatus())
                        .build())
                .toList();
    }
}

