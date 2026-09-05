package com.asrevo.cvhome.checkout.api;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import com.asrevo.cvhome.checkout.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.checkout.errors.CheckoutErrors;
import com.asrevo.cvhome.errors.remote.RemoteErrorContext;
import com.asrevo.cvhome.inventory.api.errors.InventoryApiUnavailableException;
import com.asrevo.cvhome.inventory.api.errors.ProductReservationRejectedException;
import com.asrevo.cvhome.inventory.services.ExternalProductReservationService;
import com.asrevo.cvhome.payment.api.errors.PaymentGatewayRejectedException;
import com.asrevo.cvhome.payment.services.payment.ExternalPaymentGatewayService;
import com.asrevo.cvhome.testsupport.annotations.ServiceIntegrationTest;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;

import tools.jackson.databind.JsonNode;

import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.CODE;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.ID;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.ORDER_STATUS;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.PAYMENT_STATUS;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.REDIRECT_URL;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.SHOPPER_A;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.SHOPPER_B;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.SKU;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.STORE_A;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.STORE_B;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.V1;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.expect;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.json;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.path;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.scoped;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

/**
 * Placement end to end against Postgres with the neighbours stubbed: every payment type, the rows it leaves behind,
 * and the two failure shapes — a refusal that closes the order, and an outage that leaves it resumable.
 */
@ServiceIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class CheckoutApiIntegrationTest {

    private static final String SELECT_STATUS_FROM_CHECKOUT_CART_WHERE_CART_CODE = "select status from checkout.cart where cart_code = ?";

    private static final String INVENTORY_RESERVATION_INSUFFICIENT_INVENTORY = "INVENTORY.RESERVATION.INSUFFICIENT_INVENTORY";

    private static final String PAYMENT_INITIATE_REJECTED = "PAYMENT.INITIATE.REJECTED";

    private static final String GUEST_EXAMPLE_COM = "guest@example.com";

    private static final String INVENTORY_STATUS = "inventory_status";

    private static final String PENDING_ACTION = "pending_action";

    private static final String ORDER_STATUS_2 = "order_status";

    private static final String RESERVED_2 = "RESERVED";

    private static final String PRODUCTS = "products";

    private static final String CREATED_2 = "CREATED";

    private static final String STATUS = "status";

    private static final String REMOTE = "remote";

    private static final String ORDER = "order";

    private static final String CART = "cart";

    private static final String NONE_2 = "NONE";

    private static final String EMAIL = "ada@example.com";

    private static final String STRIPE = "STRIPE";

    private static final String COD = "COD";

    private static final String MANUAL_TRANSFER = "MANUAL_TRANSFER";

    private static final String CONFIRMED = "CONFIRMED";

    private static final String PENDING_PAYMENT = "PENDING_PAYMENT";

    private static final String CANCELLED = "CANCELLED";

    private static final String SELECT_ORDER = """
            select order_status, payment_status, inventory_status, pending_action, needs_attention
              from checkout.sales_order where order_id = ?
            """;

    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private ExternalProductReservationService reservations;

    @Autowired
    private ExternalPaymentGatewayService payments;

    private CheckoutApiSupport api;

    private String shopperA;

    @BeforeEach
    void setUp() {
        api = new CheckoutApiSupport(port, signer);
        shopperA = api.shopper(STORE_A, SHOPPER_A);
    }

    @AfterEach
    void restoreDefaults() throws Exception {
        ExternalClientsTestConfiguration.reset(reservations, payments);
    }

    private Map<String, Object> row(long orderId) {
        return jdbc.queryForMap(SELECT_ORDER, orderId);
    }

    private static RemoteErrorContext refusal(String code) {
        return new RemoteErrorContext(code, "refused", Map.of(), List.of(), REMOTE, 422, null, null);
    }

    private static RemoteErrorContext outage() {
        return new RemoteErrorContext(null, null, Map.of(), List.of(), REMOTE, 0, null, new RuntimeException("down"));
    }

    @Test
    void aCardOrderIsReservedInitiatedAndLeftWaitingWithARedirect() {
        String cart = api.newCart(STORE_A, SKU, 2);

        JsonNode order = api.placed(STORE_A, cart, shopperA, STRIPE, EMAIL);

        long orderId = order.get(ID).asLong();
        assertThat(order.get(ORDER_STATUS).asString()).isEqualTo(PENDING_PAYMENT);
        assertThat(order.get(PAYMENT_STATUS).asString()).isEqualTo("PENDING");
        assertThat(order.get(REDIRECT_URL).asString()).isEqualTo(ExternalClientsTestConfiguration.REDIRECT);
        assertThat(order.get("payment").asString()).isEqualTo(STRIPE);
        assertThat(order.get("total").get("grandTotal").asString()).isEqualTo("$20.00");
        assertThat(order.get(PRODUCTS).get(0).get("productName").asString()).isEqualTo(String.format("Product %s", SKU));
        assertThat(order.get("billing").get("city").asString()).isEqualTo("London");
        assertThat(order.get("orderRef").asString()).hasSize(36);

        Map<String, Object> row = row(orderId);
        assertThat(row).containsEntry(ORDER_STATUS_2, PENDING_PAYMENT).containsEntry(INVENTORY_STATUS, RESERVED_2)
                .containsEntry(PENDING_ACTION, NONE_2).containsEntry("needs_attention", false);
        assertThat(jdbc.queryForObject("select count(*) from checkout.sales_order_line where order_id = ?", Integer.class,
                orderId)).isEqualTo(1);
        assertThat(jdbc.queryForObject("select count(*) from checkout.sales_order_total where order_id = ?", Integer.class,
                orderId)).isEqualTo(2);
        assertThat(jdbc.queryForList("""
                select event_type from checkout.sales_order_event where order_id = ? order by occurred_at, event_id
                """, String.class, orderId))
                .containsExactly("PLACED", RESERVED_2, "PAYMENT_INITIATED");
        assertThat(jdbc.queryForList("""
                select status from checkout.sales_order_history where order_id = ? order by date_added, history_id
                """, String.class, orderId))
                .containsExactly(CREATED_2, PENDING_PAYMENT);
        assertThat(jdbc.queryForObject(SELECT_STATUS_FROM_CHECKOUT_CART_WHERE_CART_CODE, String.class, cart))
                .isEqualTo("CONVERTED");
        assertThat(jdbc.queryForObject("select expires_at is not null from checkout.sales_order where order_id = ?",
                Boolean.class, orderId)).isTrue();

        JsonNode status = json(api.get(scoped(path(V1, ORDER, orderId, STATUS), STORE_A), shopperA));
        assertThat(status.get("orderId").asLong()).isEqualTo(orderId);
        assertThat(status.get(REDIRECT_URL).asString()).isEqualTo(ExternalClientsTestConfiguration.REDIRECT);
    }

    @Test
    void aCodOrderIsConfirmedAndCommittedInTheRequest() throws Exception {
        String cart = api.newCart(STORE_A, ExternalClientsTestConfiguration.SKU_VARIANT, 1);

        JsonNode order = api.placed(STORE_A, cart, shopperA, COD, EMAIL);

        long orderId = order.get(ID).asLong();
        assertThat(order.get(ORDER_STATUS).asString()).isEqualTo(CONFIRMED);
        assertThat(order.get(REDIRECT_URL) == null || order.get(REDIRECT_URL).isNull()).isTrue();
        assertThat(order.get(PRODUCTS).get(0).get("attributes").get(0).get("attributeValue").asString()).isEqualTo("L");
        assertThat(row(orderId)).containsEntry(INVENTORY_STATUS, "COMMITTED").containsEntry(PENDING_ACTION, NONE_2);
        assertThat(jdbc.queryForObject("select expires_at from checkout.sales_order where order_id = ?", Object.class,
                orderId)).isNull();
        Mockito.verify(reservations).commit(any(), any());
    }

    @Test
    void aManualTransferWaitsForTheMerchantWithoutARedirect() {
        String cart = api.newCart(STORE_A, SKU, 1);

        JsonNode order = api.placed(STORE_A, cart, shopperA, MANUAL_TRANSFER, EMAIL);

        assertThat(order.get(ORDER_STATUS).asString()).isEqualTo(PENDING_PAYMENT);
        assertThat(order.get(REDIRECT_URL).isNull()).isTrue();
    }

    @Test
    void aGuestMayOrderWhereTheStoreAllowsItAndNotWhereItDoesNot() {
        String openCart = api.newCart(STORE_A, SKU, 1);
        JsonNode guestOrder = api.placed(STORE_A, openCart, null, COD, GUEST_EXAMPLE_COM);
        assertThat(guestOrder.get(ORDER_STATUS).asString()).isEqualTo(CONFIRMED);
        assertThat(jdbc.queryForObject("select cua_external_id from checkout.customer_account where email = ?",
                String.class, GUEST_EXAMPLE_COM)).isEqualTo("guest:guest@example.com");

        String closedCart = api.newCart(STORE_B, SKU, 1);
        ResponseEntity<String> refused = api.checkout(STORE_B, closedCart, null, COD, EMAIL);
        expect(refused, HttpStatus.UNAUTHORIZED);
        assertThat(json(refused).get(CODE).asString()).isEqualTo(CheckoutErrors.ORDER_LOGIN_REQUIRED.code());

        JsonNode signedIn = api.placed(STORE_B, closedCart, api.shopper(STORE_B, SHOPPER_B), COD, EMAIL);
        assertThat(signedIn.get(ORDER_STATUS).asString()).isEqualTo(CONFIRMED);
    }

    @Test
    void aRefusedReservationCancelsTheOrderAndHandsTheCartBack() throws Exception {
        String cart = api.newCart(STORE_A, SKU, 1);
        Mockito.doThrow(ProductReservationRejectedException.from(refusal(INVENTORY_RESERVATION_INSUFFICIENT_INVENTORY)))
                .when(reservations).reserve(any(), any(), any());

        ResponseEntity<String> response = api.checkout(STORE_A, cart, shopperA, STRIPE, EMAIL);

        expect(response, HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(json(response).get(CODE).asString()).isEqualTo(INVENTORY_RESERVATION_INSUFFICIENT_INVENTORY);
        assertThat(jdbc.queryForObject(SELECT_STATUS_FROM_CHECKOUT_CART_WHERE_CART_CODE, String.class, cart))
                .isEqualTo("ACTIVE");
        assertThat(jdbc.queryForMap("select order_status, inventory_status from checkout.sales_order where cart_code = ?",
                cart)).containsEntry(ORDER_STATUS_2, CANCELLED).containsEntry(INVENTORY_STATUS, "RESERVATION_FAILED");
        Mockito.verify(payments, Mockito.never()).initiatePayment(any(), any());

        // The shopper edits the cart and tries again with a fresh cart: the old code is spent.
        ResponseEntity<String> again = api.checkout(STORE_A, cart, shopperA, STRIPE, EMAIL);
        assertThat(again.getStatusCode()).isIn(HttpStatus.CONFLICT, HttpStatus.UNPROCESSABLE_CONTENT,
                HttpStatus.CREATED);
    }

    @Test
    void anInventoryOutageAnswers502AndTheSameCartResumesTheSameOrder() throws Exception {
        String cart = api.newCart(STORE_A, SKU, 1);
        Mockito.doThrow(InventoryApiUnavailableException.from(outage())).when(reservations).reserve(any(), any(), any());

        ResponseEntity<String> first = api.checkout(STORE_A, cart, shopperA, STRIPE, EMAIL);
        expect(first, HttpStatus.BAD_GATEWAY);
        Map<String, Object> stuck = jdbc.queryForMap(
                "select order_id, order_status, pending_action from checkout.sales_order where cart_code = ?", cart);
        assertThat(stuck).containsEntry(ORDER_STATUS_2, CREATED_2).containsEntry(PENDING_ACTION, "RESERVE");

        ExternalClientsTestConfiguration.reset(reservations, payments);
        JsonNode resumed = api.placed(STORE_A, cart, shopperA, STRIPE, EMAIL);

        assertThat(resumed.get(ID).asLong()).isEqualTo(((Number) stuck.get("order_id")).longValue());
        assertThat(resumed.get(ORDER_STATUS).asString()).isEqualTo(PENDING_PAYMENT);
        assertThat(jdbc.queryForObject("select count(*) from checkout.sales_order where cart_code = ?", Integer.class,
                cart)).as("one order, not two").isEqualTo(1);
    }

    @Test
    void aRefusedPaymentCancelsTheOrderAndReleasesTheStock() throws Exception {
        String cart = api.newCart(STORE_A, SKU, 1);
        Mockito.doThrow(PaymentGatewayRejectedException.from(refusal(PAYMENT_INITIATE_REJECTED)))
                .when(payments).initiatePayment(any(), any());

        ResponseEntity<String> response = api.checkout(STORE_A, cart, shopperA, STRIPE, EMAIL);

        expect(response, HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(json(response).get(CODE).asString()).isEqualTo(PAYMENT_INITIATE_REJECTED);
        Map<String, Object> row = jdbc.queryForMap(SELECT_ORDER.replace("order_id = ?", "cart_code = ?"), cart);
        assertThat(row).containsEntry(ORDER_STATUS_2, CANCELLED).containsEntry("payment_status", "FAILED")
                .containsEntry(INVENTORY_STATUS, "RELEASED").containsEntry(PENDING_ACTION, NONE_2);
        Mockito.verify(reservations).release(any(), any());
    }

    @Test
    void aClosedOrdersCartIsSpent() throws Exception {
        String cart = api.newCart(STORE_A, SKU, 1);
        Mockito.doThrow(PaymentGatewayRejectedException.from(refusal(PAYMENT_INITIATE_REJECTED)))
                .when(payments).initiatePayment(any(), any());
        api.checkout(STORE_A, cart, shopperA, STRIPE, EMAIL);
        ExternalClientsTestConfiguration.reset(reservations, payments);

        ResponseEntity<String> again = api.checkout(STORE_A, cart, shopperA, STRIPE, EMAIL);
        expect(again, HttpStatus.CONFLICT);
        assertThat(json(again).get(CODE).asString()).isEqualTo(CheckoutErrors.CART_ALREADY_CONVERTED.code());
        expect(api.get(scoped(path(V1, CART, cart), STORE_A), null), HttpStatus.NOT_FOUND);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"paymentType\":\"STRIPE\"}", "{\"paymentType\":\"STRIPE\",\"customer\":{}}"})
    void anIncompleteBodyIsAValidationError(String body) {
        String cart = api.newCart(STORE_A, SKU, 1);

        ResponseEntity<String> response = api.send(HttpMethod.POST, scoped(path(V1, CART, cart, "checkout"), STORE_A),
                shopperA, body);

        expect(response, HttpStatus.BAD_REQUEST);
    }

    @Test
    void anEmptyOrUnknownCartCannotBeOrdered() {
        String cart = api.newCart(STORE_A, SKU, 1);
        api.send(HttpMethod.DELETE, scoped(path(V1, CART, cart, "product", SKU), STORE_A), null, null);

        ResponseEntity<String> empty = api.checkout(STORE_A, cart, shopperA, COD, EMAIL);
        expect(empty, HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(json(empty).get(CODE).asString()).isEqualTo(CheckoutErrors.CART_EMPTY.code());

        ResponseEntity<String> unknown = api.checkout(STORE_A, "no-such-cart", shopperA, COD, EMAIL);
        expect(unknown, HttpStatus.NOT_FOUND);

        ResponseEntity<String> otherStore = api.checkout(STORE_B, api.newCart(STORE_A, SKU, 1),
                api.shopper(STORE_B, SHOPPER_B), COD, EMAIL);
        expect(otherStore, HttpStatus.NOT_FOUND);
    }

    @Test
    void theStatusReadIsOwnedByTheShopperWhoOrdered() {
        long orderId = api.placed(STORE_A, api.newCart(STORE_A, SKU, 1), shopperA, STRIPE, EMAIL).get(ID).asLong();

        expect(api.get(scoped(path(V1, ORDER, orderId, STATUS), STORE_A), shopperA), HttpStatus.OK);
        expect(api.get(scoped(path(V1, ORDER, orderId, STATUS), STORE_A),
                api.shopper(STORE_A, CheckoutApiSupport.SHOPPER_A2)), HttpStatus.NOT_FOUND);
        expect(api.get(scoped(path(V1, ORDER, orderId, STATUS), STORE_B), api.shopper(STORE_B, SHOPPER_B)),
                HttpStatus.NOT_FOUND);
        expect(api.get(scoped(path(V1, ORDER, 999_999, STATUS), STORE_A), null), HttpStatus.NOT_FOUND);
    }
}
