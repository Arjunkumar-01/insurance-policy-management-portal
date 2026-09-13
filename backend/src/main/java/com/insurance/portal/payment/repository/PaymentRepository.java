package com.insurance.portal.payment.repository;

import com.insurance.portal.common.enums.PaymentStatus;
import com.insurance.portal.payment.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("select p from Payment p where p.paymentReference = :paymentNumber")
    Optional<Payment> findByPaymentNumber(String paymentNumber);

    List<Payment> findByCustomerUsernameOrderByPaymentDateDesc(String username);

    Page<Payment> findByCustomerIdOrderByPaymentDateDesc(Long customerId, Pageable pageable);

    Page<Payment> findByPolicyIdOrderByPaymentDateDesc(Long policyId, Pageable pageable);

    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);

    List<Payment> findByStatus(PaymentStatus status);

    @Query("select p from Payment p where p.status = com.insurance.portal.common.enums.PaymentStatus.SUCCESSFUL")
    List<Payment> findSuccessfulPayments();

    @Query("select p from Payment p where p.status = com.insurance.portal.common.enums.PaymentStatus.FAILED")
    List<Payment> findFailedPayments();

    Page<Payment> findByPaymentDateBetweenOrderByPaymentDateDesc(LocalDateTime from, LocalDateTime to, Pageable pageable);

    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.status = :status")
    BigDecimal sumByStatus(PaymentStatus status);

    @Query("select function('to_char', p.paymentDate, 'YYYY-MM'), coalesce(sum(p.amount),0) from Payment p where p.status = com.insurance.portal.common.enums.PaymentStatus.SUCCESSFUL group by function('to_char', p.paymentDate, 'YYYY-MM') order by function('to_char', p.paymentDate, 'YYYY-MM')")
    List<Object[]> monthlyRevenue();

    @Query("select p.policy.policyNumber, coalesce(sum(p.amount), 0) from Payment p where p.status = com.insurance.portal.common.enums.PaymentStatus.SUCCESSFUL group by p.policy.policyNumber order by coalesce(sum(p.amount), 0) desc")
    List<Object[]> policyWiseRevenue();

    @Query("select p.policy.product.productName, coalesce(sum(p.amount), 0) from Payment p where p.status = com.insurance.portal.common.enums.PaymentStatus.SUCCESSFUL group by p.policy.product.productName order by coalesce(sum(p.amount), 0) desc")
    List<Object[]> productWiseRevenue();
}

