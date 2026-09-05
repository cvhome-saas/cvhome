package com.asrevo.cvhome.inventory.api.v1;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestClient;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.errors.UncheckedBaseException;
import com.asrevo.cvhome.inventory.api.errors.InventoryApiErrors;
import com.asrevo.cvhome.inventory.api.errors.InventoryApiUnavailableException;
import com.asrevo.cvhome.inventory.api.errors.ProductReservationRejectedException;
import com.asrevo.cvhome.inventory.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.inventory.model.AvailabilityQuery;
import com.asrevo.cvhome.inventory.services.ExternalInventoryService;
import com.asrevo.cvhome.inventory.services.ExternalProductReservationService;
import com.asrevo.cvhome.s2s.utils.WebClientsUtils;
import com.asrevo.cvhome.store.core.model.catalog.ProductReservationList;
import com.asrevo.cvhome.store.core.model.catalog.ReserveProductEntry;
import com.asrevo.cvhome.testsupport.annotations.ServiceIntegrationTest;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;
import com.asrevo.cvhome.testsupport.security.Tokens;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Inventory's own reservation client, driven against inventory's running controller.
 *
 * <p>
 * {@code ExternalProductReservationService} carries a warning in its javadoc: <em>"The paths below are not checked
 * against {@code ExternalProductReservationApi}'s {@code @PostMapping} by any compiler. Keep them in step by
 * eye."</em> This test is that compiler. It builds the real proxy — the same
 * {@code WebClientsUtils.build(...)} every consumer gets — and calls it against the real controller, so a path or
 * a body shape that drifted fails here rather than in checkout at runtime.
 * </p>
 *
 * <p>
 * The half that matters most is the error contract. A refusal for lack of stock has to arrive as
 * {@link ProductReservationRejectedException} and not as a generic remote failure, because that distinction is what
 * the order flow turns into "fail the order and name the sku" as against "leave it recoverable". That mapping lives
 * in {@code InventoryApiErrors.INVENTORY} and is only exercised on the caller's side — inventory's own tests
 * cannot reach it, and its only production consumer is checkout.
 * </p>
 */
@ServiceIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
@TestPropertySource(properties = {
        "com.asrevo.cvhome.pod-info.pod.name=pod-507f1f77",
        "reservation.cleanup.interval=3600000"})
class ReservationClientContractIntegrationTest {

    static final String POD = "pod-507f1f77";

    private static final String UNREACHABLE = "unreachable";
    private static final String LOCALHOST = "http://localhost:%d";

    private static final String STORE_A = Tokens.STORE_1;

    /** Seeded in store 1 with 25 units. */
    private static final String SKU = "SKU-NK-RUN-001";

    /** Seeded in store 1 with 5 units — enough to be refused by asking for more. */
    private static final String SCARCE_SKU = "SKU-GU-BG-MAR05";

    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    private ExternalProductReservationService inventory;

    @BeforeEach
    void setUp() {
        inventory = clientFor(port);
    }

    @Test
    void thereserveCommitAndReleasePathsAllMatchTheControllerTheyName() throws Exception {
        StoreMerchantId store = new StoreMerchantId(STORE_A);
        String ref = String.format("client-contract-%d", System.nanoTime());

        assertThat(inventory.reserve(store, ref, oneOf(SKU, 1))).isNotNull();
        assertThat(inventory.commit(store, ref)).isNotNull();

        String second = String.format("%s-b", ref);
        assertThat(inventory.reserve(store, second, oneOf(SKU, 1))).isNotNull();
        assertThat(inventory.release(store, second)).isNotNull();
    }

    @Test
    void arefusalForLackOfStockArrivesAsTherejectionTypeRatherThanAgenericRemoteFailure() {
        StoreMerchantId store = new StoreMerchantId(STORE_A);
        String ref = String.format("client-short-%d", System.nanoTime());

        // The distinction the order flow turns into "fail the order" rather than "leave it recoverable".
        assertThatThrownBy(() -> inventory.reserve(store, ref, oneOf(SCARCE_SKU, 9_999)))
                .isInstanceOf(ProductReservationRejectedException.class);
    }

    @Test
    void therejectionNamesTheSkuThatBlockedItSoTheShopperCanBeTold() {
        StoreMerchantId store = new StoreMerchantId(STORE_A);
        String ref = String.format("client-named-%d", System.nanoTime());

        assertThatThrownBy(() -> inventory.reserve(store, ref, oneOf(SCARCE_SKU, 9_999)))
                .isInstanceOf(ProductReservationRejectedException.class)
                .hasMessageContaining(SCARCE_SKU);
    }

    private static ProductReservationList oneOf(String sku, int quantity) {
        return new ProductReservationList(Set.of(new ReserveProductEntry(sku, quantity)));
    }

    /**
     * Inventory unreachable is <em>not</em> a decision: nothing was reserved, nothing was refused, and the order
     * has to stay recoverable. It arrives as its own type for exactly that reason, and this is the only place the
     * mapping can be exercised — inventory cannot make itself unreachable to itself except by pointing the client
     * somewhere nothing answers.
     */
    @Test
    void aninventoryThatCannotBeReachedIsNotArefusal() {
        ExternalProductReservationService unreachable = clientFor(deadPort());

        assertThatThrownBy(() -> unreachable.reserve(new StoreMerchantId(STORE_A), UNREACHABLE, oneOf(SKU, 1)))
                .isInstanceOf(InventoryApiUnavailableException.class)
                .isNotInstanceOf(ProductReservationRejectedException.class);
    }

    @Test
    void commitAndReleaseAlsoReportUnreachabilityRatherThanFailingSilently() {
        ExternalProductReservationService unreachable = clientFor(deadPort());
        StoreMerchantId store = new StoreMerchantId(STORE_A);

        assertThatThrownBy(() -> unreachable.commit(store, UNREACHABLE))
                .isInstanceOf(InventoryApiUnavailableException.class);
        assertThatThrownBy(() -> unreachable.release(store, UNREACHABLE))
                .isInstanceOf(InventoryApiUnavailableException.class);
    }

    /**
     * The POST availability form refuses an empty sku list rather than answering an empty result, because a caller
     * asking for nothing is a caller with a bug — {@code @Size(min = 1)} on the body says so. (The
     * {@code skus.isEmpty()} guard inside the service is therefore unreachable over HTTP; it protects in-process
     * callers only.)
     */
    @Test
    void anavailabilityReadForNoSkusIsRefusedRatherThanAnsweredEmpty() {
        ExternalInventoryService reads = WebClientsUtils.build(authorized(),
                String.format(LOCALHOST, port), ExternalInventoryService.class, InventoryApiErrors.INVENTORY);

        assertThatThrownBy(() -> reads.queryBySkus(new StoreMerchantId(STORE_A), new AvailabilityQuery(List.of())))
                .isInstanceOf(UncheckedBaseException.class);
    }

    @Test
    void anavailabilityReadForRealSkusAnswersThroughTheSameClient() {
        ExternalInventoryService reads = WebClientsUtils.build(authorized(),
                String.format(LOCALHOST, port), ExternalInventoryService.class, InventoryApiErrors.INVENTORY);

        assertThat(reads.queryBySkus(new StoreMerchantId(STORE_A), new AvailabilityQuery(List.of(SKU))))
                .isNotEmpty();
    }

    private ExternalProductReservationService clientFor(int somePort) {
        return WebClientsUtils.build(authorized(), String.format(LOCALHOST, somePort),
                ExternalProductReservationService.class, InventoryApiErrors.INVENTORY);
    }

    private RestClient.Builder authorized() {
        String token = new Tokens(signer).s2s(Tokens.SCOPE_STORE_POD, POD);
        return RestClient.builder().defaultHeader(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", token));
    }

    /** A port nothing is listening on, taken and released so the OS is unlikely to hand it out again at once. */
    private static int deadPort() {
        try (java.net.ServerSocket socket = new java.net.ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
