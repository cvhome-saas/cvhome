package com.asrevo.cvhome.billing.api.v1;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import com.asrevo.cvhome.billing.api.BillingApiSupport;
import com.asrevo.cvhome.billing.api.BillingFixtures;
import com.asrevo.cvhome.billing.commons.StripeEventId;
import com.asrevo.cvhome.billing.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.billing.repository.ProcessedStripeEventRepository;
import com.asrevo.cvhome.billing.service.stripe.StripeSignatures;
import com.asrevo.cvhome.testsupport.annotations.ServiceIntegrationTest;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;

import static com.asrevo.cvhome.billing.api.BillingApiSupport.V1;
import static com.asrevo.cvhome.billing.api.BillingApiSupport.expect;
import static com.asrevo.cvhome.billing.api.BillingApiSupport.path;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Where Stripe delivers events, driven over real HTTP with payloads this test signs itself.
 *
 * <p>
 * A public URL that holds no credential of Stripe's, so the signature over the raw body is the only thing that makes
 * a payload trustworthy. The status codes are a contract with Stripe's retry machinery: 200 for accepted or
 * deliberately ignored, 400 for a payload that will never verify — retrying that forever helps nobody — and 5xx only
 * for something a redelivery could fix.
 * </p>
 *
 * <p>
 * Its own {@link RestClient} rather than the shared {@code ApiClient}: this is the one endpoint whose request is not
 * shaped like the rest of the platform's — no bearer token, no {@code ?store=}, and a header that has to be set
 * exactly.
 * </p>
 */
@ServiceIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class StripeWebhookApiIntegrationTest {

    /** Must match {@code com.asrevo.cvhome.stripe.webhook-signing-key} in {@code application-test-stores.yml}. */
    private static final String SIGNING_KEY = "whsec_integration_test_key";

    private static final String EVENTS = path(V1, "stripe-webhook", "public", "events");

    private static final String SIGNATURE_HEADER = "Stripe-Signature";

    private static final String STORE = BillingApiSupport.WEBHOOK_STORE;

    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    @Autowired
    private BillingFixtures fixtures;

    @Autowired
    private ProcessedStripeEventRepository processedEvents;

    private RestClient client;

    @BeforeEach
    void setUp() {
        client = RestClient.builder()
                .baseUrl(String.format("http://localhost:%d", port))
                .defaultStatusHandler(status -> true, (request, response) -> { })
                .build();
        fixtures.pending(STORE);
    }

    /** A subscription event for the webhook store, unique per call so no two tests share an event id. */
    private static String subscriptionEvent(String eventId) {
        return String.format("""
                {"id":"%s","object":"event","api_version":"2025-03-31.basil","created":%d,
                 "type":"customer.subscription.updated",
                 "data":{"object":{"id":"sub_%s","object":"subscription","customer":"cus_webhook_it",
                   "status":"active","cancel_at_period_end":false,
                   "metadata":{"storeId":"%s"}}}}""",
                eventId, Instant.now().getEpochSecond(), eventId, STORE);
    }

    private ResponseEntity<String> deliver(String payload, String signature) {
        RestClient.RequestBodySpec spec = client.post().uri(EVENTS).contentType(MediaType.APPLICATION_JSON);
        if (signature != null) {
            spec = spec.header(SIGNATURE_HEADER, signature);
        }
        return spec.body(payload).retrieve().toEntity(String.class);
    }

    private static String eventId(String suffix) {
        return "evt_it_" + suffix + "_" + java.util.UUID.randomUUID().toString().substring(0, 8);
    }

    // ------------------------------------------------------------------------------------------- accepted

    @Test
    @DisplayName("a payload signed with the configured secret is accepted, without a token")
    void acceptsASignedEvent() {
        String id = eventId("accepted");
        String payload = subscriptionEvent(id);

        expect(deliver(payload, StripeSignatures.sign(payload, SIGNING_KEY)), HttpStatus.OK);

        // Recorded exactly once, which is what makes a redelivery cheap.
        assertThat(processedEvents.findById(new StripeEventId(id))).isPresent();
    }

    @Test
    @DisplayName("a redelivery of the same event is accepted again and recorded once")
    void aRedeliveryIsAcceptedAndDropped() {
        String id = eventId("redelivered");
        String payload = subscriptionEvent(id);
        String signature = StripeSignatures.sign(payload, SIGNING_KEY);

        expect(deliver(payload, signature), HttpStatus.OK);
        // 200 again on purpose: a non-2xx would have Stripe retry an event that has already been handled.
        expect(deliver(payload, signature), HttpStatus.OK);

        assertThat(processedEvents.findById(new StripeEventId(id))).isPresent();
    }

    @Test
    @DisplayName("an event type billing does not act on is accepted and recorded as ignored")
    void anUnhandledTypeIsAccepted() {
        String id = eventId("ignored");
        String payload = String.format("""
                {"id":"%s","object":"event","api_version":"2025-03-31.basil","created":%d,
                 "type":"customer.updated",
                 "data":{"object":{"id":"cus_1","object":"customer"}}}""",
                id, Instant.now().getEpochSecond());

        // Stripe sends dozens of types. Answering anything but 200 to one we simply do not act on would have it
        // redeliver for days.
        expect(deliver(payload, StripeSignatures.sign(payload, SIGNING_KEY)), HttpStatus.OK);
        assertThat(processedEvents.findById(new StripeEventId(id))).isPresent();
    }

    // -------------------------------------------------------------------------------------------- refused

    @Test
    @DisplayName("a request with no signature header is refused with 400, not a 500")
    void refusesAnUnsignedRequest() {
        String payload = subscriptionEvent(eventId("unsigned"));

        // The regression: Webhook.constructEvent dereferences the header before validating it, so this used to be a
        // NullPointerException and a 500 — on a public endpoint, producible on demand by anyone with the URL, and
        // 5xx is exactly what tells Stripe to redeliver.
        expect(deliver(payload, null), HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("a body that is not JSON is refused with 400 rather than throwing")
    void refusesAMalformedBody() {
        String garbage = "{{{not json";

        // Same defect, other half: constructEvent deserialises before it verifies, so garbage came back as an
        // unchecked JsonSyntaxException and a 500.
        expect(deliver(garbage, StripeSignatures.sign(garbage, SIGNING_KEY)), HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("a signature from another secret is refused and nothing is recorded")
    void refusesAForeignSignature() {
        String id = eventId("foreign");
        String payload = subscriptionEvent(id);

        expect(deliver(payload, StripeSignatures.sign(payload, "whsec_not_ours")), HttpStatus.BAD_REQUEST);
        // Nothing in the body may be read before the signature passes: the store id, the amounts and the event type
        // are all attacker-controlled until then.
        assertThat(processedEvents.findById(new StripeEventId(id))).isEmpty();
    }

    @Test
    @DisplayName("a valid signature over a different body is refused")
    void refusesATamperedPayload() {
        String id = eventId("tampered");
        String payload = subscriptionEvent(id);
        String signature = StripeSignatures.sign(payload, SIGNING_KEY);

        expect(deliver(payload.replace("\"status\":\"active\"", "\"status\":\"canceled\""), signature),
                HttpStatus.BAD_REQUEST);
        assertThat(processedEvents.findById(new StripeEventId(id))).isEmpty();
    }

    @Test
    @DisplayName("a signature older than Stripe's tolerance is refused")
    void refusesAnExpiredSignature() {
        String payload = subscriptionEvent(eventId("stale"));

        expect(deliver(payload, StripeSignatures.sign(payload, SIGNING_KEY,
                Instant.now().minus(2L, ChronoUnit.HOURS))), HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("the endpoint is reachable with no Authorization header at all")
    void needsNoCredential() {
        String payload = subscriptionEvent(eventId("anonymous"));

        // Stripe holds no credential of ours and cannot log in. This is the one billing endpoint that breaks the
        // store-scoped convention, and that is a consequence of who the caller is rather than an oversight.
        ResponseEntity<String> response = client.post().uri(EVENTS)
                .contentType(MediaType.APPLICATION_JSON)
                .header(SIGNATURE_HEADER, StripeSignatures.sign(payload, SIGNING_KEY))
                .body(payload)
                .retrieve()
                .toEntity(String.class);

        expect(response, HttpStatus.OK);
        assertThat(response.getHeaders().getFirst(HttpHeaders.WWW_AUTHENTICATE)).isNull();
    }

}
