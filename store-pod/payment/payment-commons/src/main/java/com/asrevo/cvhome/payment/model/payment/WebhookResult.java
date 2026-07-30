package com.asrevo.cvhome.payment.model.payment;

import lombok.Builder;

@Builder
public record WebhookResult(String internalReference, PaymentUseCase paymentUseCase) {

    public static WebhookResult noneUseCase() {
        return WebhookResult.builder()
                .paymentUseCase(PaymentUseCase.NONE)
                .build();
    }
}

