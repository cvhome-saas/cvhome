package com.asrevo.cvhome.catalog.entity;

import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.asrevo.cvhome.catalog.model.product.event.ProductSearchIndexPurgedEvent;
import com.asrevo.cvhome.catalog.model.product.event.ProductSearchIndexStaleEvent;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A product that changed has to tell the search index, and it does so through the outbox. An edit that registers
 * no event is not a visible failure — the product simply stops being findable under its new name — so the
 * registration is asserted here rather than left to an integration test to notice.
 */
class ProductSearchEventsTest {

    private static final Long PRODUCT_ID = 42L;

    private static final String STORE = "65f023632bc46470c104b76f";

    private static final String PARTITION_KEY = "42";

    private static Product product() {
        Product product = new Product();
        product.setId(PRODUCT_ID);
        product.setStore(new StoreMerchantId(STORE));
        return product;
    }

    /**
     * {@code AbstractAggregateRoot.domainEvents()} is protected, and Spring Data reads it reflectively after the
     * repository call — which is exactly the moment this reproduces.
     */
    private static Collection<?> events(Product product) {
        return ReflectionTestUtils.invokeMethod(product, "domainEvents");
    }

    @Test
    void anUntouchedProductAnnouncesNothing() {
        assertThat(events(product())).isEmpty();
    }

    @Test
    void aChangedProductAnnouncesThatItsIndexIsStale() {
        Product product = product().searchIndexStale();

        assertThat(events(product)).singleElement()
                .isInstanceOfSatisfying(ProductSearchIndexStaleEvent.class, event -> {
                    assertThat(event.productId()).isEqualTo(PRODUCT_ID);
                    assertThat(event.storeId()).isEqualTo(STORE);
                });
    }

    @Test
    void aDeletedProductAnnouncesThatItsIndexRowsMustGo() {
        Product product = product().searchIndexPurged();

        assertThat(events(product)).singleElement()
                .isInstanceOfSatisfying(ProductSearchIndexPurgedEvent.class, event -> {
                    assertThat(event.productId()).isEqualTo(PRODUCT_ID);
                    assertThat(event.storeId()).isEqualTo(STORE);
                });
    }

    /**
     * Marking a product stale twice — an update that also moves a category, say — is still one refresh.
     */
    @Test
    void markingStaleTwiceAnnouncesOnce() {
        assertThat(events(product().searchIndexStale().searchIndexStale())).hasSize(1);
    }

    /**
     * A delete wins over a pending refresh: reindexing a product that is being removed would put back rows the
     * purge is there to take away.
     */
    @Test
    void aPurgeWinsOverAStaleMark() {
        Product product = product().searchIndexStale().searchIndexPurged();

        assertThat(events(product)).singleElement().isInstanceOf(ProductSearchIndexPurgedEvent.class);
    }

    /**
     * The event carries the id at publication time, not at the moment it was asked for — which is what lets a
     * newly created product, whose id only exists after the insert, be saved once rather than twice.
     */
    @Test
    void theEventPicksUpAnIdAssignedAfterTheMark() {
        Product product = new Product();
        product.setStore(new StoreMerchantId(STORE));
        product.searchIndexStale();

        product.setId(PRODUCT_ID);

        assertThat(events(product)).singleElement()
                .isInstanceOfSatisfying(ProductSearchIndexStaleEvent.class,
                        event -> assertThat(event.productId()).isEqualTo(PRODUCT_ID));
    }

    /**
     * The outbox partitions on a string. A key expression that returned the raw id would be rejected at publish
     * time, and the first thing to break would be creating a product at all.
     */
    @Test
    void thePartitionKeyIsTheProductIdAsAString() {
        assertThat(ProductSearchIndexStaleEvent.from(PRODUCT_ID, STORE).partitionKey()).isEqualTo(PARTITION_KEY);
        assertThat(ProductSearchIndexPurgedEvent.from(PRODUCT_ID, STORE).partitionKey())
                .isEqualTo(PARTITION_KEY);
    }
}
