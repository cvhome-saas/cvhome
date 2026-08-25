package com.asrevo.cvhome.inventory.api.v1;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import com.asrevo.cvhome.checkout.services.order.ExternalOrderService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.inventory.entity.ProductReservation;
import com.asrevo.cvhome.inventory.errors.InventoryErrors;
import com.asrevo.cvhome.inventory.repositories.ProductReservationRepository;
import com.asrevo.cvhome.inventory.services.ReservationExpiryJob;
import com.asrevo.cvhome.testsupport.annotations.ServiceIntegrationTest;
import com.asrevo.cvhome.testsupport.http.ApiClient;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;
import com.asrevo.cvhome.testsupport.security.Tokens;

import tools.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * Checkout's reserve / commit / release round trips over HTTP against Postgres: stock leaves on reserve and comes back
 * on release or expiry, a retry never takes it twice, and only a service principal of this pod may call any of it.
 */
@ServiceIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
@TestPropertySource(properties = {
        "com.asrevo.cvhome.pod-info.pod.name=pod-507f1f77",
        "reservation.cleanup.interval=3600000"})
class ExternalProductReservationApiIntegrationTest {

    static final String POD = "pod-507f1f77";

    private static final String STORE_A = Tokens.STORE_1;

    private static final String STORE_B = Tokens.STORE_2;

    private static final String PRIVATE = "/api/v1/private";

    private static final String RESERVE = "reserve";

    private static final String COMMIT = "commit";

    private static final String RELEASE = "release";

    private static final String AVAILABILITY = "/api/v1/availability?skus=";

    /** Seeded in store 1 with 25 units. */
    private static final String SKU = "SKU-NK-RUN-001";

    /** Seeded in store 1 with 5 units. */
    private static final String SCARCE_SKU = "SKU-GU-BG-MAR05";

    private static final String STATUS = "status";

    private static final String ORDER = "order";

    private static final String RESERVATION_ID = "reservationId";

    private static final String NO_SUCH_REF = "no-such-ref";

    private static final String CODE = "code";

    private static final String PARAMS = "params";

    private static final String AVAILABLE = "available";

    private static final String ENTRIES = "{\"entries\":[{\"sku\":\"%s\",\"reserveQty\":%d}]}";

    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    @Autowired
    private ProductReservationRepository reservations;

    @Autowired
    private ReservationExpiryJob expiryJob;

    @Autowired
    private ExternalOrderService externalOrderService;

    private ApiClient api;

    private Tokens tokens;

    private String s2s;

    @BeforeEach
    void setUp() {
        api = new ApiClient(port);
        tokens = new Tokens(signer);
        s2s = tokens.s2s(Tokens.SCOPE_STORE_POD, POD);
    }

    private ResponseEntity<String> call(String action, String store, String token, String ref, String body) {
        return api.send(HttpMethod.POST, ApiClient.scoped(ApiClient.path(PRIVATE, action, ref), store), token, body);
    }

    private JsonNode ok(ResponseEntity<String> response) {
        ApiClient.expect(response, HttpStatus.OK);
        return ApiClient.json(response);
    }

    private int quantity(String store, String sku) {
        var response = api.get(ApiClient.scoped(AVAILABILITY + sku, store), null);
        ApiClient.expect(response, HttpStatus.OK);
        return ApiClient.json(response).get(0).get("quantity").asInt();
    }

    private static String entries(String sku, int quantity) {
        return String.format(ENTRIES, sku, quantity);
    }

    @Test
    void reserveTakesStockAndCommitKeepsItTaken() {
        String ref = ApiClient.slug(ORDER);
        int before = quantity(STORE_A, SKU);

        JsonNode reserved = ok(call(RESERVE, STORE_A, s2s, ref, entries(SKU, 3)));
        assertThat(reserved.get(STATUS).asBoolean()).isTrue();
        assertThat(reserved.get(RESERVATION_ID).asLong()).isPositive();
        assertThat(reserved.get("expireAt").asString()).isNotBlank();
        assertThat(quantity(STORE_A, SKU)).isEqualTo(before - 3);

        JsonNode retried = ok(call(RESERVE, STORE_A, s2s, ref, entries(SKU, 3)));
        assertThat(retried.get(RESERVATION_ID).asLong()).isEqualTo(reserved.get(RESERVATION_ID).asLong());
        assertThat(quantity(STORE_A, SKU)).as("a retry of the same ref takes nothing more").isEqualTo(before - 3);

        JsonNode committed = ok(call(COMMIT, STORE_A, s2s, ref, null));
        assertThat(committed.get(STATUS).asBoolean()).isTrue();
        assertThat(quantity(STORE_A, SKU)).isEqualTo(before - 3);

        JsonNode releasedAfterCommit = ok(call(RELEASE, STORE_A, s2s, ref, null));
        assertThat(releasedAfterCommit.get(STATUS).asBoolean()).isFalse();
        assertThat(quantity(STORE_A, SKU)).isEqualTo(before - 3);
    }

    @Test
    void releaseGivesTheStockBack() {
        String ref = ApiClient.slug(ORDER);
        int before = quantity(STORE_A, SKU);
        ok(call(RESERVE, STORE_A, s2s, ref, entries(SKU, 2)));

        JsonNode released = ok(call(RELEASE, STORE_A, s2s, ref, null));

        assertThat(released.get(STATUS).asBoolean()).isTrue();
        assertThat(quantity(STORE_A, SKU)).isEqualTo(before);
        assertThat(ok(call(COMMIT, STORE_A, s2s, ref, null)).get(STATUS).asBoolean()).isFalse();
        assertThat(ok(call(RELEASE, STORE_A, s2s, ref, null)).get(STATUS).asBoolean()).isTrue();
    }

    @Test
    void unknownRefCannotBeCommittedOrReleased() {
        JsonNode committed = ok(call(COMMIT, STORE_A, s2s, NO_SUCH_REF, null));
        assertThat(committed.get(STATUS).asBoolean()).isFalse();
        assertThat(committed.get(RESERVATION_ID).isNull()).isTrue();
        assertThat(ok(call(RELEASE, STORE_A, s2s, NO_SUCH_REF, null)).get(STATUS).asBoolean()).isFalse();
    }

    @Test
    void shortStockIsRefusedWithTheSkuAndNothingIsTaken() {
        int before = quantity(STORE_A, SCARCE_SKU);

        var response = call(RESERVE, STORE_A, s2s, ApiClient.slug(ORDER), entries(SCARCE_SKU, before + 1));

        ApiClient.expect(response, HttpStatus.UNPROCESSABLE_CONTENT);
        JsonNode problem = ApiClient.json(response);
        assertThat(problem.get(CODE).asString())
                .isEqualTo(InventoryErrors.RESERVATION_INSUFFICIENT_INVENTORY.code());
        assertThat(problem.get(PARAMS).get("sku").asString()).isEqualTo(SCARCE_SKU);
        assertThat(problem.get(PARAMS).get(AVAILABLE).asInt()).isEqualTo(before);
        assertThat(quantity(STORE_A, SCARCE_SKU)).isEqualTo(before);
    }

    @Test
    void skuOfAnotherStoreIsNotStockedHere() {
        var response = call(RESERVE, STORE_B, s2s, ApiClient.slug(ORDER), entries(SKU, 1));

        ApiClient.expect(response, HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(ApiClient.json(response).get(PARAMS).get(AVAILABLE).asInt()).isZero();
    }

    @Test
    void emptyReservationIsABadRequest() {
        var response = call(RESERVE, STORE_A, s2s, ApiClient.slug(ORDER), "{\"entries\":[]}");

        ApiClient.expect(response, HttpStatus.BAD_REQUEST);
        assertThat(ApiClient.json(response).get(CODE).asString()).isEqualTo(InventoryErrors.RESERVATION_EMPTY.code());
    }

    @Test
    void reservationsAreKeyedByStoreSoAnotherStoreCannotCommitOrReleaseThem() {
        String ref = ApiClient.slug(ORDER);
        int before = quantity(STORE_A, SKU);
        ok(call(RESERVE, STORE_A, s2s, ref, entries(SKU, 1)));

        assertThat(ok(call(RELEASE, STORE_B, s2s, ref, null)).get(STATUS).asBoolean()).isFalse();
        assertThat(ok(call(COMMIT, STORE_B, s2s, ref, null)).get(STATUS).asBoolean()).isFalse();
        assertThat(quantity(STORE_A, SKU)).isEqualTo(before - 1);
    }

    @Test
    void onlyAServicePrincipalOfThisPodMayReserve() {
        String ref = ApiClient.slug(ORDER);
        String body = entries(SKU, 1);

        ApiClient.expect(call(RESERVE, STORE_A, tokens.staff(Tokens.ROLE_STORE_ADMIN, STORE_A), ref, body),
                HttpStatus.FORBIDDEN);
        ApiClient.expect(call(COMMIT, STORE_A, tokens.orgAdmin(Tokens.ORG_1), ref, null), HttpStatus.FORBIDDEN);
        ApiClient.expect(call(RELEASE, STORE_A, tokens.s2s(Tokens.SCOPE_STORE_POD, "pod-other"), ref, null),
                HttpStatus.FORBIDDEN);
        ApiClient.expect(call(RESERVE, STORE_A, tokens.s2s(Tokens.SCOPE_STORE_CORE, POD), ref, body),
                HttpStatus.FORBIDDEN);
        ApiClient.expect(call(RESERVE, STORE_A, null, ref, body), HttpStatus.UNAUTHORIZED);
    }

    @Test
    void expiryJobGivesBackStockOfOverdueReservationsAndTellsCheckout() {
        String ref = ApiClient.slug(ORDER);
        int before = quantity(STORE_A, SKU);
        ok(call(RESERVE, STORE_A, s2s, ref, entries(SKU, 4)));
        ProductReservation reservation = reservations.findByStoreMerchantIdAndRef(new StoreMerchantId(STORE_A), ref)
                .orElseThrow();
        reservation.setExpireAt(Instant.now().minus(Duration.ofMinutes(1)));
        reservations.save(reservation);

        expiryJob.releaseExpired();

        assertThat(quantity(STORE_A, SKU)).isEqualTo(before);
        verify(externalOrderService).handleReservationExpired(new StoreMerchantId(STORE_A), ref);
        assertThat(ok(call(COMMIT, STORE_A, s2s, ref, null)).get(STATUS).asBoolean())
                .as("an expired and released reservation cannot be committed").isFalse();
    }
}
