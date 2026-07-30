package com.asrevo.cvhome.payment.service.processor;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.entity.payment.PaymentConfiguration;
import com.asrevo.cvhome.payment.entity.payment.PaymentSecret;
import com.asrevo.cvhome.payment.model.payment.PaymentInitiateResult;
import com.asrevo.cvhome.payment.model.payment.PaymentRequest;
import com.asrevo.cvhome.payment.model.payment.WebhookResult;
import com.asrevo.cvhome.payment.service.processor.exception.InvalidPaymentReferenceId;
import com.stripe.exception.SignatureVerificationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class CODProcessor implements PaymentProcessor {
    @Override
    public PaymentInitiateResult initiate(PaymentSecret secret, PaymentRequest request, Long transactionId) {
        return null;
    }

    @Override
    public WebhookResult parseWebhook(StoreMerchantId storeMerchantId, String payload, Map<String, String> headers,
                                      PaymentSecret config) throws InvalidPaymentReferenceId {
        return null;
    }
}
