package com.insurance.portal.report.controller;

import com.insurance.portal.common.dto.ApiResponse;
import com.insurance.portal.report.dto.AdminReportResponse;
import com.insurance.portal.report.dto.CustomerReportResponse;
import com.insurance.portal.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/customer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CustomerReportResponse>> getCustomerReport() {
        CustomerReportResponse response = reportService.getCustomerReport();

        return ResponseEntity.ok(ApiResponse.<CustomerReportResponse>builder()
                .success(true).status(HttpStatus.OK.value()).message("Customer report fetched successfully").data(response).build());
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminReportResponse>> getAdminReport() {
        AdminReportResponse response = reportService.getAdminReport();

        return ResponseEntity.ok(ApiResponse.<AdminReportResponse>builder()
                .success(true).status(HttpStatus.OK.value()).message("Admin report fetched successfully").data(response).build());
    }
}

