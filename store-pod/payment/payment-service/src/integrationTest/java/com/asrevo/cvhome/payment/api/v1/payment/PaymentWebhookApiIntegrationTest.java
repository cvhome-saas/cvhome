package com.asrevo.cvhome.payment.api.v1.payment;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

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
 * The endpoint answers 200 to everything by design — it schedules an outbox record and returns. A forged payload is
 * therefore asserted by what does <em>not</em> happen to the transaction, which is the only observable difference
 * between an accepted webhook and a discarded one.
 * </p>
 */
@StorageIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class PaymentWebhookApiIntegrationTest {

    /** The store this class owns; its Stripe webhook secret is rewritten below. */
    private static final String STORE = Tokens.STORE_4;

    private static final String WEBHOOK_SECRET_VALUE = "whsec_integration_test";

    private static final String AMOUNT = "31.00";

    private static final Duration SETTLEMENT_TIMEOUT = Duration.ofSeconds(45);

    private static final Duration QUIET_PERIOD = Duration.ofSeconds(8);

    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    @Autowired
    private ExternalOrderSignalService externalOrderService;

    private PaymentApiTestSupport api;

    @BeforeEach
    void setUp() {
        api = new PaymentApiTestSupport(port, signer);
        expect(api.send(HttpMethod.PUT, scoped(path(PRIVATE_CONFIG, STRIPE), STORE), api.admin(STORE),
                configBody(STRIPE, "pk_test", "sk_test", WEBHOOK_SECRET_VALUE, true)), HttpStatus.OK);
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
        String payload = String.format(SESSION_COMPLETED_EVENT, stage(ref));

        deliver(payload, stripeSignature(payload, WEBHOOK_SECRET_VALUE));

        await().atMost(SETTLEMENT_TIMEOUT).untilAsserted(() -> assertThat(statusOf(ref)).isEqualTo(PAID));
        verify(externalOrderService, timeout(SETTLEMENT_TIMEOUT.toMillis()))
                .signalPayment(eq(new StoreMerchantId(STORE)), eq(ref), argThat(signal -> signal.status() == PaymentStatus.PAID));
    }

    @Test
    void aPayloadSignedWithTheWrongSecretSettlesNothing() throws Exception {
        String ref = slug(ORDER);
        String payload = String.format(SESSION_COMPLETED_EVENT, stage(ref));

        deliver(payload, stripeSignature(payload, "whsec_not_ours"));

        staysPending(ref);
    }

    @Test
    void anUnsignedDeliveryIsRefusedInsteadOfFailingInTheProvidersVerifier() {
        String ref = slug(ORDER);
        String payload = String.format(SESSION_COMPLETED_EVENT, stage(ref));

        // No Stripe-Signature header at all — the most ordinary probe a public endpoint sees.
        expect(api.postUnsigned(path(WEBHOOK, STORE, STRIPE), payload), HttpStatus.OK);

        staysPending(ref);
    }

}
