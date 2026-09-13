package com.insurance.portal.customer.service;

import com.insurance.portal.customer.dto.CustomerProfileResponse;
import com.insurance.portal.customer.dto.CreateCustomerRequest;
import com.insurance.portal.customer.dto.UpdateCustomerProfileRequest;
import com.insurance.portal.customer.entity.Customer;
import com.insurance.portal.customer.repository.CustomerRepository;
import com.insurance.portal.common.enums.Role;
import com.insurance.portal.exception.BadRequestException;
import com.insurance.portal.exception.ResourceNotFoundException;
import com.insurance.portal.security.CustomUserDetails;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public CustomerProfileResponse getCurrentCustomerProfile() {
        Customer customer = getAuthenticatedCustomer();
        return toProfileResponse(customer);
    }

    @Override
    public CustomerProfileResponse updateCurrentCustomerProfile(UpdateCustomerProfileRequest request) {
        Customer customer = getAuthenticatedCustomer();

        if (!customer.getEmail().equalsIgnoreCase(request.getEmail())
                && customerRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists.");
        }

        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setEmail(request.getEmail());
        customer.setAddressLine1(request.getAddressLine1());
        customer.setAddressLine2(request.getAddressLine2());
        customer.setCity(request.getCity());
        customer.setState(request.getState());
        customer.setCountry(request.getCountry());
        customer.setPostalCode(request.getPostalCode());

        Customer updated = customerRepository.save(customer);
        return toProfileResponse(updated);
    }

    @Override
    public CustomerProfileResponse getCustomerById(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        return toProfileResponse(customer);
    }

    @Override
    public CustomerProfileResponse createCustomer(CreateCustomerRequest request) {
        if (customerRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists.");
        }

        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists.");
        }

        Customer customer = Customer.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() == null ? Role.CUSTOMER : request.getRole())
                .enabled(true)
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .postalCode(request.getPostalCode())
                .build();

        Customer created = customerRepository.save(customer);
        return toProfileResponse(created);
    }

    @Override
    public void deleteCustomerById(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        customerRepository.delete(customer);
    }

    @Override
    public Page<CustomerProfileResponse> searchCustomers(String query, Role role, Boolean enabled, Pageable pageable) {
        Specification<Customer> specification = (root, cq, cb) -> cb.conjunction();

        if (query != null && !query.isBlank()) {
            String likeQuery = "%" + query.trim().toLowerCase() + "%";
            specification = specification.and((root, cq, cb) -> cb.or(
                    cb.like(cb.lower(root.get("firstName")), likeQuery),
                    cb.like(cb.lower(root.get("lastName")), likeQuery),
                    cb.like(cb.lower(root.get("username")), likeQuery),
                    cb.like(cb.lower(root.get("email")), likeQuery)
            ));
        }

        if (role != null) {
            specification = specification.and((root, cq, cb) -> cb.equal(root.get("role"), role));
        }

        if (enabled != null) {
            specification = specification.and((root, cq, cb) -> cb.equal(root.get("enabled"), enabled));
        }

        return customerRepository.findAll(specification, pageable).map(this::toProfileResponse);
    }

    private Customer getAuthenticatedCustomer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails principal)) {
            throw new BadRequestException("Authenticated user context is not available.");
        }

        return customerRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated customer not found."));
    }

    private CustomerProfileResponse toProfileResponse(Customer customer) {
        return CustomerProfileResponse.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .dateOfBirth(customer.getDateOfBirth())
                .gender(customer.getGender())
                .phoneNumber(customer.getPhoneNumber())
                .email(customer.getEmail())
                .username(customer.getUsername())
                .role(customer.getRole())
                .enabled(customer.isEnabled())
                .addressLine1(customer.getAddressLine1())
                .addressLine2(customer.getAddressLine2())
                .city(customer.getCity())
                .state(customer.getState())
                .country(customer.getCountry())
                .postalCode(customer.getPostalCode())
                .build();
    }
}


