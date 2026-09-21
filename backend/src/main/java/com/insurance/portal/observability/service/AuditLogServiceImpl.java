package com.insurance.portal.observability.service;

import com.insurance.portal.observability.dto.AuditLogResponse;
import com.insurance.portal.observability.entity.AuditLog;
import com.insurance.portal.observability.repository.AuditLogRepository;
import com.insurance.portal.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    // Routed to its own audit.log file via logback-spring.xml, independent of the class-name application logger
    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String action, String entityType, Object entityIdentifier, String details) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails principal) {
            record(principal.getUsername(), principal.getCustomer().getRole().name(), action,
                    entityType, entityIdentifier, details);
            return;
        }
        record(null, null, action, entityType, entityIdentifier, details);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String username, String role, String action, String entityType,
                       Object entityIdentifier, String details) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .username(sanitize(username, 255))
                    .role(sanitize(role, 50))
                    .action(sanitize(action, 80))
                    .entityType(sanitize(entityType, 80))
                    .entityIdentifier(entityIdentifier == null ? null : sanitize(String.valueOf(entityIdentifier), 255))
                    .eventTimestamp(LocalDateTime.now())
                    .details(sanitize(details, 1000))
                    .build();
            auditLogRepository.save(auditLog);
            AUDIT.info("event=audit action={} entityType={} entityIdentifier={} username={} role={}",
                    action, entityType, entityIdentifier, username, role);
        } catch (RuntimeException ex) {
            log.error("event=audit_write_failure action={} entityType={}", action, entityType, ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByEventTimestampDesc(pageable).map(AuditLogResponse::from);
    }

    private String sanitize(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.replaceAll("[\\r\\n]", " ").substring(0, Math.min(value.length(), maxLength));
    }
}