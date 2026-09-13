package com.insurance.portal.claim.entity;

import com.insurance.portal.common.entity.BaseEntity;
import com.insurance.portal.common.enums.ClaimStatus;
import com.insurance.portal.customer.entity.Customer;
import com.insurance.portal.policy.entity.Policy;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "claims")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Claim extends BaseEntity {

    @Column(nullable = false, unique = true, length = 40)
    private String claimNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by_customer_id", nullable = false)
    private Customer submittedBy;

    @Column(nullable = false)
    private LocalDate incidentDate;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal claimAmount;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(length = 500)
    private String supportingDocuments;

    @Column(length = 500)
    private String decisionReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ClaimStatus status;

    @Column(nullable = false)
    private LocalDateTime submittedDate;

    @Column
    private LocalDateTime reviewedDate;

    @Column
    private LocalDateTime settledDate;
}

