package com.insurance.portal.agent.controller;

import com.insurance.portal.agent.service.AgentService;
import com.insurance.portal.common.dto.ApiResponse;
import com.insurance.portal.common.enums.Role;
import com.insurance.portal.customer.dto.CustomerProfileResponse;
import com.insurance.portal.policy.dto.PolicyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/agents")
@RequiredArgsConstructor
@PreAuthorize("hasRole('AGENT')")
public class AgentController {

    private final AgentService agentService;

    @GetMapping("/customers")
    public ResponseEntity<ApiResponse<Page<CustomerProfileResponse>>> searchCustomers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CustomerProfileResponse> response = agentService.searchCustomers(query, role, enabled, pageable);

        return ResponseEntity.ok(ApiResponse.<Page<CustomerProfileResponse>>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Customers fetched successfully")
                .data(response)
                .build());
    }

    @GetMapping("/customers/{username}/policies")
    public ResponseEntity<ApiResponse<List<PolicyResponse>>> getCustomerPolicies(@PathVariable String username) {
        List<PolicyResponse> response = agentService.getPoliciesByCustomerUsername(username);

        return ResponseEntity.ok(ApiResponse.<List<PolicyResponse>>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Customer policies fetched successfully")
                .data(response)
                .build());
    }
}

