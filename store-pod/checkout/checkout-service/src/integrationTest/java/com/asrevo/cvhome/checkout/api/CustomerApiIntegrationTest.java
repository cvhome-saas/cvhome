package com.asrevo.cvhome.checkout.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;

import com.asrevo.cvhome.checkout.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.testsupport.annotations.ServiceIntegrationTest;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;

import tools.jackson.databind.JsonNode;

import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.ID;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.ORDER_STATUS;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.SHOPPER_A;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.SHOPPER_A2;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.SHOPPER_B;
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
 * The shopper's own view and the console's customer list: a shopper sees exactly their orders, a seller cannot use
 * the shopper endpoints, and the list is one store's.
 */
@ServiceIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
@TestPropertySource(properties = CheckoutApiSupport.POD_PROPERTY)
class CustomerApiIntegrationTest {

    private static final String NEVER_ORDERED = "never-ordered";

    private static final String CUA_EXTERNAL_ID = "cuaExternalId";

    private static final String PAGE_0_COUNT_50 = "page=0&count=50";

    private static final String BOB_EXAMPLE_COM = "bob@example.com";

    private static final String CUAEXTERNALID = CUA_EXTERNAL_ID;

    private static final String EMAILADDRESS = "emailAddress";

    private static final String CONFIRMED_2 = "CONFIRMED";

    private static final String HISTORY = "history";

    private static final String ORDERS = "orders";

    private static final String INFO = "info";

    private static final String COD_2 = "COD";

    private static final String CUSTOMER = "customer";

    private static final String CUSTOMERS = "customers";

    private static final String ORDER = "order";

    private static final String CONTENT = "content";

    private static final String EMAIL_A = "ada@example.com";

    private static final String EMAIL_A2 = "grace@example.com";

    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    private CheckoutApiSupport api;

    private String shopperA;

    @BeforeEach
    void setUp() {
        api = new CheckoutApiSupport(port, signer);
        shopperA = api.shopper(STORE_A, SHOPPER_A);
    }

    @Test
    void aShopperReadsTheirProfileAndOnlyTheirOrders() {
        long own = api.placed(STORE_A, api.newCart(STORE_A, SKU, 1), shopperA, COD_2, EMAIL_A).get(ID).asLong();
        long someoneElses = api.placed(STORE_A, api.newCart(STORE_A, SKU, 1), api.shopper(STORE_A, SHOPPER_A2), COD_2,
                EMAIL_A2).get(ID).asLong();

        JsonNode info = json(api.get(scoped(path(V1_PRIVATE, CUSTOMER, INFO), STORE_A), shopperA));
        assertThat(info.get(EMAILADDRESS).asString()).isEqualTo(EMAIL_A);
        assertThat(info.get(CUAEXTERNALID).asString()).isEqualTo(SHOPPER_A);
        assertThat(info.get("billing").get("city").asString()).isEqualTo("London");
        assertThat(info.get("firstName").asString()).isEqualTo("Ada");

        JsonNode orders = json(api.get(with(scoped(path(V1_PRIVATE, CUSTOMER, ORDERS), STORE_A), PAGE_0_COUNT_50),
                shopperA));
        assertThat(orders.get(CONTENT)).extracting(node -> node.get(ID).asLong()).contains(own)
                .doesNotContain(someoneElses);
        assertThat(orders.get(CONTENT).get(0).get("products")).isNotEmpty();

        JsonNode detail = json(api.get(scoped(path(V1_PRIVATE, CUSTOMER, own, ORDER), STORE_A), shopperA));
        assertThat(detail.get(ORDER_STATUS).asString()).isEqualTo(CONFIRMED_2);
        assertThat(detail.get("total").get("grandTotal").asString()).isEqualTo("$10.00");
        JsonNode history = json(api.get(scoped(path(V1_PRIVATE, CUSTOMER, own, ORDER, HISTORY), STORE_A), shopperA));
        assertThat(history).extracting(node -> node.get(ORDER_STATUS).asString()).contains("CREATED", CONFIRMED_2);
        assertThat(history.get(0).get("orderId").asLong()).isEqualTo(own);

        expect(api.get(scoped(path(V1_PRIVATE, CUSTOMER, someoneElses, ORDER), STORE_A), shopperA), HttpStatus.NOT_FOUND);
        expect(api.get(scoped(path(V1_PRIVATE, CUSTOMER, someoneElses, ORDER, HISTORY), STORE_A), shopperA),
                HttpStatus.NOT_FOUND);
    }

    @Test
    void aShopperWhoNeverOrderedGetsAnEmptyProfileAndAnEmptyList() {
        String fresh = api.shopper(STORE_A, NEVER_ORDERED);

        JsonNode info = json(api.get(scoped(path(V1_PRIVATE, CUSTOMER, INFO), STORE_A), fresh));
        assertThat(info.get(CUA_EXTERNAL_ID).asString()).isEqualTo(NEVER_ORDERED);
        assertThat(info.get(ID).isNull()).isTrue();
        JsonNode orders = json(api.get(scoped(path(V1_PRIVATE, CUSTOMER, ORDERS), STORE_A), fresh));
        assertThat(orders.get(CONTENT)).isEmpty();
        assertThat(orders.get("totalElements").asLong()).isZero();
    }

    @Test
    void shopperEndpointsRefuseSellersAndForeignShoppers() {
        api.placed(STORE_A, api.newCart(STORE_A, SKU, 1), shopperA, COD_2, EMAIL_A);

        expect(api.get(scoped(path(V1_PRIVATE, CUSTOMER, INFO), STORE_A), api.admin(STORE_A)), HttpStatus.FORBIDDEN);
        expect(api.get(scoped(path(V1_PRIVATE, CUSTOMER, ORDERS), STORE_A), api.s2s()), HttpStatus.FORBIDDEN);
        expect(api.get(scoped(path(V1_PRIVATE, CUSTOMER, INFO), STORE_A), api.shopper(STORE_B, SHOPPER_B)),
                HttpStatus.FORBIDDEN);
        expect(api.get(scoped(path(V1_PRIVATE, CUSTOMER, INFO), STORE_A), null), HttpStatus.UNAUTHORIZED);
    }

    @Test
    void theConsoleListsOneStoresCustomersWithFilters() {
        api.placed(STORE_A, api.newCart(STORE_A, SKU, 1), shopperA, COD_2, EMAIL_A);
        api.placed(STORE_B, api.newCart(STORE_B, SKU, 1), api.shopper(STORE_B, SHOPPER_B), COD_2, BOB_EXAMPLE_COM);

        JsonNode all = json(api.get(with(scoped(path(V1_PRIVATE, CUSTOMERS), STORE_A), PAGE_0_COUNT_50), api.admin(STORE_A)));
        assertThat(all.get(CONTENT)).extracting(node -> node.get(EMAILADDRESS).asString()).contains(EMAIL_A)
                .doesNotContain(BOB_EXAMPLE_COM);
        assertThat(all.get(CONTENT).get(0).has(CUAEXTERNALID)).isTrue();

        JsonNode byEmail = json(api.get(with(scoped(path(V1_PRIVATE, CUSTOMERS), STORE_A), "email=ada@"), api.admin(STORE_A)));
        assertThat(byEmail.get(CONTENT)).hasSize(1);
        JsonNode byCountry = json(api.get(with(scoped(path(V1_PRIVATE, CUSTOMERS), STORE_A), "country=gb&email=ada@"),
                api.admin(STORE_A)));
        assertThat(byCountry.get(CONTENT)).hasSize(1);
        JsonNode byName = json(api.get(with(scoped(path(V1_PRIVATE, CUSTOMERS), STORE_A), "name=nobody-here"), api.admin(STORE_A)));
        assertThat(byName.get(CONTENT)).isEmpty();

        expect(api.get(scoped(path(V1_PRIVATE, CUSTOMERS), STORE_A), shopperA), HttpStatus.FORBIDDEN);
        expect(api.get(scoped(path(V1_PRIVATE, CUSTOMERS), STORE_A), api.admin(STORE_B)), HttpStatus.FORBIDDEN);
    }
}
