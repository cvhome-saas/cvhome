package com.asrevo.cvhome.payment.service.processor;

import java.util.Map;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.entity.payment.PaymentSecret;
import com.asrevo.cvhome.payment.errors.InvalidWebhookSignatureException;
import com.asrevo.cvhome.payment.errors.PaymentInitiateRejectedException;
import com.asrevo.cvhome.payment.errors.PaymentProviderUnavailableException;
import com.asrevo.cvhome.payment.errors.UnexpectedWebhookObjectException;
import com.asrevo.cvhome.payment.errors.UnreadableWebhookPayloadException;
import com.asrevo.cvhome.payment.model.payment.PaymentInitiateResult;
import com.asrevo.cvhome.payment.model.payment.PaymentRequest;
import com.asrevo.cvhome.payment.model.payment.WebhookResult;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

/**
 * A payment provider integration.
 *
 * <p>
 * Each operation names the failures it can actually produce, so a caller can decide per condition — a bad signature is
 * discarded, a provider outage is retried — without inspecting an error code at runtime. The previous signature
 * declared {@code InvalidPaymentReferenceId}, an unchecked type that was never thrown, and
 * {@code SignatureVerificationException}, which the Stripe implementation catches rather than propagates; neither told
 * a caller anything true.
 * </p>
 *
 * <p>
 * A new provider whose failures do not fit these types should widen this contract with a named exception of its own
 * rather than fall back on a generic supertype — the compiler then points at every caller that has to decide what the
 * new condition means.
 * </p>
 */
public interface PaymentProcessor {

    /**
     * Starts a payment with the provider.
     *
     * @throws PaymentInitiateRejectedException    the provider refused the payment; a decision, and a final one
     * @throws PaymentProviderUnavailableException the provider could not be reached or failed without deciding
     *                                             anything; the payment's fate is unknown
     */
    PaymentInitiateResult initiate(String internalReference, PaymentSecret secret, PaymentRequest request)
            throws PaymentInitiateRejectedException, PaymentProviderUnavailableException;

    /**
     * Verifies and decodes an incoming webhook. Every declared failure is permanent for the payload in hand — none of
     * them can succeed on a redelivery of the same body.
     *
     * @throws InvalidWebhookSignatureException  the payload is not authentic
     * @throws UnreadableWebhookPayloadException the payload is authentic but cannot be deserialized
     * @throws UnexpectedWebhookObjectException  the event carried a data object of an unexpected type
     */
    WebhookResult parseWebhook(StoreMerchantId storeMerchantId, String payload, Map<String, String> headers,
                               PaymentSecret config)
            throws InvalidWebhookSignatureException, UnreadableWebhookPayloadException,
            UnexpectedWebhookObjectException;

    PaymentType type();

}
