package com.asrevo.cvhome.payment.service.processor;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.entity.payment.PaymentSecret;
import com.asrevo.cvhome.payment.errors.InvalidWebhookSignatureException;
import com.asrevo.cvhome.payment.model.payment.PaymentInitiateResult;
import com.asrevo.cvhome.payment.model.payment.PaymentInitiateStatus;
import com.asrevo.cvhome.payment.model.payment.PaymentRequest;
import com.asrevo.cvhome.payment.model.payment.WebhookResult;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class ManualTransferredProcessor implements PaymentProcessor {

    private static final String PROVIDER = "manual_transfer";

    @Override
    public PaymentInitiateResult initiate(String internalReference, PaymentSecret secret, PaymentRequest request) {
        log.info("Processing Manual transferred payment for internal reference: {}", internalReference);
        return PaymentInitiateResult.builder()
                .status(PaymentInitiateStatus.PENDING)
                .build();
    }

    /**
     * Nothing can sign for an offline provider, so no delivery is authentic. Refusing here matters: every store has
     * this type enabled, and a no-op would leave the public webhook writing an outbox row for any store under it.
     */
    @Override
    public void authenticateWebhook(StoreMerchantId storeMerchantId, String payload, Map<String, String> headers,
                                    PaymentSecret config) throws InvalidWebhookSignatureException {
        throw InvalidWebhookSignatureException.verificationFailed(PROVIDER, false, null);
    }

    @Override
    public WebhookResult parseWebhook(StoreMerchantId storeMerchantId, String payload, Map<String, String> headers,
                                      PaymentSecret config) {
        return WebhookResult.noneUseCase();
    }

    @Override
    public PaymentType type() {
        return PaymentType.MANUAL_TRANSFER;
    }
}
