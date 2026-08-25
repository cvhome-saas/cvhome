package com.asrevo.cvhome.payment.api.v1.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;

import com.asrevo.cvhome.payment.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.testsupport.annotations.StorageIntegrationTest;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;
import com.asrevo.cvhome.testsupport.security.Tokens;

import tools.jackson.databind.JsonNode;

import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.COD;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.FAILED;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.GATEWAY_REF;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.INITIATE;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.MANUAL_TRANSFER;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.ORDER;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.PAYMENTS;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.PAYPAL;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.PENDING;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.STATUS;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.STATUS_SEGMENT;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.expect;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.json;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.path;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.paymentRequestBody;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.scoped;
import static com.asrevo.cvhome.payment.api.v1.payment.PaymentApiTestSupport.slug;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The service-to-service gateway checkout calls, over real HTTP.
 *
 * <p>
 * Stripe is deliberately never initiated here — every case uses an offline processor or a type with no processor at
 * all. What the tests are actually about is the decisions payment makes on its own: an order that is initiated twice
 * must not become two payments, and a store that cannot take a payment must be told so as a {@code FAILED} result
 * rather than an exception, because a caller has to distinguish "we decided no" from "we could not decide".
 * </p>
 */
@StorageIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class PaymentGatewayApiIntegrationTest {

    /** The store this class initiates against. */
    private static final String STORE = Tokens.STORE_3;

    /** No row in payment_configuration at all. */
    private static final String UNCONFIGURED_STORE = "65f023632bc46470c104b999";

    private static final String AMOUNT = "42.50";

    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    private PaymentApiTestSupport api;

    @BeforeEach
    void setUp() {
        api = new PaymentApiTestSupport(port, signer);
    }

    private JsonNode initiate(String store, String ref, String type, HttpStatus expected) {
        var response = api.post(scoped(INITIATE, store), api.s2s(), paymentRequestBody(ref, AMOUNT, type));
        expect(response, expected);
        return json(response);
    }

    private JsonNode status(String store, String ref) {
        var response = api.get(scoped(path(PAYMENTS, ref, STATUS_SEGMENT), store), api.s2s());
        expect(response, HttpStatus.OK);
        return json(response);
    }

    @Test
    void anOfflinePaymentIsAcceptedAsPendingAndFindableByItsOrderReference() {
        String ref = slug(ORDER);

        JsonNode result = initiate(STORE, ref, COD, HttpStatus.OK);

        assertThat(result.get(STATUS).asString()).isEqualTo(PENDING);
        assertThat(result.get(GATEWAY_REF).asString()).isNotBlank();
        JsonNode status = status(STORE, ref);
        assertThat(status.get(STATUS).asString()).isEqualTo(PENDING);
        assertThat(status.get(GATEWAY_REF).asString()).isEqualTo(result.get(GATEWAY_REF).asString());
    }

    @Test
    void initiatingTheSameOrderTwiceReturnsTheFirstTransaction() {
        String ref = slug(ORDER);

        String first = initiate(STORE, ref, MANUAL_TRANSFER, HttpStatus.OK).get(GATEWAY_REF).asString();
        JsonNode second = initiate(STORE, ref, MANUAL_TRANSFER, HttpStatus.OK);

        assertThat(second.get(GATEWAY_REF).asString()).isEqualTo(first);
        assertThat(second.get(STATUS).asString()).isEqualTo(PENDING);
    }

    @Test
    void aStoreWithNoConfigurationIsAnAnswerNotAnError() {
        JsonNode result = initiate(UNCONFIGURED_STORE, slug(ORDER), COD, HttpStatus.OK);

        assertThat(result.get(STATUS).asString()).isEqualTo(FAILED);
        // Nothing was staged: refusing before createInitialTransaction is what keeps orphan rows out of the table.
        JsonNode gatewayRef = result.get(GATEWAY_REF);
        assertThat(gatewayRef == null || gatewayRef.isNull()).isTrue();
    }

    @Test
    void aConfiguredTypeWithNoProcessorFailsAfterTheTransactionIsStaged() {
        // PAYPAL is seeded and enabled in every store but no PaymentProcessor implements it.
        JsonNode result = initiate(STORE, slug(ORDER), PAYPAL, HttpStatus.OK);

        assertThat(result.get(STATUS).asString()).isEqualTo(FAILED);
    }

    @Test
    void anUnknownOrderReferenceHasAFailedStatusRatherThanA404() {
        assertThat(status(STORE, slug("never-initiated")).get(STATUS).asString()).isEqualTo(FAILED);
    }

    @Test
    void theGatewayIsNotOpenToUnauthenticatedCallers() {
        expect(api.post(scoped(INITIATE, STORE), null, paymentRequestBody(slug(ORDER), AMOUNT, COD)),
                HttpStatus.UNAUTHORIZED);
        expect(api.get(scoped(path(PAYMENTS, slug(ORDER), STATUS_SEGMENT), STORE), null), HttpStatus.UNAUTHORIZED);
    }

}
