package com.asrevo.cvhome.checkout.api;

import java.time.ZonedDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;

import com.asrevo.cvhome.checkout.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.testsupport.annotations.ServiceIntegrationTest;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;

import tools.jackson.databind.JsonNode;

import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.SHOPPER_A;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.STORE_A;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.STORE_B;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.V1;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.expect;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.json;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.path;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.scoped;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The country list in the request language, and the three dashboard charts scoped to one store.
 */
@ServiceIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
@TestPropertySource(properties = CheckoutApiSupport.POD_PROPERTY)
class ReferenceAndStatisticApiIntegrationTest {

    private static final String CUSTOMER_STATISTIC = "customer-statistic";

    private static final String PRODUCT_STATISTIC = "product-statistic";

    private static final String ORDER_STATISTIC = "order-statistic";

    private static final String COUNTRY = "country";

    private static final String V2_PRIVATE = "/api/v2/private";

    private static final String ENTRIES = "entries";

    private static final String NAME = "name";

    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    private CheckoutApiSupport api;

    @BeforeEach
    void setUp() {
        api = new CheckoutApiSupport(port, signer);
    }

    private String range() {
        ZonedDateTime now = ZonedDateTime.now();
        return String.format("{\"fromDate\":\"%s\",\"toDate\":\"%s\"}", now.minusDays(1), now.plusDays(1));
    }

    private JsonNode statistic(String store, String token, String chart) {
        var response = api.send(HttpMethod.POST, scoped(path(V2_PRIVATE, chart), store), token, range());
        expect(response, HttpStatus.OK);
        return json(response);
    }

    @Test
    void countriesArePublicAndNamedInTheRequestLanguage() {
        JsonNode en = json(api.get(scoped(path(V1, COUNTRY), STORE_A), null));
        JsonNode fr = json(api.get(String.format("%s?store=%s&lang=fr", path(V1, COUNTRY), STORE_A), null));

        assertThat(en.size()).isGreaterThan(200);
        assertThat(en.findValuesAsString(NAME)).contains("Germany");
        assertThat(fr.findValuesAsString(NAME)).contains("Allemagne");
        assertThat(en.get(0).get("supported").asBoolean()).isTrue();
        assertThat(en.get(0).get("zones")).isEmpty();
    }

    @Test
    void theThreeChartsCountOneStoresOrders() {
        String sku = String.format("SKU-STAT-%s", java.util.UUID.randomUUID().toString().substring(0, 8));
        api.placed(STORE_A, api.newCart(STORE_A, sku, 3), api.shopper(STORE_A, SHOPPER_A), "COD", "ada@example.com");
        String admin = api.admin(STORE_A);

        JsonNode orders = statistic(STORE_A, admin, ORDER_STATISTIC);
        assertThat(orders.get(ENTRIES)).isNotEmpty();
        assertThat(orders.get(ENTRIES).findValuesAsString(NAME)).contains("CONFIRMED");
        assertThat(orders.get(ENTRIES).get(0).get("date").asString()).isNotBlank();

        JsonNode customers = statistic(STORE_A, admin, CUSTOMER_STATISTIC);
        assertThat(customers.get(ENTRIES).findValuesAsString(NAME)).contains("GB");

        JsonNode products = statistic(STORE_A, admin, PRODUCT_STATISTIC);
        JsonNode entry = products.get(ENTRIES).valueStream().filter(e -> sku.equals(e.get(NAME).asString())).findFirst()
                .orElseThrow();
        assertThat(entry.get("value").asInt()).as("units, not orders").isEqualTo(3);

        JsonNode otherStore = statistic(STORE_B, api.admin(STORE_B), PRODUCT_STATISTIC);
        assertThat(otherStore.get(ENTRIES).findValuesAsString(NAME)).doesNotContain(sku);
    }

    @Test
    void statisticsNeedTheSellerToken() {
        expect(api.send(HttpMethod.POST, scoped(path(V2_PRIVATE, ORDER_STATISTIC), STORE_A),
                api.shopper(STORE_A, SHOPPER_A), range()), HttpStatus.FORBIDDEN);
        expect(api.send(HttpMethod.POST, scoped(path(V2_PRIVATE, ORDER_STATISTIC), STORE_A), null, range()),
                HttpStatus.UNAUTHORIZED);
        expect(api.send(HttpMethod.POST, scoped(path(V2_PRIVATE, CUSTOMER_STATISTIC), STORE_A), api.admin(STORE_B),
                range()), HttpStatus.FORBIDDEN);
    }
}
