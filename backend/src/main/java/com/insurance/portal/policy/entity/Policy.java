package com.insurance.portal.policy.entity;

import com.insurance.portal.common.entity.BaseEntity;
import com.insurance.portal.common.enums.CancellationStatus;
import com.insurance.portal.common.enums.PolicyStatus;
import com.insurance.portal.common.enums.RenewalStatus;
import com.insurance.portal.claim.entity.Claim;
import com.insurance.portal.customer.entity.Customer;
import com.insurance.portal.payment.entity.Payment;
import com.insurance.portal.product.entity.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "policies")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Policy extends BaseEntity {

    @Column(nullable = false, unique = true, length = 40)
    private String policyNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 100)
    private String nomineeName;

    @Column(nullable = false, length = 60)
    private String nomineeRelation;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal coverageAmount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal renewalPremium;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PolicyStatus status;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RenewalStatus renewalStatus = RenewalStatus.NOT_DUE;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CancellationStatus cancellationStatus = CancellationStatus.NONE;

    @Builder.Default
    @OneToMany(mappedBy = "policy")
    private List<Payment> payments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "policy")
    private List<Claim> claims = new ArrayList<>();

    @PrePersist
    void applyDefaults() {
        if (status == null) {
            status = PolicyStatus.PENDING;
        }
        if (renewalStatus == null) {
            renewalStatus = RenewalStatus.NOT_DUE;
        }
        if (cancellationStatus == null) {
            cancellationStatus = CancellationStatus.NONE;
        }
    }
}

