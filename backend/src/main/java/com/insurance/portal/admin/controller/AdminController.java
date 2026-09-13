package com.insurance.portal.admin.controller;

import com.insurance.portal.admin.dto.AdminOverviewResponse;
import com.insurance.portal.admin.service.AdminService;
import com.insurance.portal.common.dto.ApiResponse;
import com.insurance.portal.constants.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AppConstants.Api.ADMIN_BASE_PATH)
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminOverviewResponse>> getOverview() {
        AdminOverviewResponse response = adminService.getOverview();

        return ResponseEntity.ok(ApiResponse.<AdminOverviewResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message(AppConstants.Message.ADMIN_OVERVIEW_FETCHED)
                .data(response)
                .build());
    }
}

