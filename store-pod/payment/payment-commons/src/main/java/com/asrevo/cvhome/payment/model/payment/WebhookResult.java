package com.asrevo.cvhome.payment.model.payment;

import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;

import lombok.Builder;

@Builder
public record WebhookResult(String internalReference, PaymentStatus status, PaymentUseCase paymentUseCase) {

    public static WebhookResult noneUseCase() {
        return WebhookResult.builder()
                .paymentUseCase(PaymentUseCase.NONE)
                .build();
    }
}

