package com.asrevo.cvhome.payment.model.payment;

import java.util.Objects;

import lombok.Builder;

@Builder
public record PaymentResponse(
        Long transactionId,
        PaymentStatus status,
        String redirectUrl) {

    public static PaymentResponse failed() {
        return PaymentResponse.builder().status(PaymentStatus.FAILED).build();
    }

    public static PaymentResponse pending() {
        return PaymentResponse.builder().status(PaymentStatus.PENDING).build();
    }

    public boolean shouldRedirect() {
        return !Objects.isNull(redirectUrl);
    }
}