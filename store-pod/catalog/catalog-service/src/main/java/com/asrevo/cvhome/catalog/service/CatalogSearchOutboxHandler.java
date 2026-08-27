package com.asrevo.cvhome.catalog.service;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.catalog.model.product.event.BrandRenamedEvent;
import com.asrevo.cvhome.catalog.model.product.event.ProductSearchIndexPurgedEvent;
import com.asrevo.cvhome.catalog.model.product.event.ProductSearchIndexStaleEvent;
import com.asrevo.cvhome.catalog.services.product.ProductSearchIndexer;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import io.namastack.outbox.annotation.OutboxHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Where the search index catches up with the catalogue.
 *
 * <p>
 * The events were written to the outbox in the same transaction as the change that caused them, so nothing is lost
 * if this service dies between the two. What it costs is immediacy: a merchant who saves a product does not find
 * it by search until the poller has been round, a second or two later.
 * </p>
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CatalogSearchOutboxHandler {

    private final ProductSearchIndexer indexer;

    @OutboxHandler
    public void handleProductSearchIndexStaleEvent(ProductSearchIndexStaleEvent event) {
        log.debug("reindexing product {} of store {}", event.productId(), event.storeId());
        indexer.reindex(event.productId());
    }

    @OutboxHandler
    public void handleProductSearchIndexPurgedEvent(ProductSearchIndexPurgedEvent event) {
        log.debug("purging product {} of store {} from the search index", event.productId(), event.storeId());
        indexer.purge(event.productId());
    }

    @OutboxHandler
    public void handleBrandRenamedEvent(BrandRenamedEvent event) {
        indexer.reindexBrand(event.manufacturerId(), new StoreMerchantId(event.storeId()));
    }
}
