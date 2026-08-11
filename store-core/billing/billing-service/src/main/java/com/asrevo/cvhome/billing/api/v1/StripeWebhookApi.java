package com.asrevo.cvhome.billing.api.v1;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.billing.commons.errors.InvalidWebhookSignatureException;
import com.asrevo.cvhome.billing.service.stripe.StripeWebhookVerifier;
import com.asrevo.cvhome.billing.service.stripe.WebhookIngestService;
import com.stripe.model.Event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Where Stripe delivers events.
 *
 * <p>
 * Public by path, and it has to be: Stripe holds no credential of ours and cannot log in. What authenticates a
 * request here is the signature over the raw body, which is why the body is taken as a {@code String} — deserialising
 * it first would change the bytes the signature was computed over and every payload would fail to verify.
 * </p>
 *
 * <p>
 * There is no {@code @PreAuthorize} and no {@code store} parameter, which makes this the one billing endpoint that
 * breaks the store-scoped convention. That is a consequence of who the caller is, not an oversight.
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/stripe-webhook")
@RequiredArgsConstructor
public class StripeWebhookApi {

    private final StripeWebhookVerifier verifier;

    private final WebhookIngestService ingestService;

    /**
     * Accepts an event.
     *
     * <p>
     * The status codes here are a contract with Stripe's retry machinery, and each is chosen rather than defaulted:
     * </p>
     *
     * <ul>
     * <li><b>200</b> — accepted, or recognised and deliberately ignored. Stripe stops.</li>
     * <li><b>400</b> on a bad signature — the payload will never verify, so retrying it forever helps nobody. This is
     * also why the failure is caught here instead of reaching the problem-detail advice, which would answer 4xx with
     * a body describing an error Stripe cannot act on.</li>
     * <li><b>5xx</b> on anything else, by letting it propagate. A database that was down when the event arrived is
     * exactly the case where Stripe redelivering is what saves the payment.</li>
     * </ul>
     */
    @PostMapping("public/events")
    public ResponseEntity<Void> events(@RequestBody String payload,
                                       @RequestHeader Map<String, String> headers) {
        try {
            Event event = verifier.verify(payload, headers);
            ingestService.ingest(event);
            return ResponseEntity.ok().build();
        } catch (InvalidWebhookSignatureException e) {
            log.warn("Rejected a Stripe webhook whose signature did not verify");
            return ResponseEntity.badRequest().build();
        }
    }

}
