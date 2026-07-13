package com.asrevo.cvhome.payment.service.processor;

import java.util.Map;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.entity.payment.PaymentConfiguration;
import com.asrevo.cvhome.payment.entity.payment.PaymentSecret;
import com.asrevo.cvhome.payment.model.payment.PaymentRequest;
import com.asrevo.cvhome.payment.model.payment.WebhookResult;
import com.asrevo.cvhome.payment.service.processor.exception.InvalidPaymentReferenceId;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

public interface PaymentProcessor {
    PaymentInitiateResult initiate(String internalReference, PaymentSecret secret, PaymentRequest request);

    WebhookResult handleWebhook(StoreMerchantId storeMerchantId, String payload, Map<String, String> headers, PaymentConfiguration config)
            throws InvalidPaymentReferenceId;

    PaymentType type();
}
