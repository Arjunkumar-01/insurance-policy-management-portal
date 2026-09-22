package com.insurance.portal.observability.service;

import com.insurance.portal.observability.dto.AuditLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditLogService {
    void record(String action, String entityType, Object entityIdentifier, String details);

    void record(String username, String role, String action, String entityType,
                Object entityIdentifier, String details);

    Page<AuditLogResponse> getAuditLogs(Pageable pageable);
}