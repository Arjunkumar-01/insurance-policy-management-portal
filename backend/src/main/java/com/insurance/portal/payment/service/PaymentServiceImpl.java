package com.insurance.portal.payment.service;

import com.insurance.portal.common.enums.PaymentStatus;
import com.insurance.portal.common.enums.PolicyStatus;
import com.insurance.portal.common.enums.Role;
import com.insurance.portal.customer.entity.Customer;
import com.insurance.portal.customer.repository.CustomerRepository;
import com.insurance.portal.exception.BadRequestException;
import com.insurance.portal.exception.ResourceNotFoundException;
import com.insurance.portal.observability.service.AuditLogService;
import com.insurance.portal.payment.dto.MakePaymentRequest;
import com.insurance.portal.payment.dto.PaymentAnalyticsResponse;
import com.insurance.portal.payment.dto.PaymentReceiptResponse;
import com.insurance.portal.payment.dto.PaymentRevenueItem;
import com.insurance.portal.payment.dto.PaymentRevenueResponse;
import com.insurance.portal.payment.dto.PaymentResponse;
import com.insurance.portal.payment.dto.UpdatePaymentStatusRequest;
import com.insurance.portal.payment.entity.Payment;
import com.insurance.portal.payment.repository.PaymentRepository;
import com.insurance.portal.policy.entity.Policy;
import com.insurance.portal.policy.repository.PolicyRepository;
import com.insurance.portal.security.CustomUserDetails;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PolicyRepository policyRepository;
    private final CustomerRepository customerRepository;

    private final AuditLogService auditLogService;

    @Override
    public PaymentResponse makePayment(MakePaymentRequest request) {
        Customer customer = getAuthenticatedCustomer();
        if (customer.getRole() != Role.CUSTOMER) {
            throw new AccessDeniedException("Only customers can initiate payments.");
        }

        Policy policy = policyRepository.findByPolicyNumber(request.getPolicyNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found: " + request.getPolicyNumber()));

        if (!policy.getCustomer().getId().equals(customer.getId())) {
            throw new BadRequestException("Policy does not belong to the current customer.");
        }

        if (policy.getStatus() != PolicyStatus.ACTIVE && policy.getStatus() != PolicyStatus.RENEWED) {
            throw new BadRequestException("Payments are allowed only for active or renewed policies.");
        }

        String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Payment payment = Payment.builder()
                .paymentReference("PAY-" + suffix)
                .policy(policy)
                .customer(customer)
                .amount(request.getAmount())
                .paymentDate(LocalDateTime.now())
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.PENDING)
                .transactionReference("TXN-" + suffix)
                .invoiceNumber("INV-" + suffix)
                .receiptNumber("RCT-" + suffix)
                .build();

        transition(payment, PaymentStatus.PROCESSING, null);
        transition(payment, PaymentStatus.SUCCESSFUL, null);

        Payment savedPayment = paymentRepository.save(payment);
        log.info("event=payment_completed paymentId={} paymentReference={} status={}",
            savedPayment.getId(), savedPayment.getPaymentReference(), savedPayment.getStatus());
        if (auditLogService != null) {
            auditLogService.record("PAYMENT_COMPLETED", "PAYMENT", savedPayment.getId(), "Payment completed");
        }
        return toResponse(savedPayment);
    }

    @Override
    public PaymentResponse getPayment(Long paymentId) {
        Payment payment = findPayment(paymentId);
        authorizeRead(payment);
        return toResponse(payment);
    }

    @Override
    public Page<PaymentResponse> getMyPayments(Pageable pageable) {
        Customer customer = getAuthenticatedCustomer();
        return paymentRepository.findByCustomerIdOrderByPaymentDateDesc(customer.getId(), pageable).map(this::toResponse);
    }

    @Override
    public Page<PaymentResponse> getPaymentsByCustomer(Long customerId, Pageable pageable) {
        Customer actor = getAuthenticatedCustomer();
        if (actor.getRole() == Role.CUSTOMER && !actor.getId().equals(customerId)) {
            throw new AccessDeniedException("Customers can access only their own payments.");
        }
        if (actor.getRole() != Role.CUSTOMER && actor.getRole() != Role.AGENT && actor.getRole() != Role.ADMIN && actor.getRole() != Role.CLAIMS_OFFICER) {
            throw new AccessDeniedException("You do not have permission to view customer payments.");
        }
        return paymentRepository.findByCustomerIdOrderByPaymentDateDesc(customerId, pageable).map(this::toResponse);
    }

    @Override
    public Page<PaymentResponse> getPolicyPayments(Long policyId, Pageable pageable) {
        Page<Payment> page = paymentRepository.findByPolicyIdOrderByPaymentDateDesc(policyId, pageable);
        Customer actor = getAuthenticatedCustomer();
        if (actor.getRole() == Role.CUSTOMER) {
            page.forEach(this::authorizeRead);
        }
        return page.map(this::toResponse);
    }

    @Override
    public Page<PaymentResponse> getPayments(Pageable pageable) {
        Role role = getAuthenticatedCustomer().getRole();
        if (role != Role.ADMIN && role != Role.CLAIMS_OFFICER) {
            throw new AccessDeniedException("Only admin or claims officer can view all payments.");
        }
        return paymentRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    public PaymentReceiptResponse generateReceipt(Long paymentId) {
        Payment payment = findPayment(paymentId);
        authorizeRead(payment);
        return PaymentReceiptResponse.builder()
                .paymentId(payment.getId())
                .paymentNumber(payment.getPaymentReference())
                .policyNumber(payment.getPolicy().getPolicyNumber())
                .customerUsername(payment.getCustomer().getUsername())
                .amount(payment.getAmount())
                .paymentDate(payment.getPaymentDate())
                .invoiceNumber(payment.getInvoiceNumber())
                .receiptNumber(payment.getReceiptNumber())
                .transactionReference(payment.getTransactionReference())
                .build();
    }

    @Override
    public PaymentResponse updatePaymentStatus(Long paymentId, UpdatePaymentStatusRequest request) {
        ensureAdmin();
        Payment payment = findPayment(paymentId);
        transition(payment, request.getPaymentStatus(), request.getFailureReason());
        return toResponse(paymentRepository.save(payment));
    }

    @Override
    public PaymentRevenueResponse getRevenueSummary() {
        ensureAdmin();

        BigDecimal total = paymentRepository.sumByStatus(PaymentStatus.SUCCESSFUL);
        List<PaymentRevenueItem> monthly = paymentRepository.monthlyRevenue().stream()
                .map(row -> PaymentRevenueItem.builder().key(String.valueOf(row[0])).amount((BigDecimal) row[1]).build())
                .toList();
        List<PaymentRevenueItem> policyWise = paymentRepository.policyWiseRevenue().stream()
                .map(row -> PaymentRevenueItem.builder().key(String.valueOf(row[0])).amount((BigDecimal) row[1]).build())
                .toList();
        List<PaymentRevenueItem> productWise = paymentRepository.productWiseRevenue().stream()
                .map(row -> PaymentRevenueItem.builder().key(String.valueOf(row[0])).amount((BigDecimal) row[1]).build())
                .toList();

        return PaymentRevenueResponse.builder()
                .totalPremiumCollected(total)
                .monthlyRevenue(monthly)
                .policyWiseRevenue(policyWise)
                .productWiseRevenue(productWise)
                .build();
    }

    @Override
    public PaymentAnalyticsResponse getPaymentAnalytics() {
        ensureAdmin();

        long pending = paymentRepository.findByStatus(PaymentStatus.PENDING).size();
        long processing = paymentRepository.findByStatus(PaymentStatus.PROCESSING).size();
        long successful = paymentRepository.findSuccessfulPayments().size();
        long failed = paymentRepository.findFailedPayments().size();
        long refunded = paymentRepository.findByStatus(PaymentStatus.REFUNDED).size();
        long total = pending + processing + successful + failed + refunded;

        BigDecimal successRate = total == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(successful).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);

        BigDecimal failureRate = total == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(failed).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);

        return PaymentAnalyticsResponse.builder()
                .totalPayments(total)
                .pendingPayments(pending)
                .processingPayments(processing)
                .successfulPayments(successful)
                .failedPayments(failed)
                .refundedPayments(refunded)
                .totalPremiumCollected(paymentRepository.sumByStatus(PaymentStatus.SUCCESSFUL))
                .successRate(successRate)
                .failureRate(failureRate)
                .build();
    }

    @Override
    public List<PaymentResponse> getMyPaymentHistory() {
        Customer customer = getAuthenticatedCustomer();

        return paymentRepository.findByCustomerUsernameOrderByPaymentDateDesc(customer.getUsername())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private Payment findPayment(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
    }

    private void authorizeRead(Payment payment) {
        Customer actor = getAuthenticatedCustomer();
        if (actor.getRole() == Role.CUSTOMER && !payment.getCustomer().getId().equals(actor.getId())) {
            throw new AccessDeniedException("Customers can access only their own payments.");
        }
        if (actor.getRole() != Role.CUSTOMER && actor.getRole() != Role.AGENT && actor.getRole() != Role.ADMIN && actor.getRole() != Role.CLAIMS_OFFICER) {
            throw new AccessDeniedException("You do not have permission to access payment details.");
        }
    }

    private void ensureAdmin() {
        if (getAuthenticatedCustomer().getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only admins can perform this operation.");
        }
    }

    private void transition(Payment payment, PaymentStatus target, String failureReason) {
        PaymentStatus current = payment.getStatus();
        if (current == target) {
            return;
        }

        boolean valid = switch (current) {
            case PENDING -> target == PaymentStatus.PROCESSING || target == PaymentStatus.FAILED;
            case PROCESSING -> target == PaymentStatus.SUCCESSFUL || target == PaymentStatus.FAILED;
            case SUCCESSFUL -> target == PaymentStatus.REFUNDED;
            case FAILED, REFUNDED -> false;
        };

        if (!valid) {
            throw new BadRequestException("Invalid payment status transition from " + current + " to " + target + ".");
        }

        if (target == PaymentStatus.FAILED && (failureReason == null || failureReason.isBlank())) {
            throw new BadRequestException("Failure reason is required when payment fails.");
        }

        payment.setStatus(target);
        if (target == PaymentStatus.FAILED) {
            payment.setFailureReason(failureReason.trim());
        } else {
            payment.setFailureReason(null);
        }
    }

    private Customer getAuthenticatedCustomer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails principal)) {
            throw new BadRequestException("Authenticated user context is not available.");
        }

        return customerRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated customer not found."));
    }

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .paymentNumber(payment.getPaymentReference())
                .paymentReference(payment.getPaymentReference())
                .policyId(payment.getPolicy().getId())
                .customerId(payment.getCustomer().getId())
                .policyNumber(payment.getPolicy().getPolicyNumber())
                .amount(payment.getAmount())
                .paymentDate(payment.getPaymentDate())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getStatus())
                .status(payment.getStatus())
                .transactionReference(payment.getTransactionReference())
                .invoiceNumber(payment.getInvoiceNumber())
                .receiptNumber(payment.getReceiptNumber())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}

