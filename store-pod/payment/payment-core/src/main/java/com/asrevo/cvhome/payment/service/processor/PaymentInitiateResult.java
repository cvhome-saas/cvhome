package com.asrevo.cvhome.payment.service.processor;

import lombok.Builder;

@Builder
public record PaymentInitiateResult(String redirectUrl, String externalId) {
}
