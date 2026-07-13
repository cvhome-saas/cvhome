package com.asrevo.cvhome.payment.service.processor;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.entity.payment.PaymentConfiguration;
import com.asrevo.cvhome.payment.entity.payment.PaymentSecret;
import com.asrevo.cvhome.payment.model.payment.PaymentRequest;
import com.asrevo.cvhome.payment.model.payment.PaymentStatus;
import com.asrevo.cvhome.payment.model.payment.WebhookResult;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class ManualTransferredProcessor implements PaymentProcessor {

    @Override
    public PaymentInitiateResult initiate(String internalReference, PaymentSecret secret, PaymentRequest request) {
        log.info("Processing Manual transferred payment for internal reference: {}", internalReference);
        return PaymentInitiateResult.builder()
                .status(PaymentStatus.PENDING)
                .build();
    }

    @Override
    public WebhookResult handleWebhook(StoreMerchantId storeMerchantId, String payload, Map<String, String> headers,
                                       PaymentConfiguration config) {
        return WebhookResult.noneUseCase();
    }

    @Override
    public PaymentType type() {
        return PaymentType.MANUAL_TRANSFER;
    }
}
