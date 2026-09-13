package com.insurance.portal.payment.controller;

import com.insurance.portal.common.dto.ApiResponse;
import com.insurance.portal.payment.dto.MakePaymentRequest;
import com.insurance.portal.payment.dto.PaymentAnalyticsResponse;
import com.insurance.portal.payment.dto.PaymentReceiptResponse;
import com.insurance.portal.payment.dto.PaymentRevenueResponse;
import com.insurance.portal.payment.dto.PaymentResponse;
import com.insurance.portal.payment.dto.UpdatePaymentStatusRequest;
import com.insurance.portal.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<PaymentResponse>> makePayment(@Valid @RequestBody MakePaymentRequest request) {
        PaymentResponse response = paymentService.makePayment(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<PaymentResponse>builder()
                .success(true).status(HttpStatus.CREATED.value()).message("Payment completed successfully").data(response).build());
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getMyPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<PaymentResponse> response = paymentService.getMyPayments(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "paymentDate"))
        );

        return ResponseEntity.ok(ApiResponse.<Page<PaymentResponse>>builder()
                .success(true).status(HttpStatus.OK.value()).message("My payments fetched successfully").data(response).build());
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("hasAnyRole('CUSTOMER','AGENT','ADMIN','CLAIMS_OFFICER')")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(@PathVariable Long paymentId) {
        PaymentResponse response = paymentService.getPayment(paymentId);
        return ResponseEntity.ok(ApiResponse.<PaymentResponse>builder()
                .success(true).status(HttpStatus.OK.value()).message("Payment fetched successfully").data(response).build());
    }

    @GetMapping("/{paymentId}/receipt")
    @PreAuthorize("hasAnyRole('CUSTOMER','AGENT','ADMIN')")
    public ResponseEntity<ApiResponse<PaymentReceiptResponse>> getReceipt(@PathVariable Long paymentId) {
        PaymentReceiptResponse response = paymentService.generateReceipt(paymentId);
        return ResponseEntity.ok(ApiResponse.<PaymentReceiptResponse>builder()
                .success(true).status(HttpStatus.OK.value()).message("Payment receipt fetched successfully").data(response).build());
    }

    @GetMapping("/policy/{policyId}")
    @PreAuthorize("hasAnyRole('CUSTOMER','AGENT','ADMIN','CLAIMS_OFFICER')")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getPolicyPayments(
            @PathVariable Long policyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<PaymentResponse> response = paymentService.getPolicyPayments(
                policyId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "paymentDate"))
        );
        return ResponseEntity.ok(ApiResponse.<Page<PaymentResponse>>builder()
                .success(true).status(HttpStatus.OK.value()).message("Policy payments fetched successfully").data(response).build());
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('AGENT','ADMIN')")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getCustomerPayments(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<PaymentResponse> response = paymentService.getPaymentsByCustomer(
                customerId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "paymentDate"))
        );
        return ResponseEntity.ok(ApiResponse.<Page<PaymentResponse>>builder()
                .success(true).status(HttpStatus.OK.value()).message("Customer payments fetched successfully").data(response).build());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CLAIMS_OFFICER')")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<PaymentResponse> response = paymentService.getPayments(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "paymentDate"))
        );
        return ResponseEntity.ok(ApiResponse.<Page<PaymentResponse>>builder()
                .success(true).status(HttpStatus.OK.value()).message("Payments fetched successfully").data(response).build());
    }

    @PatchMapping("/{paymentId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentResponse>> updatePaymentStatus(
            @PathVariable Long paymentId,
            @Valid @RequestBody UpdatePaymentStatusRequest request) {

        PaymentResponse response = paymentService.updatePaymentStatus(paymentId, request);
        return ResponseEntity.ok(ApiResponse.<PaymentResponse>builder()
                .success(true).status(HttpStatus.OK.value()).message("Payment status updated successfully").data(response).build());
    }

    @GetMapping("/analytics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentAnalyticsResponse>> getPaymentAnalytics() {
        PaymentAnalyticsResponse response = paymentService.getPaymentAnalytics();
        return ResponseEntity.ok(ApiResponse.<PaymentAnalyticsResponse>builder()
                .success(true).status(HttpStatus.OK.value()).message("Payment analytics fetched successfully").data(response).build());
    }

    @GetMapping("/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentRevenueResponse>> getRevenue() {
        PaymentRevenueResponse response = paymentService.getRevenueSummary();
        return ResponseEntity.ok(ApiResponse.<PaymentRevenueResponse>builder()
                .success(true).status(HttpStatus.OK.value()).message("Payment revenue fetched successfully").data(response).build());
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentHistory() {
        List<PaymentResponse> response = paymentService.getMyPaymentHistory();

        return ResponseEntity.ok(ApiResponse.<List<PaymentResponse>>builder()
                .success(true).status(HttpStatus.OK.value()).message("Payment history fetched successfully").data(response).build());
    }
}

