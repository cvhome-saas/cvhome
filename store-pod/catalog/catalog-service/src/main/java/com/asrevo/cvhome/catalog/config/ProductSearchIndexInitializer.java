package com.asrevo.cvhome.catalog.config;

import java.util.List;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.catalog.repositories.ProductSearchIndexRepository;
import com.asrevo.cvhome.catalog.services.product.ProductSearchIndexer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Indexes anything the catalogue holds that the search index has never seen.
 *
 * <p>
 * The index normally keeps itself current from domain events, so on a running system this finds nothing and
 * costs one indexed query. It exists for the two cases events cannot cover: a catalogue that predates the
 * feature, and a database seeded through {@code spring.sql.init}, whose rows are inserted after
 * {@code schema.sql} has already run.
 * </p>
 *
 * <p>
 * Deliberately additive. It never rebuilds a product that is already indexed — that is what the private rebuild
 * endpoint is for, and doing it here would mean re-reading every catalogue in the pod on every restart.
 * </p>
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ProductSearchIndexInitializer {

    private static final int BATCH = 200;

    private final ProductSearchIndexRepository searchIndexRepository;

    private final ProductSearchIndexer indexer;

    @EventListener(ApplicationReadyEvent.class)
    public void indexWhatIsMissing() {
        List<Long> missing = searchIndexRepository.productIdsMissingFromIndex();
        if (missing.isEmpty()) {
            return;
        }
        log.info("indexing {} products the product search index has never seen", missing.size());
        for (int from = 0; from < missing.size(); from += BATCH) {
            indexer.reindexBatch(missing.subList(from, Math.min(from + BATCH, missing.size())));
        }
    }
}
