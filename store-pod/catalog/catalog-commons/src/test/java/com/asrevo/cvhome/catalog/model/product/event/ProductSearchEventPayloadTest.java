package com.asrevo.cvhome.catalog.model.product.event;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What actually goes into the outbox row.
 *
 * <p>
 * The record is serialised into the row's payload and its {@code eventType} is how the handler is matched, so
 * both are part of the wire contract rather than incidental. {@code data()} is what shows up in the log when
 * someone is working out why an index is stale.
 * </p>
 */
class ProductSearchEventPayloadTest {

    private static final String STORE = "65f023632bc46470c104b76f";

    private static final long PRODUCT_ID = 42L;

    private static final String PRODUCT_KEY = "42";

    private static final long BRAND_ID = 7L;

    private static final String BRAND_KEY = "7";

    private static final String PRODUCT_FIELD = "productId";

    private static final String STORE_FIELD = "storeId";

    @Test
    void aStaleEventNamesItselfAndCarriesTheProductAndStore() {
        ProductSearchIndexStaleEvent event = ProductSearchIndexStaleEvent.from(PRODUCT_ID, STORE);

        assertThat(event.eventType()).isEqualTo("ProductSearchIndexStaleEvent");
        assertThat(event.partitionKey()).isEqualTo(PRODUCT_KEY);
        assertThat(event.data()).containsEntry(PRODUCT_FIELD, PRODUCT_KEY).containsEntry(STORE_FIELD, STORE);
    }

    @Test
    void aPurgedEventNamesItselfAndCarriesTheProductAndStore() {
        ProductSearchIndexPurgedEvent event = ProductSearchIndexPurgedEvent.from(PRODUCT_ID, STORE);

        assertThat(event.eventType()).isEqualTo("ProductSearchIndexPurgedEvent");
        assertThat(event.partitionKey()).isEqualTo(PRODUCT_KEY);
        assertThat(event.data()).containsEntry(PRODUCT_FIELD, PRODUCT_KEY).containsEntry(STORE_FIELD, STORE);
    }

    @Test
    void aBrandRenameNamesItselfAndCarriesTheBrandAndStore() {
        BrandRenamedEvent event = BrandRenamedEvent.from(BRAND_ID, STORE);

        assertThat(event.eventType()).isEqualTo("BrandRenamedEvent");
        assertThat(event.partitionKey()).isEqualTo(BRAND_KEY);
        assertThat(event.data()).containsEntry("manufacturerId", BRAND_KEY).containsEntry(STORE_FIELD, STORE);
    }

    /**
     * A product and a brand can share an id, and their events must still land in different partitions —
     * otherwise a busy brand would serialise reindexing for an unrelated product.
     */
    @Test
    void aProductAndABrandWithTheSameIdAreStillDifferentEvents() {
        assertThat(ProductSearchIndexStaleEvent.from(BRAND_ID, STORE).eventType())
                .isNotEqualTo(BrandRenamedEvent.from(BRAND_ID, STORE).eventType());
    }
}
