package com.asrevo.cvhome.payment.api.v1.payment.model;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

@Data
@Schema(description = "Model for approving a payment transaction")
public class PaymentApprovalRequest {

    @NotBlank
    @Schema(description = "The external transaction reference number from the payment gateway or bank transfer", example = "TXN123456789")
    private String transactionNo;
}
