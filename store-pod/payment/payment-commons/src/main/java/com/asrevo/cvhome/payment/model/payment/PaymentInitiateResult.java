package com.asrevo.cvhome.payment.model.payment;

import java.util.Objects;

import lombok.Builder;

@Builder
public record PaymentInitiateResult(Long transactionId, PaymentInitiateStatus status, String redirectUrl, String externalId) {

    public static PaymentInitiateResult failed() {
        return PaymentInitiateResult.builder().status(PaymentInitiateStatus.FAILED).build();
    }

    public static PaymentInitiateResult failed(Long transactionId) {
        return PaymentInitiateResult.builder().transactionId(transactionId).status(PaymentInitiateStatus.FAILED).build();
    }

    public static PaymentInitiateResult pending() {
        return PaymentInitiateResult.builder().status(PaymentInitiateStatus.PENDING).build();
    }

    public boolean shouldRedirect() {
        return !Objects.isNull(redirectUrl);
    }
}
