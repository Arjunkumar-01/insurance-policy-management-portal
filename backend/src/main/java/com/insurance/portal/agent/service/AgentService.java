package com.insurance.portal.agent.service;

import com.insurance.portal.common.enums.Role;
import com.insurance.portal.customer.dto.CustomerProfileResponse;
import com.insurance.portal.policy.dto.PolicyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AgentService {

    Page<CustomerProfileResponse> searchCustomers(String query, Role role, Boolean enabled, Pageable pageable);

    List<PolicyResponse> getPoliciesByCustomerUsername(String username);
}

