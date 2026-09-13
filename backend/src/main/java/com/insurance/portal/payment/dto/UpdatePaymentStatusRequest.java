package com.insurance.portal.payment.dto;

import com.insurance.portal.common.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePaymentStatusRequest {

    @NotNull
    private PaymentStatus paymentStatus;

    @Size(max = 500, message = "Failure reason cannot exceed 500 characters")
    private String failureReason;
}

