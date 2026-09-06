package com.asrevo.cvhome.checkout.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import com.asrevo.cvhome.checkout.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.checkout.errors.CheckoutErrors;
import com.asrevo.cvhome.testsupport.annotations.ServiceIntegrationTest;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;

import tools.jackson.databind.JsonNode;

import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.CODE;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.ID;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.ORDER_STATUS;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.PAYMENT_STATUS;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.SHOPPER_A;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.SKU;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.STORE_A;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.STORE_B;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.V1_PRIVATE;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.expect;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.json;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.path;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.scoped;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Payment and inventory reporting on an order over HTTP: idempotent, order-tolerant, and only for the pod's own
 * service principal.
 */
@ServiceIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
@TestPropertySource(properties = CheckoutApiSupport.POD_PROPERTY)
class ExternalOrderSignalApiIntegrationTest {

    private static final String ADA_EXAMPLE_COM = "ada@example.com";

    private static final String CANCELLED_2 = "CANCELLED";

    private static final String DUPLICATE_2 = "DUPLICATE";

    private static final String CONFIRMED_2 = "CONFIRMED";

    private static final String ORDERREF = "orderRef";

    private static final String PAYMENT_2 = "payment";

    private static final String SIGNALS = "signals";

    private static final String ORDERS = "orders";

    private static final String TX_1 = "tx-1";

    private static final String TX_3 = "tx-3";

    private static final String PAID_2 = "PAID";

    private static final String TX = "tx";

    private static final String OUTCOME = "outcome";

    private static final String PAYMENT_BODY = "{\"status\":\"%s\",\"transactionRef\":\"%s\"}";

    private static final String EXPIRED_BODY = "{\"reservationRef\":\"%s\"}";

    private static final String APPLIED = "APPLIED";

    private static final String IGNORED = "IGNORED";

    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    @Autowired
    private JdbcTemplate jdbc;

    private CheckoutApiSupport api;

    private String s2s;

    @BeforeEach
    void setUp() {
        api = new CheckoutApiSupport(port, signer);
        s2s = api.s2s();
    }

    private JsonNode placeCardOrder() {
        return api.placed(STORE_A, api.newCart(STORE_A, SKU, 1), api.shopper(STORE_A, SHOPPER_A), "STRIPE",
                ADA_EXAMPLE_COM);
    }

    private ResponseEntity<String> payment(String store, String ref, String token, String status, String tx) {
        return api.send(HttpMethod.POST, scoped(path(V1_PRIVATE, ORDERS, ref, SIGNALS, PAYMENT_2), store), token,
                String.format(PAYMENT_BODY, status, tx));
    }

    private ResponseEntity<String> expired(String store, String ref, String token) {
        return api.send(HttpMethod.POST, scoped(path(V1_PRIVATE, ORDERS, ref, SIGNALS, "reservation-expired"), store),
                token, String.format(EXPIRED_BODY, ref));
    }

    @Test
    void aPaidSignalConfirmsTheOrderCommitsTheStockAndIsIdempotent() {
        JsonNode order = placeCardOrder();
        String ref = order.get(ORDERREF).asString();
        long id = order.get(ID).asLong();

        ResponseEntity<String> first = payment(STORE_A, ref, s2s, PAID_2, TX_1);
        expect(first, HttpStatus.OK);
        assertThat(json(first).get(OUTCOME).asString()).isEqualTo(APPLIED);
        assertThat(json(first).get(ORDER_STATUS).asString()).isEqualTo(CONFIRMED_2);
        assertThat(json(first).get(PAYMENT_STATUS).asString()).isEqualTo(PAID_2);
        assertThat(jdbc.queryForMap("select inventory_status, pending_action from checkout.sales_order where order_id = ?",
                id)).containsEntry("inventory_status", "COMMITTED").containsEntry("pending_action", "NONE");

        ResponseEntity<String> again = payment(STORE_A, ref, s2s, PAID_2, TX_1);
        expect(again, HttpStatus.OK);
        assertThat(json(again).get(OUTCOME).asString()).isEqualTo(DUPLICATE_2);
        assertThat(jdbc.queryForObject("""
                select count(*) from checkout.sales_order_event
                 where order_id = ? and source = 'PAYMENT' and (source_ref = 'tx-1:PAID' or payload = 'tx-1:PAID')
                """, Integer.class, id)).as("the applied row and the duplicate row").isEqualTo(2);

        ResponseEntity<String> late = payment(STORE_A, ref, s2s, "FAILED", TX_1);
        assertThat(json(late).get(OUTCOME).asString()).isEqualTo(IGNORED);
        assertThat(json(late).get(ORDER_STATUS).asString()).isEqualTo(CONFIRMED_2);
    }

    @Test
    void aRejectedTransferCancelsTheOrderAndReleasesTheStock() {
        JsonNode order = api.placed(STORE_A, api.newCart(STORE_A, SKU, 1), api.shopper(STORE_A, SHOPPER_A),
                "MANUAL_TRANSFER", ADA_EXAMPLE_COM);
        String ref = order.get(ORDERREF).asString();

        JsonNode outcome = json(payment(STORE_A, ref, s2s, "REJECTED", "tx-2"));

        assertThat(outcome.get(OUTCOME).asString()).isEqualTo(APPLIED);
        assertThat(outcome.get(ORDER_STATUS).asString()).isEqualTo(CANCELLED_2);
        assertThat(jdbc.queryForObject("select inventory_status from checkout.sales_order where order_ref = ?",
                String.class, ref)).isEqualTo("RELEASED");
    }

    @Test
    void anExpiredReservationCancelsAnUnpaidOrder() {
        String ref = placeCardOrder().get(ORDERREF).asString();

        ResponseEntity<String> response = expired(STORE_A, ref, s2s);
        expect(response, HttpStatus.OK);
        JsonNode outcome = json(response);

        assertThat(outcome.get(OUTCOME).asString()).isEqualTo(APPLIED);
        assertThat(outcome.get(ORDER_STATUS).asString()).isEqualTo(CANCELLED_2);
        assertThat(outcome.get(PAYMENT_STATUS).asString()).isEqualTo("EXPIRED");
        assertThat(json(expired(STORE_A, ref, s2s)).get(OUTCOME).asString()).isEqualTo(DUPLICATE_2);
    }

    @Test
    void onlyThePodsServicePrincipalMaySignal() {
        String ref = placeCardOrder().get(ORDERREF).asString();

        expect(payment(STORE_A, ref, api.shopper(STORE_A, SHOPPER_A), PAID_2, TX_3), HttpStatus.FORBIDDEN);
        expect(payment(STORE_A, ref, api.admin(STORE_A), PAID_2, TX_3), HttpStatus.FORBIDDEN);
        expect(payment(STORE_A, ref, null, PAID_2, TX_3), HttpStatus.UNAUTHORIZED);
        expect(expired(STORE_A, ref, api.admin(STORE_A)), HttpStatus.FORBIDDEN);
        assertThat(jdbc.queryForObject("select payment_status from checkout.sales_order where order_ref = ?",
                String.class, ref)).isEqualTo("PENDING");
    }

    @Test
    void anUnknownRefOrAnotherStoresRefIs404() {
        String ref = placeCardOrder().get(ORDERREF).asString();

        ResponseEntity<String> unknown = payment(STORE_A, "00000000-0000-0000-0000-000000000000", s2s, PAID_2, TX);
        expect(unknown, HttpStatus.NOT_FOUND);
        assertThat(json(unknown).get(CODE).asString()).isEqualTo(CheckoutErrors.ORDER_NOT_FOUND.code());

        expect(payment(STORE_B, ref, s2s, PAID_2, TX), HttpStatus.NOT_FOUND);
        expect(expired(STORE_B, ref, s2s), HttpStatus.NOT_FOUND);
    }

    @Test
    void aMalformedSignalIsABadRequest() {
        String ref = placeCardOrder().get(ORDERREF).asString();

        ResponseEntity<String> response = api.send(HttpMethod.POST,
                scoped(path(V1_PRIVATE, ORDERS, ref, SIGNALS, PAYMENT_2), STORE_A), s2s, "{\"status\":\"PAID\"}");

        expect(response, HttpStatus.BAD_REQUEST);
    }
}
