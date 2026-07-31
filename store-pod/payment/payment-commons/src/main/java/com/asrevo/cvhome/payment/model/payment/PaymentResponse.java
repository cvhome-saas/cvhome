package com.asrevo.cvhome.payment.model.payment;

import java.util.Objects;

import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;

import lombok.Builder;

@Builder
public record PaymentResponse(
        String gatewayRef,
        PaymentStatus status,
        String redirectUrl) {

    public static PaymentResponse failed() {
        return PaymentResponse.builder().status(PaymentStatus.FAILED).build();
    }

    public static PaymentResponse failed(String gatewayRef) {
        return PaymentResponse.builder().gatewayRef(gatewayRef).status(PaymentStatus.FAILED).build();
    }

    public static PaymentResponse pending() {
        return PaymentResponse.builder().status(PaymentStatus.PENDING).build();
    }

    public boolean shouldRedirect() {
        return !Objects.isNull(redirectUrl);
    }
}