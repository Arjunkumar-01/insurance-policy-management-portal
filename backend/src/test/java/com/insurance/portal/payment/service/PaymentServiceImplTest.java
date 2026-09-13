package com.insurance.portal.payment.service;

import com.insurance.portal.common.enums.PaymentMethod;
import com.insurance.portal.common.enums.PaymentStatus;
import com.insurance.portal.common.enums.PolicyStatus;
import com.insurance.portal.common.enums.Role;
import com.insurance.portal.customer.entity.Customer;
import com.insurance.portal.customer.repository.CustomerRepository;
import com.insurance.portal.exception.BadRequestException;
import com.insurance.portal.payment.dto.UpdatePaymentStatusRequest;
import com.insurance.portal.payment.entity.Payment;
import com.insurance.portal.payment.repository.PaymentRepository;
import com.insurance.portal.policy.entity.Policy;
import com.insurance.portal.policy.repository.PolicyRepository;
import com.insurance.portal.product.entity.Product;
import com.insurance.portal.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    @SuppressWarnings("unused")
    private PolicyRepository policyRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Customer admin;

    @BeforeEach
    void setUp() {
        admin = new Customer();
        admin.setId(1L);
        admin.setUsername("admin_user");
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);

        Mockito.when(customerRepository.findByUsername("admin_user")).thenReturn(Optional.of(admin));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new CustomUserDetails(admin), null)
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void successfulToPendingTransitionIsBlocked() {
        Payment payment = buildPayment(PaymentStatus.SUCCESSFUL);
        Mockito.when(paymentRepository.findById(10L)).thenReturn(Optional.of(payment));

        UpdatePaymentStatusRequest request = new UpdatePaymentStatusRequest();
        request.setPaymentStatus(PaymentStatus.PENDING);

        BadRequestException ex = Assertions.assertThrows(BadRequestException.class,
                () -> paymentService.updatePaymentStatus(10L, request));

        Assertions.assertTrue(ex.getMessage().contains("Invalid payment status transition"));
    }

    @Test
    void failedToProcessingTransitionIsBlocked() {
        Payment payment = buildPayment(PaymentStatus.FAILED);
        Mockito.when(paymentRepository.findById(11L)).thenReturn(Optional.of(payment));

        UpdatePaymentStatusRequest request = new UpdatePaymentStatusRequest();
        request.setPaymentStatus(PaymentStatus.PROCESSING);

        BadRequestException ex = Assertions.assertThrows(BadRequestException.class,
                () -> paymentService.updatePaymentStatus(11L, request));

        Assertions.assertTrue(ex.getMessage().contains("Invalid payment status transition"));
    }

    @Test
    void pendingToProcessingToSuccessfulIsAllowed() {
        Payment payment = buildPayment(PaymentStatus.PENDING);
        Mockito.when(paymentRepository.findById(12L)).thenReturn(Optional.of(payment));
        Mockito.when(paymentRepository.save(ArgumentMatchers.any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UpdatePaymentStatusRequest toProcessing = new UpdatePaymentStatusRequest();
        toProcessing.setPaymentStatus(PaymentStatus.PROCESSING);
        paymentService.updatePaymentStatus(12L, toProcessing);
        Assertions.assertEquals(PaymentStatus.PROCESSING, payment.getStatus());

        UpdatePaymentStatusRequest toSuccessful = new UpdatePaymentStatusRequest();
        toSuccessful.setPaymentStatus(PaymentStatus.SUCCESSFUL);
        paymentService.updatePaymentStatus(12L, toSuccessful);
        Assertions.assertEquals(PaymentStatus.SUCCESSFUL, payment.getStatus());
    }

    private Payment buildPayment(PaymentStatus status) {
        Customer customer = new Customer();
        customer.setId(2L);
        customer.setUsername("john_customer");
        customer.setRole(Role.CUSTOMER);
        customer.setEnabled(true);

        Product product = new Product();
        product.setId(3L);
        product.setProductName("Travel");

        Policy policy = new Policy();
        policy.setId(4L);
        policy.setPolicyNumber("POL-12345678");
        policy.setCustomer(customer);
        policy.setProduct(product);
        policy.setStartDate(LocalDate.now().minusDays(5));
        policy.setEndDate(LocalDate.now().plusDays(30));
        policy.setStatus(PolicyStatus.ACTIVE);

        Payment payment = new Payment();
        payment.setId(99L);
        payment.setPaymentReference("PAY-12345678");
        payment.setPolicy(policy);
        payment.setCustomer(customer);
        payment.setAmount(BigDecimal.valueOf(500));
        payment.setPaymentDate(LocalDateTime.now().minusDays(1));
        payment.setPaymentMethod(PaymentMethod.UPI);
        payment.setStatus(status);
        payment.setTransactionReference("TXN-12345678");
        payment.setInvoiceNumber("INV-12345678");
        payment.setReceiptNumber("RCT-12345678");
        if (status == PaymentStatus.FAILED) {
            payment.setFailureReason("gateway timeout");
        }
        return payment;
    }
}

