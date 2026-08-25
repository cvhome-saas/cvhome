package com.asrevo.cvhome.billing.service.stripe;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.billing.commons.errors.InvalidWebhookSignatureException;
import com.asrevo.cvhome.billing.config.StripeCredentials;
import com.stripe.model.Event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The only authentication the webhook endpoint has.
 *
 * <p>
 * Every case here is driven by a signature computed in the test from the same secret Stripe would have used, so what
 * is being checked is that a payload verifies — not that some collaborator was called. The refusals matter more than
 * the acceptance: this endpoint is public, holds no credential, and anyone at all can POST to it.
 * </p>
 */
class StripeWebhookVerifierTest {

    private static final String SECRET = "whsec_test_9c4a1f2b8d7e6a5c3b0f9e8d7c6b5a49";

    private static final String PAYLOAD = """
            {"id":"evt_test_1","object":"event","api_version":"2025-03-31.basil",
             "type":"invoice.payment_succeeded",
             "data":{"object":{"id":"in_1","object":"invoice","amount_paid":1000}}}""";

    private static StripeWebhookVerifier verifierWith(String signingKey) {
        StripeCredentials credentials = mock(StripeCredentials.class);
        when(credentials.webhookSigningKey()).thenReturn(signingKey);
        return new StripeWebhookVerifier(credentials);
    }

    private static Map<String, String> headers(String signature) {
        Map<String, String> headers = new HashMap<>();
        if (signature != null) {
            headers.put("stripe-signature", signature);
        }
        return headers;
    }

    @Test
    @DisplayName("a payload signed with the configured secret verifies and comes back as the event")
    void acceptsARealSignature() throws Exception {
        StripeWebhookVerifier verifier = verifierWith(SECRET);

        Event event = verifier.verify(PAYLOAD, headers(StripeSignatures.sign(PAYLOAD, SECRET)));

        assertThat(event.getId()).isEqualTo("evt_test_1");
        assertThat(event.getType()).isEqualTo("invoice.payment_succeeded");
        // The raw JSON has to survive: every handler downstream reads the document rather than the SDK's model,
        // because Stripe has moved fields between API versions.
        assertThat(event.getDataObjectDeserializer().getRawJson()).contains("in_1");
    }

    @Test
    @DisplayName("the header may arrive title-cased, as a proxy may rewrite it")
    void acceptsTheTitleCasedHeader() throws Exception {
        StripeWebhookVerifier verifier = verifierWith(SECRET);
        Map<String, String> headers = new HashMap<>();
        headers.put("Stripe-Signature", StripeSignatures.sign(PAYLOAD, SECRET));

        assertThat(verifier.verify(PAYLOAD, headers)).isNotNull();
    }

    @Test
    @DisplayName("a request with no signature header is refused, not a 500")
    void refusesAMissingSignature() {
        StripeWebhookVerifier verifier = verifierWith(SECRET);

        // The regression this guards. Webhook.constructEvent dereferences the header before validating it —
        // Signature.getTimestamp calls sigHeader.split — so a missing header threw NullPointerException, and on a
        // public unauthenticated endpoint that became a 500 anyone could produce on demand. Without the guard in
        // StripeWebhookVerifier this line fails with NPE rather than the checked refusal.
        assertThatThrownBy(() -> verifier.verify(PAYLOAD, headers(null)))
                .isInstanceOf(InvalidWebhookSignatureException.class)
                .hasNoCause();
    }

    @Test
    @DisplayName("a blank signature header is refused the same way")
    void refusesABlankSignature() {
        StripeWebhookVerifier verifier = verifierWith(SECRET);

        assertThatThrownBy(() -> verifier.verify(PAYLOAD, headers("   ")))
                .isInstanceOf(InvalidWebhookSignatureException.class);
    }

    @Test
    @DisplayName("a body that is not JSON at all is refused, not a 500")
    void refusesAMalformedBody() {
        StripeWebhookVerifier verifier = verifierWith(SECRET);
        String garbage = "{{{not json";

        // Same class of defect as the missing header: constructEvent deserialises before it verifies, so an
        // unsigned body of garbage came back as an unchecked JsonSyntaxException and a 500.
        assertThatThrownBy(() -> verifier.verify(garbage, headers(StripeSignatures.sign(garbage, SECRET))))
                .isInstanceOf(InvalidWebhookSignatureException.class);
    }

    @Test
    @DisplayName("a signature computed with another secret is refused")
    void refusesAForeignSignature() {
        StripeWebhookVerifier verifier = verifierWith(SECRET);

        assertThatThrownBy(() -> verifier.verify(PAYLOAD, headers(StripeSignatures.sign(PAYLOAD, "whsec_someone_else"))))
                .isInstanceOf(InvalidWebhookSignatureException.class);
    }

    @Test
    @DisplayName("a valid signature over a different payload is refused")
    void refusesATamperedPayload() {
        StripeWebhookVerifier verifier = verifierWith(SECRET);
        String signature = StripeSignatures.sign(PAYLOAD, SECRET);
        String tampered = PAYLOAD.replace("1000", "999999");

        // The signature covers the bytes. This is the case that matters: an attacker replaying a real event with
        // the amounts changed.
        assertThatThrownBy(() -> verifier.verify(tampered, headers(signature)))
                .isInstanceOf(InvalidWebhookSignatureException.class);
    }

    @Test
    @DisplayName("a signature older than Stripe's tolerance is refused")
    void refusesAnExpiredSignature() {
        StripeWebhookVerifier verifier = verifierWith(SECRET);
        String stale = StripeSignatures.sign(PAYLOAD, SECRET, Instant.now().minus(2, ChronoUnit.HOURS));

        // Replay protection: without the timestamp check a captured request could be re-posted indefinitely.
        assertThatThrownBy(() -> verifier.verify(PAYLOAD, headers(stale)))
                .isInstanceOf(InvalidWebhookSignatureException.class);
    }

    @Test
    @DisplayName("a header with no v1 signature in it is refused")
    void refusesAHeaderWithoutASignature() {
        StripeWebhookVerifier verifier = verifierWith(SECRET);

        assertThatThrownBy(() -> verifier.verify(PAYLOAD, headers("t=" + Instant.now().getEpochSecond())))
                .isInstanceOf(InvalidWebhookSignatureException.class);
    }

    @Test
    @DisplayName("with no signing key configured every payload is refused, however well signed")
    void refusesEverythingWithNoKeyConfigured() {
        StripeWebhookVerifier verifier = verifierWith(null);

        // Nothing can be verified without a secret, so nothing may be trusted. A misconfigured service must refuse
        // rather than let the SDK fail on a null and answer 500 — which Stripe would retry for days.
        assertThatThrownBy(() -> verifier.verify(PAYLOAD, headers(StripeSignatures.sign(PAYLOAD, SECRET))))
                .isInstanceOf(InvalidWebhookSignatureException.class);
    }

    @Test
    @DisplayName("a blank signing key is treated as no key at all")
    void refusesEverythingWithABlankKey() {
        StripeWebhookVerifier verifier = verifierWith("  ");

        assertThatThrownBy(() -> verifier.verify(PAYLOAD, headers(StripeSignatures.sign(PAYLOAD, SECRET))))
                .isInstanceOf(InvalidWebhookSignatureException.class);
    }

}
