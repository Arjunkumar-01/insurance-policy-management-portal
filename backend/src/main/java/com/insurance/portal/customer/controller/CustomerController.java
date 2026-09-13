package com.insurance.portal.customer.controller;

import com.insurance.portal.common.dto.ApiResponse;
import com.insurance.portal.common.enums.Role;
import com.insurance.portal.customer.dto.CustomerProfileResponse;
import com.insurance.portal.customer.dto.CreateCustomerRequest;
import com.insurance.portal.customer.dto.UpdateCustomerProfileRequest;
import com.insurance.portal.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CustomerProfileResponse>> getCurrentProfile() {
        CustomerProfileResponse response = customerService.getCurrentCustomerProfile();

        return ResponseEntity.ok(
                ApiResponse.<CustomerProfileResponse>builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("Customer profile fetched successfully")
                        .data(response)
                        .build()
        );
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<CustomerProfileResponse>> updateCurrentProfile(
            @Valid @RequestBody UpdateCustomerProfileRequest request) {

        CustomerProfileResponse response = customerService.updateCurrentCustomerProfile(request);

        return ResponseEntity.ok(
                ApiResponse.<CustomerProfileResponse>builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("Customer profile updated successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CustomerProfileResponse>> getCustomerById(@PathVariable Long customerId) {
        CustomerProfileResponse response = customerService.getCustomerById(customerId);

        return ResponseEntity.ok(
                ApiResponse.<CustomerProfileResponse>builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("Customer fetched successfully")
                        .data(response)
                        .build()
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CustomerProfileResponse>> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request) {

        CustomerProfileResponse response = customerService.createCustomer(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<CustomerProfileResponse>builder()
                        .success(true)
                        .status(HttpStatus.CREATED.value())
                        .message("Customer created successfully")
                        .data(response)
                        .build());
    }

    @DeleteMapping("/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable Long customerId) {
        customerService.deleteCustomerById(customerId);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("Customer deleted successfully")
                        .build()
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<CustomerProfileResponse>>> searchCustomers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<CustomerProfileResponse> response = customerService.searchCustomers(query, role, enabled, pageable);

        return ResponseEntity.ok(ApiResponse.<Page<CustomerProfileResponse>>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Customers fetched successfully")
                .data(response)
                .build());
    }
}


