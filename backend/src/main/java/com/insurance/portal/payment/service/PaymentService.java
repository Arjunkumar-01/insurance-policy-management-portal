package com.insurance.portal.payment.service;

import com.insurance.portal.payment.dto.MakePaymentRequest;
import com.insurance.portal.payment.dto.PaymentAnalyticsResponse;
import com.insurance.portal.payment.dto.PaymentReceiptResponse;
import com.insurance.portal.payment.dto.PaymentRevenueResponse;
import com.insurance.portal.payment.dto.PaymentResponse;
import com.insurance.portal.payment.dto.UpdatePaymentStatusRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PaymentService {

    PaymentResponse makePayment(MakePaymentRequest request);

    PaymentResponse getPayment(Long paymentId);

    Page<PaymentResponse> getMyPayments(Pageable pageable);

    Page<PaymentResponse> getPaymentsByCustomer(Long customerId, Pageable pageable);

    Page<PaymentResponse> getPolicyPayments(Long policyId, Pageable pageable);

    Page<PaymentResponse> getPayments(Pageable pageable);

    PaymentReceiptResponse generateReceipt(Long paymentId);

    PaymentResponse updatePaymentStatus(Long paymentId, UpdatePaymentStatusRequest request);

    PaymentRevenueResponse getRevenueSummary();

    PaymentAnalyticsResponse getPaymentAnalytics();

    List<PaymentResponse> getMyPaymentHistory();
}

