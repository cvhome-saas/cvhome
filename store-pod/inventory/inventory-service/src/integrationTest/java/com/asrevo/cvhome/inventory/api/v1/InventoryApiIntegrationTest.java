package com.asrevo.cvhome.inventory.api.v1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.asrevo.cvhome.inventory.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.testsupport.annotations.ServiceIntegrationTest;
import com.asrevo.cvhome.testsupport.http.ApiClient;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;
import com.asrevo.cvhome.testsupport.security.Tokens;

import tools.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The merchant's write side and the public bulk read over HTTP: an upsert creates then edits one row per sku, a
 * product delete removes its rows, and a store admin of another store can neither read the row through the private
 * API nor change it.
 */
@ServiceIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class InventoryApiIntegrationTest {

    private static final String STORE_A = Tokens.STORE_1;

    private static final String STORE_B = Tokens.STORE_2;

    private static final String PRIVATE = "/api/v1/private/inventory";

    private static final String AVAILABILITY = "/api/v1/availability";

    private static final String SKUS = "skus=";

    private static final String SEEDED_SKU = "SKU-NK-RUN-001";

    private static final String OTHER_STORE_SKU = "ELEC-SKU-136";

    private static final String QUANTITY = "quantity";

    private static final String PRICE = "price";

    private static final String SKU_FIELD = "sku";

    private static final String CAN_BE_PURCHASED = "canBePurchased";

    private static final String FINAL_PRICE = "finalPrice";

    private static final String MIN = "quantityOrderMinimum";

    private static final String MAX = "quantityOrderMaximum";

    private static final String DISCOUNTED = "discounted";

    private static final String BAD = "SKU-BAD";

    private static final String DEL = "SKU-DEL";

    private static final String BY_PRODUCT = "by-product";

    private static final String NULL = "null";

    private static final String BODY = """
            {"productId":%d,"quantity":%d,"available":%b,"quantityOrderMinimum":%s,"quantityOrderMaximum":%s,
             "price":{"amount":"20.00","specialAmount":%s,"specialStartDate":null,"specialEndDate":null}}""";

    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    private ApiClient api;

    private Tokens tokens;

    private String admin;

    @BeforeEach
    void setUp() {
        api = new ApiClient(port);
        tokens = new Tokens(signer);
        admin = tokens.staff(Tokens.ROLE_STORE_ADMIN, STORE_A);
    }

    private static String body(long productId, int quantity, boolean available, String min, String max,
                               String special) {
        return String.format(BODY, productId, quantity, available, min, max, special);
    }

    private ResponseEntity<String> upsert(String store, String token, String sku, String body) {
        return api.send(HttpMethod.PUT, ApiClient.scoped(ApiClient.path(PRIVATE, sku), store), token, body);
    }

    private JsonNode availability(String store, String... skus) {
        var response = api.get(ApiClient.scoped(ApiClient.query(AVAILABILITY, SKUS + String.join(",", skus)), store),
                null);
        ApiClient.expect(response, HttpStatus.OK);
        return ApiClient.json(response);
    }

    @Test
    void availabilityIsPublicAndOnlyAnswersForTheStoreAsked() {
        JsonNode own = availability(STORE_A, SEEDED_SKU, OTHER_STORE_SKU, "NO-SUCH-SKU");

        assertThat(own).hasSize(1);
        assertThat(own.get(0).get(SKU_FIELD).asString()).isEqualTo(SEEDED_SKU);
        assertThat(own.get(0).get(QUANTITY).asInt()).isEqualTo(25);
        assertThat(own.get(0).get(CAN_BE_PURCHASED).asBoolean()).isTrue();
        assertThat(own.get(0).get(PRICE).get(FINAL_PRICE).asDouble()).isPositive();
        assertThat(availability(STORE_B, OTHER_STORE_SKU)).hasSize(1);
        assertThat(availability(STORE_B, SEEDED_SKU)).isEmpty();
    }

    @Test
    void upsertCreatesThenEditsOneRowPerSku() {
        String sku = ApiClient.slug("SKU-IT");
        long productId = 900001L;

        var created = upsert(STORE_A, admin, sku, body(productId, 7, true, NULL, NULL, "\"15.00\""));
        ApiClient.expect(created, HttpStatus.OK);
        JsonNode first = ApiClient.json(created);
        assertThat(first.get(SKU_FIELD).asString()).isEqualTo(sku);
        assertThat(first.get("productId").asLong()).isEqualTo(productId);
        assertThat(first.get(QUANTITY).asInt()).isEqualTo(7);
        assertThat(first.get(MIN).asInt()).isEqualTo(1);
        assertThat(first.get(MAX).asInt()).isZero();
        assertThat(first.get(PRICE).get(DISCOUNTED).asBoolean()).isTrue();
        assertThat(first.get(PRICE).get(FINAL_PRICE).asDouble()).isEqualTo(15.0);
        assertThat(first.get(PRICE).get("discountPercent").asInt()).isEqualTo(25);

        var edited = upsert(STORE_A, admin, sku, body(productId, 0, false, "2", "4", NULL));
        ApiClient.expect(edited, HttpStatus.OK);
        JsonNode second = ApiClient.json(edited);
        assertThat(second.get(QUANTITY).asInt()).isZero();
        assertThat(second.get(CAN_BE_PURCHASED).asBoolean()).isFalse();
        assertThat(second.get(MIN).asInt()).isEqualTo(2);
        assertThat(second.get(MAX).asInt()).isEqualTo(4);
        assertThat(second.get(PRICE).get(DISCOUNTED).asBoolean()).isFalse();
        assertThat(second.get(PRICE).get(FINAL_PRICE).asDouble()).isEqualTo(20.0);

        assertThat(availability(STORE_A, sku)).hasSize(1);
        assertThat(availability(STORE_A, sku).get(0).get(QUANTITY).asInt()).isZero();
    }

    @Test
    void invalidBodyIsRejected() {
        var response = upsert(STORE_A, admin, ApiClient.slug(BAD),
                "{\"quantity\":-1,\"available\":true,\"price\":{\"amount\":\"1.00\"}}");
        ApiClient.expect(response, HttpStatus.BAD_REQUEST);
        var noPrice = upsert(STORE_A, admin, ApiClient.slug(BAD), "{\"quantity\":1,\"available\":true}");
        ApiClient.expect(noPrice, HttpStatus.BAD_REQUEST);
    }

    @Test
    void deleteByProductRemovesEveryRowOfTheProductInThatStoreOnly() {
        long productId = 900002L;
        String skuOne = ApiClient.slug(DEL);
        String skuTwo = ApiClient.slug(DEL);
        String otherAdmin = tokens.staff(Tokens.ROLE_STORE_ADMIN, STORE_B);
        String otherSku = ApiClient.slug("SKU-KEEP");
        ApiClient.expect(upsert(STORE_A, admin, skuOne, body(productId, 1, true, NULL, NULL, NULL)),
                HttpStatus.OK);
        ApiClient.expect(upsert(STORE_A, admin, skuTwo, body(productId, 1, true, NULL, NULL, NULL)),
                HttpStatus.OK);
        ApiClient.expect(upsert(STORE_B, otherAdmin, otherSku, body(productId, 1, true, NULL, NULL, NULL)),
                HttpStatus.OK);

        var deleted = api.send(HttpMethod.DELETE,
                ApiClient.scoped(ApiClient.path(PRIVATE, BY_PRODUCT, productId), STORE_A), admin, null);
        ApiClient.expect(deleted, HttpStatus.OK);

        assertThat(availability(STORE_A, skuOne, skuTwo)).isEmpty();
        assertThat(availability(STORE_B, otherSku)).hasSize(1);
    }

    @Test
    void anotherStoresAdminCannotWriteIntoThisStore() {
        String other = tokens.staff(Tokens.ROLE_STORE_ADMIN, STORE_B);

        var upserted = upsert(STORE_A, other, SEEDED_SKU, body(1, 0, false, NULL, NULL, NULL));
        ApiClient.expect(upserted, HttpStatus.FORBIDDEN);
        var deleted = api.send(HttpMethod.DELETE, ApiClient.scoped(ApiClient.path(PRIVATE, BY_PRODUCT, 1), STORE_A),
                other, null);
        ApiClient.expect(deleted, HttpStatus.FORBIDDEN);
        assertThat(availability(STORE_A, SEEDED_SKU).get(0).get(QUANTITY).asInt()).isEqualTo(25);
    }

    @Test
    void moderatorAndAnonymousCannotWrite() {
        String moderator = tokens.staff(Tokens.ROLE_STORE_MODERATOR, STORE_A);
        String body = body(1, 0, false, NULL, NULL, NULL);

        ApiClient.expect(upsert(STORE_A, moderator, SEEDED_SKU, body), HttpStatus.FORBIDDEN);
        ApiClient.expect(upsert(STORE_A, null, SEEDED_SKU, body), HttpStatus.UNAUTHORIZED);
    }
}
