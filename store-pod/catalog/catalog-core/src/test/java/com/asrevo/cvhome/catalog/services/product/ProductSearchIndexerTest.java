package com.asrevo.cvhome.catalog.services.product;

import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.asrevo.cvhome.catalog.repositories.ProductSearchIndexRepository;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * What the outbox handler asks for, and what a brand rename costs.
 *
 * <p>
 * A brand can carry a whole catalogue, so the rename walks it in batches rather than holding one transaction
 * open across all of it. That batching is the only thing here that is not a one-line delegation, and it is
 * the thing that would quietly become a single huge transaction if someone simplified it.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class ProductSearchIndexerTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    private static final int BATCH = 200;

    @Mock
    private ProductSearchIndexRepository searchIndexRepository;

    @InjectMocks
    private ProductSearchIndexer indexer;

    @Test
    void reindexRefreshesOneProduct() {
        indexer.reindex(42L);

        verify(searchIndexRepository).refresh(42L);
    }

    @Test
    void purgeRemovesOneProduct() {
        indexer.purge(42L);

        verify(searchIndexRepository).purge(42L);
    }

    @Test
    void rebuildDelegatesToTheStoreWideFunction() {
        when(searchIndexRepository.rebuildStore(STORE.getId())).thenReturn(45);

        indexer.rebuild(STORE);

        verify(searchIndexRepository).rebuildStore(STORE.getId());
    }

    @Test
    void aBrandRenameRefreshesEveryProductCarryingIt() {
        when(searchIndexRepository.productIdsForBrand(7L, STORE.getId())).thenReturn(List.of(1L, 2L, 3L));

        indexer.reindexBrand(7L, STORE);

        verify(searchIndexRepository).refresh(1L);
        verify(searchIndexRepository).refresh(2L);
        verify(searchIndexRepository).refresh(3L);
    }

    /**
     * More products than a batch holds must still all be refreshed, and exactly once each.
     */
    @Test
    void aLargeBrandIsWalkedInBatchesWithNothingDroppedOrRepeated() {
        List<Long> ids = IntStream.rangeClosed(1, BATCH * 2 + 37).mapToObj(Long::valueOf).toList();
        when(searchIndexRepository.productIdsForBrand(7L, STORE.getId())).thenReturn(ids);

        indexer.reindexBrand(7L, STORE);

        verify(searchIndexRepository, times(ids.size())).refresh(anyLong());
        verify(searchIndexRepository).refresh(1L);
        verify(searchIndexRepository).refresh((long) ids.size());
    }

    @Test
    void aBrandWithNoProductsRefreshesNothing() {
        when(searchIndexRepository.productIdsForBrand(7L, STORE.getId())).thenReturn(List.of());

        indexer.reindexBrand(7L, STORE);

        verify(searchIndexRepository, never()).refresh(anyLong());
    }
}
