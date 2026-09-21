package com.insurance.portal.observability.dto;

import com.insurance.portal.observability.entity.AuditLog;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class AuditLogResponse {
    Long id;
    String username;
    String role;
    String action;
    String entityType;
    String entityIdentifier;
    LocalDateTime eventTimestamp;
    String details;

    public static AuditLogResponse from(AuditLog auditLog) {
        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .username(auditLog.getUsername())
                .role(auditLog.getRole())
                .action(auditLog.getAction())
                .entityType(auditLog.getEntityType())
                .entityIdentifier(auditLog.getEntityIdentifier())
                .eventTimestamp(auditLog.getEventTimestamp())
                .details(auditLog.getDetails())
                .build();
    }
}