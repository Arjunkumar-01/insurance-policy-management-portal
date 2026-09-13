package com.insurance.portal.policy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PurchasePolicyRequest {

    private Long customerId;

    @NotNull
    private Long productId;

    @NotBlank
    @Size(max = 100)
    private String nomineeName;

    @NotBlank
    @Size(max = 60)
    private String nomineeRelation;
}

