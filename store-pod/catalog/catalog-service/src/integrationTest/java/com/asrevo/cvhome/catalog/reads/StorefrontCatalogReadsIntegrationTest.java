package com.asrevo.cvhome.catalog.reads;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

import com.asrevo.cvhome.cache.QueryKey;
import com.asrevo.cvhome.catalog.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.catalog.model.group.PersistableProductGroup;
import com.asrevo.cvhome.catalog.model.product.ProductFilter;
import com.asrevo.cvhome.catalog.model.product.ProductSearchCriteria;
import com.asrevo.cvhome.catalog.services.group.ProductGroupService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.testsupport.annotations.StorageIntegrationTest;
import com.asrevo.cvhome.testsupport.security.Tokens;
import com.asrevo.cvhome.testsupport.sql.SqlStatements;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The storefront reads on a real catalogue: a second read of each costs no statement, a merchant's committed write
 * drops their own store's entries and leaves the other store's warm, and two requests that mean the same listing
 * share one entry.
 */
@StorageIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class StorefrontCatalogReadsIntegrationTest {

    private static final StoreMerchantId STORE = new StoreMerchantId(Tokens.STORE_1);

    private static final StoreMerchantId OTHER_STORE = new StoreMerchantId(Tokens.STORE_2);

    private static final LanguageCode EN = new LanguageCode("en");

    private static final String FEATURED = "FEATURED_ITEMS";

    private static final String TYPED = "run";

    @Autowired
    private StorefrontCatalogReads reads;

    @Autowired
    private ProductGroupService groups;

    private static QueryKey<ProductFilter> everything() {
        ProductFilter filter = new ProductFilter();
        return QueryKey.of(filter.normalised(), filter);
    }

    private static QueryKey<ProductSearchCriteria> rail() {
        ProductSearchCriteria criteria = new ProductSearchCriteria();
        criteria.setRows(false);
        return QueryKey.of(criteria.normalised(), criteria);
    }

    @Test
    void aSecondReadCostsNoStatementAndAMerchantsWriteDropsTheirStoresEntriesAlone() throws Exception {
        reads.group(STORE, EN, FEATURED);
        reads.hierarchy(STORE, EN, PageRequest.of(0, 20));
        reads.list(STORE, EN, everything(), PageRequest.of(0, 15));
        reads.search(STORE, EN, rail(), PageRequest.of(0, 15));
        reads.suggest(STORE, EN, SuggestQuery.of(TYPED, 8));
        reads.group(OTHER_STORE, EN, FEATURED);

        SqlStatements.Recorded<Object> cached = SqlStatements.during(() -> {
            reads.group(STORE, EN, FEATURED);
            reads.hierarchy(STORE, EN, PageRequest.of(0, 20));
            reads.list(STORE, EN, everything(), PageRequest.of(0, 15));
            reads.search(STORE, EN, rail(), PageRequest.of(0, 15));
            return reads.suggest(STORE, EN, SuggestQuery.of(" Run", 8));
        });
        assertThat(cached.count()).as(cached.toString()).isZero();

        PersistableProductGroup group = new PersistableProductGroup();
        group.setCode(String.format("CACHE-%s", UUID.randomUUID().toString().substring(0, 8)));
        groups.save(STORE, group);

        SqlStatements.Recorded<Object> afterWrite = SqlStatements.during(() -> reads.group(STORE, EN, FEATURED));
        assertThat(afterWrite.count()).as("the store that wrote reads the database again").isPositive();
        SqlStatements.Recorded<Object> otherStore = SqlStatements.during(() -> reads.group(OTHER_STORE, EN, FEATURED));
        assertThat(otherStore.count()).as("another store's entries stayed warm").isZero();
    }
}
