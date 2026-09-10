package com.asrevo.cvhome.payment.api.v1.payment;

import java.time.Duration;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import com.asrevo.cvhome.checkout.services.order.ExternalOrderSignalService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;
import com.asrevo.cvhome.testsupport.annotations.StorageIntegrationTest;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;
import com.asrevo.cvhome.testsupport.security.Tokens;

import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.COD;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.GATEWAY_REF;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.INITIATE;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.ORDER;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.PAID;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.PAYMENTS;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.PAYPAL;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.PENDING;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.PRIVATE_CONFIG;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.SESSION_COMPLETED_EVENT;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.STATUS;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.STATUS_SEGMENT;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.STRIPE;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.WEBHOOK;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.configBody;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.expect;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.json;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.path;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.paymentRequestBody;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.scoped;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.slug;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.stripeSignature;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

/**
 * The public webhook endpoint end to end: signature verification against the store's own stored secret, the outbox
 * hop, and the settlement it produces.
 *
 * <p>
 * Stripe is never contacted. The store's webhook secret is written through the configuration API first, and the
 * payload is signed here with the same HMAC scheme Stripe uses, so the verification the processor performs is the
 * real one rather than a stub. A transaction to settle is staged through an offline processor for the same reason:
 * nothing in this class may reach the provider.
 * </p>
 *
 * <p>
 * The delivery is authenticated before it is scheduled: a forged or unsigned payload is a 400 and leaves no outbox
 * row, an unknown store or an unconfigured type a 404. The outbox handler verifies again on its own, so a wrongly
 * signed body is also asserted by what does <em>not</em> happen to the transaction — the two checks are independent
 * and both must hold.
 * </p>
 */
@StorageIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
@TestPropertySource(properties = PaymentApiTestSupport.POD_PROPERTY)
class PaymentWebhookApiIntegrationTest {

    /** The store this class owns; its Stripe webhook secret is rewritten below. */
    private static final String STORE = Tokens.STORE_4;

    private static final String WEBHOOK_SECRET_VALUE = "whsec_integration_test";

    private static final String AMOUNT = "31.00";

    private static final Duration SETTLEMENT_TIMEOUT = Duration.ofSeconds(45);

    private static final Duration QUIET_PERIOD = Duration.ofSeconds(8);

    /** Webhook rows on payment's outbox whose body carries the given reference, whatever their status. */
    private static final String WEBHOOK_ROWS = """
            select count(*) from payment.outbox_record where record_type like '%WebhookEvent%' and payload like ?""";

    private static final String NOT_AN_ID = "not-an-object-id";

    private static final String PUBLISHABLE_KEY = "pk_test";

    private static final String SECRET_KEY = "sk_test";

    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    @Autowired
    private ExternalOrderSignalService externalOrderService;

    @Autowired
    private JdbcTemplate jdbc;

    private PaymentApiTestSupport api;

    @BeforeEach
    void setUp() {
        api = new PaymentApiTestSupport(port, signer);
        expect(api.send(HttpMethod.PUT, scoped(path(PRIVATE_CONFIG, STRIPE), STORE), api.admin(STORE),
                configBody(STRIPE, PUBLISHABLE_KEY, SECRET_KEY, WEBHOOK_SECRET_VALUE, true)), HttpStatus.OK);
    }

    /** Stages a pending transaction with an offline processor and answers its internal reference. */
    private String stage(String ref) {
        var response = api.post(scoped(INITIATE, STORE), api.s2s(), paymentRequestBody(ref, AMOUNT, COD));
        expect(response, HttpStatus.OK);
        return json(response).get(GATEWAY_REF).asString();
    }

    private String statusOf(String ref) {
        var response = api.get(scoped(path(PAYMENTS, ref, STATUS_SEGMENT), STORE), api.s2s());
        expect(response, HttpStatus.OK);
        return json(response).get(STATUS).asString();
    }

    private void deliver(String payload, String signature) {
        expect(api.postSigned(path(WEBHOOK, STORE, STRIPE), payload, signature), HttpStatus.OK);
    }

    private long webhookRowsCarrying(String reference) {
        Long count = jdbc.queryForObject(WEBHOOK_ROWS, Long.class, String.format("%%%s%%", reference));
        return count == null ? 0 : count;
    }

    /**
     * The outbox polls every two seconds; still PENDING after several rounds is what tells a discarded delivery
     * apart from a merely slow one.
     */
    private void staysPending(String ref) {
        await().pollDelay(QUIET_PERIOD).atMost(SETTLEMENT_TIMEOUT)
                .untilAsserted(() -> assertThat(statusOf(ref)).isEqualTo(PENDING));
    }

    @Test
    void aSignedCompletedSessionSettlesItsTransactionAndNotifiesCheckout() throws Exception {
        String ref = slug(ORDER);
        String internalRef = stage(ref);
        String payload = String.format(SESSION_COMPLETED_EVENT, internalRef);

        deliver(payload, stripeSignature(payload, WEBHOOK_SECRET_VALUE));

        assertThat(webhookRowsCarrying(internalRef)).isOne();
        await().atMost(SETTLEMENT_TIMEOUT).untilAsserted(() -> assertThat(statusOf(ref)).isEqualTo(PAID));
        verify(externalOrderService, timeout(SETTLEMENT_TIMEOUT.toMillis()))
                .signalPayment(eq(new StoreMerchantId(STORE)), eq(ref), argThat(signal -> signal.status() == PaymentStatus.PAID));
    }

    @Test
    void aPayloadSignedWithTheWrongSecretIsRefusedAndSettlesNothing() throws Exception {
        String ref = slug(ORDER);
        String internalRef = stage(ref);
        String payload = String.format(SESSION_COMPLETED_EVENT, internalRef);

        expect(api.postSigned(path(WEBHOOK, STORE, STRIPE), payload, stripeSignature(payload, "whsec_not_ours")),
                HttpStatus.BAD_REQUEST);

        assertThat(webhookRowsCarrying(internalRef)).isZero();
        staysPending(ref);
    }

    @Test
    void anUnsignedDeliveryIsRefusedWithoutTouchingTheOutbox() {
        String ref = slug(ORDER);
        String internalRef = stage(ref);
        String payload = String.format(SESSION_COMPLETED_EVENT, internalRef);

        // No Stripe-Signature header at all — the most ordinary probe a public endpoint sees. Before the check moved in
        // front of the outbox this was a 200 and a row for the taking.
        expect(api.postUnsigned(path(WEBHOOK, STORE, STRIPE), payload), HttpStatus.BAD_REQUEST);

        assertThat(webhookRowsCarrying(internalRef)).isZero();
        staysPending(ref);
    }

    @Test
    void anUnknownStoreIsNotFoundWhetherOrNotItLooksLikeOne() throws Exception {
        String ref = slug(ORDER);
        String payload = String.format(SESSION_COMPLETED_EVENT, ref);
        String signature = stripeSignature(payload, WEBHOOK_SECRET_VALUE);

        // A well-formed id nobody owns, and a string that is not an id at all: the same 404, so a probe learns nothing.
        expect(api.postSigned(path(WEBHOOK, new ObjectId().toHexString(), STRIPE), payload, signature),
                HttpStatus.NOT_FOUND);
        expect(api.postSigned(path(WEBHOOK, NOT_AN_ID, STRIPE), payload, signature), HttpStatus.NOT_FOUND);

        assertThat(webhookRowsCarrying(ref)).isZero();
    }

    @Test
    void aStoreWithNoEnabledConfigurationForTheTypeIsNotFound() throws Exception {
        String ref = slug(ORDER);
        String payload = String.format(SESSION_COMPLETED_EVENT, ref);
        String signature = stripeSignature(payload, WEBHOOK_SECRET_VALUE);
        expect(api.send(HttpMethod.PUT, scoped(path(PRIVATE_CONFIG, STRIPE), STORE), api.admin(STORE),
                configBody(STRIPE, PUBLISHABLE_KEY, SECRET_KEY, WEBHOOK_SECRET_VALUE, false)), HttpStatus.OK);

        // Disabled Stripe, and a type this pod has no processor for: neither has anything to verify against.
        expect(api.postSigned(path(WEBHOOK, STORE, STRIPE), payload, signature), HttpStatus.NOT_FOUND);
        expect(api.postSigned(path(WEBHOOK, STORE, PAYPAL), payload, signature), HttpStatus.NOT_FOUND);

        assertThat(webhookRowsCarrying(ref)).isZero();
    }

}
