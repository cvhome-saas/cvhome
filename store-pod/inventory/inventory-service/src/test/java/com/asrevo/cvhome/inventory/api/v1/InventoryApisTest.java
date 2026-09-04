package com.asrevo.cvhome.inventory.api.v1;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.model.AvailabilityQuery;
import com.asrevo.cvhome.inventory.model.PersistableInventory;
import com.asrevo.cvhome.inventory.model.PersistableInventoryBatch;
import com.asrevo.cvhome.inventory.model.PersistableSkuInventory;
import com.asrevo.cvhome.inventory.model.SkuInventory;
import com.asrevo.cvhome.inventory.services.InventoryService;
import com.asrevo.cvhome.inventory.services.ReservationService;
import com.asrevo.cvhome.store.core.model.catalog.ProductReservationList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The inventory endpoints: that each one passes the store through unchanged, and that the private ones are gated.
 *
 * <p>
 * The delegation assertions are thin by design — these controllers are one line each — but the store argument is
 * not incidental. Every one of these methods takes a {@link StoreMerchantId} resolved from the {@code store} query
 * parameter, and a controller that dropped it or substituted its own would read another merchant's stock through an
 * endpoint that still looked correct.
 * </p>
 *
 * <p>
 * The annotation test is the one that earns its place. A missing {@code @PreAuthorize} on a private endpoint is
 * invisible at runtime until somebody without the token succeeds, and it is on the reject-on-sight list in
 * AGENTS.md precisely because review keeps missing it.
 * </p>
 */
class InventoryApisTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");
    private static final String SKU = "SKU-1";
    private static final String REF = "order-1";
    private static final String MANAGE = "STORE-POD.INVENTORY.*";
    private static final String RESERVE = "STORE-POD.INVENTORY.RESERVE";

    private final InventoryService inventoryService = Mockito.mock(InventoryService.class);
    private final ReservationService reservationService = Mockito.mock(ReservationService.class);
    private final InventoryApi inventoryApi = new InventoryApi(inventoryService);
    private final ExternalInventoryApi externalInventoryApi = new ExternalInventoryApi(inventoryService);
    private final ExternalProductReservationApi reservationApi =
            new ExternalProductReservationApi(reservationService);

    private static SkuInventory sku() {
        return new SkuInventory(SKU, 1L, true, true, 5, 1, 0, null);
    }

    @Test
    void readingByProductIdsPassesTheStoreAndTheIdsThrough() {
        when(inventoryService.getByProductIds(STORE, List.of(1L))).thenReturn(List.of(sku()));

        assertThat(inventoryApi.getByProducts(List.of(1L), STORE)).containsExactly(sku());
        verify(inventoryService).getByProductIds(STORE, List.of(1L));
    }

    @Test
    void aBulkUpsertForwardsTheBatchEntriesRatherThanTheWrapper() {
        PersistableInventory entry = new PersistableInventory(1L, 5, true, 1, 0, null);
        PersistableInventoryBatch batch =
                new PersistableInventoryBatch(List.of(new PersistableSkuInventory(SKU, entry)));
        when(inventoryService.bulkUpsert(eq(STORE), any())).thenReturn(List.of(sku()));

        assertThat(inventoryApi.bulkUpsert(batch, STORE)).hasSize(1);
        verify(inventoryService).bulkUpsert(STORE, batch.entries());
    }

    @Test
    void aSingleUpsertIsKeyedBySkuWithinTheStore() {
        PersistableInventory body = new PersistableInventory(1L, 5, true, 1, 0, null);
        when(inventoryService.upsert(STORE, SKU, body)).thenReturn(sku());

        assertThat(inventoryApi.upsert(SKU, body, STORE)).isEqualTo(sku());
        verify(inventoryService).upsert(STORE, SKU, body);
    }

    @Test
    void bothDeletesAreScopedToTheStore() {
        inventoryApi.deleteByProduct(1L, STORE);
        inventoryApi.deleteBySku(SKU, STORE);

        verify(inventoryService).deleteByProduct(STORE, 1L);
        verify(inventoryService).deleteBySku(STORE, SKU);
    }

    @Test
    void theStorefrontAvailabilityReadsBothAcceptSkusAndAnswerTheSameWay() {
        when(inventoryService.getBySkus(STORE, List.of(SKU))).thenReturn(List.of(sku()));

        assertThat(externalInventoryApi.getBySkus(STORE, List.of(SKU))).containsExactly(sku());
        assertThat(externalInventoryApi.queryBySkus(STORE, new AvailabilityQuery(List.of(SKU))))
                .containsExactly(sku());
        verify(inventoryService, Mockito.times(2)).getBySkus(STORE, List.of(SKU));
    }

    @Test
    void reserveCommitAndReleaseAllCarryTheOrderReference() throws Exception {
        ProductReservationList lines = new ProductReservationList(Set.of());

        reservationApi.reserve(STORE, REF, lines);
        reservationApi.commit(STORE, REF);
        reservationApi.release(STORE, REF);

        verify(reservationService).reserve(STORE, REF, lines);
        verify(reservationService).commit(STORE, REF);
        verify(reservationService).release(STORE, REF);
    }

    private static Stream<Method> gatedEndpoints() {
        return Stream.concat(Stream.of(InventoryApi.class.getDeclaredMethods()),
                        Stream.of(ExternalProductReservationApi.class.getDeclaredMethods()))
                .filter(m -> m.isAnnotationPresent(org.springframework.web.bind.annotation.GetMapping.class)
                        || m.isAnnotationPresent(org.springframework.web.bind.annotation.PutMapping.class)
                        || m.isAnnotationPresent(org.springframework.web.bind.annotation.PostMapping.class)
                        || m.isAnnotationPresent(org.springframework.web.bind.annotation.DeleteMapping.class))
                .sorted((a, b) -> a.getName().compareTo(b.getName()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("gatedEndpoints")
    void everyPrivateEndpointCarriesItsPermissionToken(Method endpoint) {
        PreAuthorize gate = endpoint.getAnnotation(PreAuthorize.class);

        assertThat(gate).as("%s.%s has no @PreAuthorize", endpoint.getDeclaringClass().getSimpleName(),
                endpoint.getName()).isNotNull();
        assertThat(gate.value()).contains("hasPermission(#merchantStore,'StoreMerchantId'")
                .containsAnyOf(MANAGE, RESERVE);
    }

    @Test
    void theStorefrontReadsAreDeliberatelyUngatedAndLiveOnTheirOwnPath() {
        // ExternalInventoryApi answers the shopper-facing availability calls, so it is public by design; keeping it
        // on /api/v1 rather than /api/v1/private is what says so.
        assertThat(Stream.of(ExternalInventoryApi.class.getDeclaredMethods())
                .anyMatch(m -> m.isAnnotationPresent(PreAuthorize.class))).isFalse();
        assertThat(ExternalInventoryApi.class.getAnnotation(RequestMapping.class).value())
                .containsExactly("/api/v1");
        assertThat(InventoryApi.class.getAnnotation(RequestMapping.class).value())
                .containsExactly("/api/v1/private/inventory");
    }
}
