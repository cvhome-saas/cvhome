package com.asrevo.cvhome.billing.service.stripe;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.billing.commons.errors.InvalidWebhookSignatureException;
import com.asrevo.cvhome.billing.config.StripeCredentials;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Establishes that a webhook payload really came from Stripe.
 *
 * <p>
 * This is the only authentication the webhook endpoint has. It is a public URL holding no credential of Stripe's, so
 * anyone can post to it; what makes a payload trustworthy is that it verifies against the signing secret. Nothing in
 * the body may be read before this passes — the store id, the amounts and the event type are all attacker-controlled
 * until then.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StripeWebhookVerifier {

    private static final String SIGNATURE_HEADER = "stripe-signature";

    private static final String SIGNATURE_HEADER_TITLE_CASE = "Stripe-Signature";

    private static final String STRIPE = "stripe";

    private final StripeCredentials credentials;

    /**
     * @throws InvalidWebhookSignatureException the payload did not verify, so it is not from Stripe
     */
    public Event verify(String payload, Map<String, String> headers) throws InvalidWebhookSignatureException {
        String signature = headers.get(SIGNATURE_HEADER);
        if (signature == null) {
            signature = headers.get(SIGNATURE_HEADER_TITLE_CASE);
        }
        if (signature == null || signature.isBlank()) {
            // Refused here rather than handed to the SDK. Webhook.constructEvent dereferences the header before it
            // validates it — Signature.getTimestamp calls sigHeader.split — so a request with no signature threw a
            // NullPointerException, which on a public, unauthenticated endpoint became a 500 anyone could produce at
            // will. A missing signature is the plainest possible "this did not come from Stripe", and 400 is the
            // answer that says so.
            throw InvalidWebhookSignatureException.verificationFailed(STRIPE, false, null);
        }
        if (credentials.webhookSigningKey() == null || credentials.webhookSigningKey().isBlank()) {
            // Nothing can be verified without a secret, so nothing may be trusted. Refusing here rather than letting
            // the SDK fail on a null turns a 500 — which Stripe would retry for days — into a clear 400 and one log
            // line naming the actual problem, which is configuration rather than the payload.
            log.error("No Stripe webhook signing key is configured; every webhook will be refused until one is set");
            throw InvalidWebhookSignatureException.verificationFailed(STRIPE, true, null);
        }
        try {
            return Webhook.constructEvent(payload, signature, credentials.webhookSigningKey());
        } catch (SignatureVerificationException e) {
            // Not logged here: the caller logs it once with context, and a failed signature is an ordinary condition
            // on a public endpoint rather than an incident deserving a stack trace at every layer.
            throw InvalidWebhookSignatureException.verificationFailed(STRIPE, true, e);
        } catch (RuntimeException e) {
            // constructEvent deserialises the payload *before* it verifies the signature, so a body that is not JSON
            // at all comes back as an unchecked JsonSyntaxException rather than as a verification failure. Same
            // conclusion either way — this is not a payload Stripe signed — and the same 400, instead of the 500 an
            // unauthenticated caller could otherwise produce with three characters of garbage.
            throw InvalidWebhookSignatureException.verificationFailed(STRIPE, true, e);
        }
    }

}
