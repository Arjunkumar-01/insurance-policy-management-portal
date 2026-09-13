package com.insurance.portal.claim.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class SubmitClaimRequest {

    @NotBlank
    private String policyNumber;

    @NotNull
    private LocalDate incidentDate;

    @NotNull
    @DecimalMin(value = "0.01", message = "Claim amount must be greater than zero")
    private BigDecimal claimAmount;

    @NotBlank
    @Size(max = 2000)
    private String description;

    @Size(max = 500)
    @Pattern(regexp = "^(https?://).*$", message = "Supporting document must be a valid http/https URL")
    private String supportingDocuments;
}

