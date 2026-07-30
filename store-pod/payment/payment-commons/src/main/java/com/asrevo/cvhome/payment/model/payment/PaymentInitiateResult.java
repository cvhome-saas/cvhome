package com.asrevo.cvhome.payment.model.payment;

import java.util.Objects;

import lombok.Builder;

@Builder
public record PaymentInitiateResult(Long transactionId, PaymentInitiateStatus status, String redirectUrl, String externalId,String gatewayRef) {

    public static PaymentInitiateResult failed() {
        return PaymentInitiateResult.builder().status(PaymentInitiateStatus.FAILED).build();
    }

    public static PaymentInitiateResult failed(String internalRef) {
        return PaymentInitiateResult.builder().gatewayRef(internalRef).status(PaymentInitiateStatus.FAILED).build();
    }

    public static PaymentInitiateResult pending() {
        return PaymentInitiateResult.builder().status(PaymentInitiateStatus.PENDING).build();
    }

    public boolean shouldRedirect() {
        return !Objects.isNull(redirectUrl);
    }
}
