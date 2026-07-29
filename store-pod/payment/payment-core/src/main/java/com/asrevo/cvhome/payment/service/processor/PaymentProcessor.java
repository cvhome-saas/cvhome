package com.asrevo.cvhome.payment.service.processor;

import java.util.Map;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.entity.payment.PaymentConfiguration;
import com.asrevo.cvhome.payment.entity.payment.PaymentSecret;
import com.asrevo.cvhome.payment.model.payment.PaymentInitiateResult;
import com.asrevo.cvhome.payment.model.payment.PaymentRequest;
import com.asrevo.cvhome.payment.model.payment.WebhookResult;
import com.asrevo.cvhome.payment.service.processor.exception.InvalidPaymentReferenceId;
import com.stripe.exception.SignatureVerificationException;

public interface PaymentProcessor {
    PaymentInitiateResult initiate(PaymentSecret secret, PaymentRequest request,
                                   Long transactionId);

    WebhookResult parseWebhook(StoreMerchantId storeMerchantId, String payload, Map<String, String> headers, PaymentConfiguration config)
            throws InvalidPaymentReferenceId, SignatureVerificationException;

}
