package com.asrevo.cvhome.catalog.services.product;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public void reindex(Long productId) {
        searchIndexRepository.refresh(productId);
    }

    @Transactional
    public void purge(Long productId) {
        searchIndexRepository.purge(productId);
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
    }
}
