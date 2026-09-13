package com.insurance.portal.customer.service;

import com.insurance.portal.customer.dto.CustomerProfileResponse;
import com.insurance.portal.customer.dto.CreateCustomerRequest;
import com.insurance.portal.customer.dto.UpdateCustomerProfileRequest;
import com.insurance.portal.common.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {

    CustomerProfileResponse getCurrentCustomerProfile();

    CustomerProfileResponse updateCurrentCustomerProfile(UpdateCustomerProfileRequest request);

    CustomerProfileResponse getCustomerById(Long customerId);

    CustomerProfileResponse createCustomer(CreateCustomerRequest request);

    void deleteCustomerById(Long customerId);

    Page<CustomerProfileResponse> searchCustomers(String query, Role role, Boolean enabled, Pageable pageable);
}


