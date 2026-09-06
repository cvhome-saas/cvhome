package com.asrevo.cvhome.checkout.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import com.asrevo.cvhome.checkout.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.checkout.errors.CheckoutErrors;
import com.asrevo.cvhome.testsupport.annotations.ServiceIntegrationTest;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;

import tools.jackson.databind.JsonNode;

import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.CODE;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.ID;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.ORDER_STATUS;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.SHOPPER_A;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.SKU;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.STORE_A;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.STORE_B;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.V1_PRIVATE;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.expect;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.json;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.path;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.scoped;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.with;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The console's orders: list, detail, history, the one write and its 409, and the two things every private endpoint
 * owes — a 403 without the token and nothing across stores.
 */
@ServiceIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
@TestPropertySource(properties = CheckoutApiSupport.POD_PROPERTY)
class OrderApiIntegrationTest {

    private static final String INVENTORYSTATUS = "inventoryStatus";

    private static final String PAYMENTSTATUS = "paymentStatus";

    private static final String DELIVERING_2 = "DELIVERING";

    private static final String PROCESSING_2 = "PROCESSING";

    private static final String CANCELLED_2 = "CANCELLED";

    private static final String CONFIRMED_2 = "CONFIRMED";

    private static final String DELIVERED_2 = "DELIVERED";

    private static final String PRODUCTS = "products";

    private static final String PACKING = "packing";

    private static final String BILLING = "billing";

    private static final String SHIPPED_2 = "SHIPPED";

    private static final String ID_S = "id=%s";

    private static final String TOTAL = "total";

    private static final String COD_2 = "COD";

    private static final String X = "x";

    private static final String ORDERS = "orders";

    private static final String HISTORY = "history";

    private static final String CONTENT = "content";

    private static final String TRANSITION = "{\"orderStatus\":\"%s\",\"comments\":\"%s\"}";

    private static final String EMAIL = "ada@example.com";

    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    private CheckoutApiSupport api;

    private String admin;

    @BeforeEach
    void setUp() {
        api = new CheckoutApiSupport(port, signer);
        admin = api.admin(STORE_A);
    }

    private long codOrder() {
        return api.placed(STORE_A, api.newCart(STORE_A, SKU, 2), api.shopper(STORE_A, SHOPPER_A), COD_2, EMAIL)
                .get(ID).asLong();
    }

    private ResponseEntity<String> transition(long id, String token, String status, String comment) {
        return api.send(HttpMethod.POST, scoped(path(V1_PRIVATE, ORDERS, id, HISTORY), STORE_A), token,
                String.format(TRANSITION, status, comment));
    }

    @Test
    void theListIsPagedFilteredAndCarriesTheConsoleShape() {
        long id = codOrder();

        JsonNode list = json(api.get(with(scoped(path(V1_PRIVATE, ORDERS), STORE_A), "page=0&count=5"), admin));
        assertThat(list.get("totalElements").asLong()).isPositive();
        assertThat(list.get(CONTENT).size()).isLessThanOrEqualTo(5);
        JsonNode first = list.get(CONTENT).get(0);
        assertThat(first.get(ID).asLong()).isGreaterThanOrEqualTo(id).as("newest first");
        assertThat(first.get(PRODUCTS).isNull()).as("the list omits lines").isTrue();
        assertThat(first.get(INVENTORYSTATUS).asString()).isNotBlank();
        assertThat(first.get(TOTAL).get(TOTAL).asString()).startsWith("$");
        assertThat(first.get("currency").asString()).isEqualTo("USD");

        JsonNode byId = json(api.get(with(scoped(path(V1_PRIVATE, ORDERS), STORE_A), String.format(ID_S, id)), admin));
        assertThat(byId.get(CONTENT)).hasSize(1);
        JsonNode byStatus = json(api.get(with(scoped(path(V1_PRIVATE, ORDERS), STORE_A),
                String.format("status=RETURNED&id=%s", id)), admin));
        assertThat(byStatus.get(CONTENT)).isEmpty();
        JsonNode byEmail = json(api.get(with(scoped(path(V1_PRIVATE, ORDERS), STORE_A), String.format("email=ADA@&id=%s", id)), admin));
        assertThat(byEmail.get(CONTENT)).hasSize(1);
        JsonNode byName = json(api.get(with(scoped(path(V1_PRIVATE, ORDERS), STORE_A), String.format("name=lovel&id=%s", id)), admin));
        assertThat(byName.get(CONTENT)).hasSize(1);
        String ref = first.get("orderRef").asString();
        JsonNode byRef = json(api.get(with(scoped(path(V1_PRIVATE, ORDERS), STORE_A), String.format("ref=%s", ref)), admin));
        assertThat(byRef.get(CONTENT)).as("the payments screen finds an order by the ref payment holds").hasSize(1);
        assertThat(byRef.get(CONTENT).get(0).get(ID).asLong()).isEqualTo(first.get(ID).asLong());
    }

    @Test
    void theDetailCarriesLinesCustomerAndAddresses() {
        long id = codOrder();

        JsonNode order = json(api.get(scoped(path(V1_PRIVATE, ORDERS, id), STORE_A), admin));

        assertThat(order.get(ORDER_STATUS).asString()).isEqualTo(CONFIRMED_2);
        assertThat(order.get("paymentType").asString()).isEqualTo(COD_2);
        assertThat(order.get(PRODUCTS).get(0).get("orderedQuantity").asInt()).isEqualTo(2);
        assertThat(order.get(PRODUCTS).get(0).get("price").asString()).isEqualTo("$10.00");
        assertThat(order.get(PRODUCTS).get(0).get("subTotal").asString()).isEqualTo("$20.00");
        assertThat(order.get("customer").get("emailAddress").asString()).isEqualTo(EMAIL);
        assertThat(order.get(BILLING).get("email").asString()).isEqualTo(EMAIL);
        assertThat(order.get(BILLING).get("country").asString()).isEqualTo("GB");
        assertThat(order.get("totals")).hasSize(2);
        assertThat(order.get("needsAttention").asBoolean()).isFalse();
        assertThat(order.has("redirectUri")).as("the console field is redirectUrl now").isFalse();
    }

    @Test
    void theConsoleWalksTheOrderForwardAndAnIllegalStepIs409() {
        long id = codOrder();

        ResponseEntity<String> processing = transition(id, admin, PROCESSING_2, PACKING);
        expect(processing, HttpStatus.CREATED);
        assertThat(json(processing).get(ORDER_STATUS).asString()).isEqualTo(PROCESSING_2);
        assertThat(json(processing).get("comments").asString()).isEqualTo(PACKING);

        ResponseEntity<String> skip = transition(id, admin, DELIVERED_2, "too fast");
        expect(skip, HttpStatus.CONFLICT);
        assertThat(json(skip).get(CODE).asString()).isEqualTo(CheckoutErrors.ORDER_ILLEGAL_TRANSITION.code());
        assertThat(json(skip).get("params").get("from").asString()).isEqualTo(PROCESSING_2);

        expect(transition(id, admin, SHIPPED_2, ""), HttpStatus.CREATED);
        expect(transition(id, admin, DELIVERING_2, ""), HttpStatus.CREATED);
        expect(transition(id, admin, DELIVERED_2, "cash collected"), HttpStatus.CREATED);

        JsonNode history = json(api.get(scoped(path(V1_PRIVATE, ORDERS, id, HISTORY), STORE_A), admin));
        assertThat(history).extracting(node -> node.get(ORDER_STATUS).asString())
                .containsExactly("CREATED", CONFIRMED_2, PROCESSING_2, SHIPPED_2, DELIVERING_2, DELIVERED_2);
        JsonNode order = json(api.get(scoped(path(V1_PRIVATE, ORDERS, id), STORE_A), admin));
        assertThat(order.get(PAYMENTSTATUS).asString()).as("COD is paid at the door").isEqualTo("PAID");
    }

    @Test
    void cancellingFromTheConsoleReleasesTheStock() {
        long id = codOrder();

        expect(transition(id, admin, CANCELLED_2, "customer asked"), HttpStatus.CREATED);

        JsonNode order = json(api.get(scoped(path(V1_PRIVATE, ORDERS, id), STORE_A), admin));
        assertThat(order.get(ORDER_STATUS).asString()).isEqualTo(CANCELLED_2);
        assertThat(order.get(INVENTORYSTATUS).asString()).isEqualTo("COMMITTED");
        assertThat(order.get(PAYMENTSTATUS).asString()).isEqualTo(CANCELLED_2);
    }

    @Test
    void privateEndpointsRefuseWithoutTheSellerToken() {
        long id = codOrder();
        String shopper = api.shopper(STORE_A, SHOPPER_A);

        expect(api.get(scoped(path(V1_PRIVATE, ORDERS), STORE_A), null), HttpStatus.UNAUTHORIZED);
        expect(api.get(scoped(path(V1_PRIVATE, ORDERS), STORE_A), shopper), HttpStatus.FORBIDDEN);
        expect(api.get(scoped(path(V1_PRIVATE, ORDERS, id), STORE_A), shopper), HttpStatus.FORBIDDEN);
        expect(api.get(scoped(path(V1_PRIVATE, ORDERS, id, HISTORY), STORE_A), api.moderator(STORE_A)),
                HttpStatus.FORBIDDEN);
        expect(transition(id, shopper, PROCESSING_2, X), HttpStatus.FORBIDDEN);
    }

    @Test
    void anotherStoresSellerSeesNothing() {
        long id = codOrder();
        String otherAdmin = api.admin(STORE_B);

        JsonNode list = json(api.get(with(scoped(path(V1_PRIVATE, ORDERS), STORE_B), String.format(ID_S, id)), otherAdmin));
        assertThat(list.get(CONTENT)).isEmpty();
        expect(api.get(scoped(path(V1_PRIVATE, ORDERS, id), STORE_B), otherAdmin), HttpStatus.NOT_FOUND);
        expect(api.get(scoped(path(V1_PRIVATE, ORDERS, id, HISTORY), STORE_B), otherAdmin), HttpStatus.NOT_FOUND);
        expect(api.send(HttpMethod.POST, scoped(path(V1_PRIVATE, ORDERS, id, HISTORY), STORE_B), otherAdmin,
                String.format(TRANSITION, PROCESSING_2, X)), HttpStatus.NOT_FOUND);
        expect(api.get(scoped(path(V1_PRIVATE, ORDERS, id), STORE_B), admin), HttpStatus.FORBIDDEN);
    }
}
