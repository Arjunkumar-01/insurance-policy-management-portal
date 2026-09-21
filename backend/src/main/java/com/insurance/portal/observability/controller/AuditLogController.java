package com.insurance.portal.observability.controller;

import com.insurance.portal.common.dto.ApiResponse;
import com.insurance.portal.observability.dto.AuditLogResponse;
import com.insurance.portal.observability.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAuditLogs(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.<Page<AuditLogResponse>>builder()
                .success(true)
                .status(200)
                .message("Audit logs fetched successfully")
                .data(auditLogService.getAuditLogs(pageable))
                .build());
    }
}