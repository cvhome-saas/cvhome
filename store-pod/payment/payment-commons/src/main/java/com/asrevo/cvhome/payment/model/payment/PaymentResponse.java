package com.asrevo.cvhome.payment.model.payment;

import lombok.Builder;

@Builder
public record PaymentResponse(
        PaymentStatus status,
        String redirectUrl,
        boolean isRedirect,
        String externalId) {

    public static PaymentResponse failed() {
        return PaymentResponse.builder().status(PaymentStatus.FAILED).build();
    }

    public static PaymentResponse pending() {
        return PaymentResponse.builder().status(PaymentStatus.PENDING).build();
    }
}