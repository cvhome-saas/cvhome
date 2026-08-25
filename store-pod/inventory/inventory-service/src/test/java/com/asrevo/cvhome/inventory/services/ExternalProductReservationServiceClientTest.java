package com.asrevo.cvhome.inventory.services;

import java.net.SocketTimeoutException;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.api.errors.InventoryApiErrors;
import com.asrevo.cvhome.inventory.api.errors.InventoryApiUnavailableException;
import com.asrevo.cvhome.inventory.api.errors.ProductReservationRejectedException;
import com.asrevo.cvhome.inventory.errors.InventoryErrors;
import com.asrevo.cvhome.inventory.model.reservation.ProductReservationCommitResult;
import com.asrevo.cvhome.inventory.model.reservation.ProductReservationReleaseResult;
import com.asrevo.cvhome.inventory.model.reservation.ProductReservationReserveResult;
import com.asrevo.cvhome.s2s.utils.WebClientsUtils;
import com.asrevo.cvhome.store.core.model.catalog.ProductReservationList;
import com.asrevo.cvhome.store.core.model.catalog.ReserveProductEntry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * The client proxy checkout is handed, end to end over a mocked HTTP exchange: paths, the store query parameter, and
 * the wire-to-exception decoding declared on {@link ExternalProductReservationService}.
 */
class ExternalProductReservationServiceClientTest {

    private static final String BASE = "http://inventory";

    private static final String PRIVATE = "http://inventory/api/v1/private/%s/%s";

    private static final String STORE_ID = "65f023632bc46470c104b76f";

    private static final StoreMerchantId STORE = new StoreMerchantId(STORE_ID);

    private static final String REF = "order-1";

    private static final String SKU = "SKU-1";

    private static final String RESULT = "{\"status\":true,\"reservationId\":5,\"expireAt\":\"2026-08-24T10:00:00Z\"}";

    private MockRestServiceServer server;

    private ExternalProductReservationService client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = WebClientsUtils.build(builder, BASE, ExternalProductReservationService.class,
                InventoryApiErrors.INVENTORY);
    }

    private static ProductReservationList oneLine() {
        return new ProductReservationList(Set.of(new ReserveProductEntry(SKU, 2)));
    }

    @Test
    void reservePostsTheLinesToTheRefScopedToTheStore() throws Exception {
        server.expect(requestTo(startsWith(String.format(PRIVATE, "reserve", REF))))
                .andExpect(method(HttpMethod.POST))
                .andExpect(queryParam("store", STORE_ID))
                .andExpect(content().json("{\"entries\":[{\"sku\":\"SKU-1\",\"reserveQty\":2}]}"))
                .andRespond(withSuccess(RESULT, MediaType.APPLICATION_JSON));

        ProductReservationReserveResult result = client.reserve(STORE, REF, oneLine());

        assertThat(result.status()).isTrue();
        assertThat(result.reservationId()).isEqualTo(5L);
        server.verify();
    }

    @Test
    void aRefusalForLackOfStockArrivesAsARejectionNamingTheSku() {
        String problem = String.format("{\"code\":\"%s\",\"detail\":\"Only 1 of sku SKU-1 available, 2 requested.\","
                + "\"params\":{\"sku\":\"SKU-1\",\"requested\":2,\"available\":1}}",
                InventoryErrors.RESERVATION_INSUFFICIENT_INVENTORY.code());
        server.expect(requestTo(containsString("/reserve/")))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_CONTENT).contentType(MediaType.APPLICATION_JSON)
                        .body(problem));

        assertThatThrownBy(() -> client.reserve(STORE, REF, oneLine()))
                .isInstanceOf(ProductReservationRejectedException.class)
                .satisfies(e -> {
                    ProductReservationRejectedException rejected = (ProductReservationRejectedException) e;
                    assertThat(rejected.payload().params()).containsEntry("sku", SKU);
                    assertThat(rejected.remoteStatus()).isEqualTo(422);
                });
    }

    @Test
    void commitAndReleasePostToTheirOwnPaths() throws Exception {
        server.expect(requestTo(startsWith(String.format(PRIVATE, "commit", REF))))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(RESULT, MediaType.APPLICATION_JSON));
        server.expect(requestTo(startsWith(String.format(PRIVATE, "release", REF))))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(RESULT.replace("true", "false"), MediaType.APPLICATION_JSON));

        ProductReservationCommitResult committed = client.commit(STORE, REF);
        ProductReservationReleaseResult released = client.release(STORE, REF);

        assertThat(committed.status()).isTrue();
        assertThat(released.status()).isFalse();
        server.verify();
    }

    @Test
    void anUnreachableInventoryArrivesAsUnavailableNotAsARejection() {
        server.expect(requestTo(containsString("/commit/")))
                .andRespond(request -> {
                    throw new ResourceAccessException("timeout", new SocketTimeoutException("read timed out"));
                });

        assertThatThrownBy(() -> client.commit(STORE, REF))
                .isInstanceOf(InventoryApiUnavailableException.class)
                .isNotInstanceOf(ProductReservationRejectedException.class);
    }

    @Test
    void aServerErrorWithoutAProblemBodyIsNotNarrowedIntoADeclaredType() {
        server.expect(requestTo(containsString("/release/")))
                .andRespond(withStatus(HttpStatus.BAD_GATEWAY).body("<html>gateway</html>"));

        // No catalog entry for a bare 502: the proxy cannot narrow it into a declared type, so the carrier flows on
        // for the shared advice to render.
        assertThatThrownBy(() -> client.release(STORE, REF)).isNotInstanceOf(InventoryApiUnavailableException.class);
    }
}
