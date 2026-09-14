package com.asrevo.cvhome.catalog.services.product;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.cache.EntityCommitCacheEviction;
import com.asrevo.cvhome.catalog.repositories.ProductSearchIndexRepository;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Applies the index work the outbox events ask for.
 *
 * <p>
 * Everything here is idempotent, because an outbox record can be delivered more than once: a refresh replaces the
 * product's rows outright, and a purge of a product that is already gone deletes nothing.
 * </p>
 *
 * <p>
 * The index is a derived table refreshed natively, so Hibernate reports no entity write for it, and the product's
 * own write dropped the store's cached reads before the outbox got here: a search or suggest cached since would hold
 * the old index for a whole time-to-live. Every refresh drops the store's entries again once it has committed.
 * </p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProductSearchIndexer {

    /**
     * How many products a brand rename refreshes per transaction. A brand can carry a whole catalogue, and one
     * transaction around all of it would hold locks for as long as it took.
     */
    private static final int BRAND_BATCH = 200;

    private final ProductSearchIndexRepository searchIndexRepository;

    private final EntityCommitCacheEviction caches;

    @Transactional
    public void reindex(Long productId, StoreMerchantId store) {
        searchIndexRepository.refresh(productId);
        caches.evictAfterCommit(store);
    }

    @Transactional
    public void purge(Long productId, StoreMerchantId store) {
        searchIndexRepository.purge(productId);
        caches.evictAfterCommit(store);
    }

    /**
     * Rebuild every product carrying a brand, a batch per transaction.
     */
    public void reindexBrand(Long manufacturerId, StoreMerchantId store) {
        List<Long> productIds = searchIndexRepository.productIdsForBrand(manufacturerId, store.getId());
        log.info("brand {} renamed in store {}; reindexing {} products", manufacturerId, store.getId(),
                productIds.size());
        for (int from = 0; from < productIds.size(); from += BRAND_BATCH) {
            reindexBatch(productIds.subList(from, Math.min(from + BRAND_BATCH, productIds.size())));
        }
        caches.evictAfterCommit(store);
    }

    @Transactional
    public void reindexBatch(List<Long> productIds) {
        productIds.forEach(searchIndexRepository::refresh);
    }

    /**
     * Rebuild a whole store. Behind the private rebuild endpoint, and what to run after the document's shape
     * changes in {@code schema.sql}.
     */
    @Transactional
    public void rebuild(StoreMerchantId store) {
        int rows = searchIndexRepository.rebuildStore(store.getId());
        log.info("rebuilt the product search index for store {}: {} rows", store.getId(), rows);
        caches.evictAfterCommit(store);
    }
}
