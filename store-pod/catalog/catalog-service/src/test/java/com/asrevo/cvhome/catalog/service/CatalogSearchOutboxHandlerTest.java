package com.asrevo.cvhome.catalog.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.asrevo.cvhome.catalog.model.product.event.BrandRenamedEvent;
import com.asrevo.cvhome.catalog.model.product.event.ProductSearchIndexPurgedEvent;
import com.asrevo.cvhome.catalog.model.product.event.ProductSearchIndexStaleEvent;
import com.asrevo.cvhome.catalog.services.product.ProductSearchIndexer;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * Where the search index catches up with the catalogue. Each event has exactly one right reaction, and
 * mixing two of them up is invisible until a shopper searches: a purge treated as a refresh puts back rows a
 * delete was meant to remove.
 */
@ExtendWith(MockitoExtension.class)
class CatalogSearchOutboxHandlerTest {

    private static final String STORE = "65f023632bc46470c104b76f";

    @Mock
    private ProductSearchIndexer indexer;

    @InjectMocks
    private CatalogSearchOutboxHandler handler;

    @Test
    void aStaleProductIsReindexed() {
        handler.handleProductSearchIndexStaleEvent(ProductSearchIndexStaleEvent.from(42L, STORE));

        verify(indexer).reindex(42L);
    }

    @Test
    void aPurgedProductIsRemoved() {
        handler.handleProductSearchIndexPurgedEvent(ProductSearchIndexPurgedEvent.from(42L, STORE));

        verify(indexer).purge(42L);
    }

    @Test
    void aRenamedBrandReindexesItsProductsInItsOwnStore() {
        handler.handleBrandRenamedEvent(BrandRenamedEvent.from(7L, STORE));

        ArgumentCaptor<StoreMerchantId> store = ArgumentCaptor.forClass(StoreMerchantId.class);
        verify(indexer).reindexBrand(org.mockito.ArgumentMatchers.eq(7L), store.capture());
        assertThat(store.getValue().getId()).isEqualTo(STORE);
    }
}
