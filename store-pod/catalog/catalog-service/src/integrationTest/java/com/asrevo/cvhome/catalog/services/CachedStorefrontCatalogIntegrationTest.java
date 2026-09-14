package com.asrevo.cvhome.catalog.services;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

import com.asrevo.cvhome.catalog.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.catalog.model.group.PersistableProductGroup;
import com.asrevo.cvhome.catalog.services.group.ProductGroupService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.testsupport.annotations.StorageIntegrationTest;
import com.asrevo.cvhome.testsupport.security.Tokens;
import com.asrevo.cvhome.testsupport.sql.SqlStatements;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The storefront's catalog reads come from the cache on a second read, and a merchant's write clears it.
 *
 * <p>
 * Nothing was cached: every render asked catalog again for the same group, tree and suggestions, and catalog spent 90 s
 * of the 2026-09-14 production mix at its CPU cap. Same context as the other storage tests.
 * </p>
 */
@StorageIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class CachedStorefrontCatalogIntegrationTest {

    private static final StoreMerchantId STORE = new StoreMerchantId(Tokens.STORE_1);

    private static final LanguageCode EN = new LanguageCode("en");

    private static final String FEATURED = "FEATURED_ITEMS";

    private static final String TYPED = "run";

    @Autowired
    private CachedStorefrontCatalog storefront;

    @Autowired
    private ProductGroupService groups;

    @Test
    void aSecondReadCostsNoStatementAndAMerchantsWriteClearsTheCache() throws Exception {
        storefront.group(STORE, FEATURED, EN);
        storefront.hierarchy(STORE, null, EN, PageRequest.of(0, 20));
        storefront.suggest(STORE, TYPED, EN, 8);

        SqlStatements.Recorded<Object> cached = SqlStatements.during(() -> {
            storefront.group(STORE, FEATURED, EN);
            storefront.hierarchy(STORE, null, EN, PageRequest.of(0, 20));
            return storefront.suggest(STORE, TYPED, EN, 8);
        });
        assertThat(cached.count()).as(cached.toString()).isZero();

        PersistableProductGroup group = new PersistableProductGroup();
        group.setCode(String.format("CACHE-%s", UUID.randomUUID().toString().substring(0, 8)));
        groups.save(STORE, group);

        SqlStatements.Recorded<Object> afterWrite = SqlStatements.during(() -> storefront.group(STORE, FEATURED, EN));
        assertThat(afterWrite.count()).isPositive();
    }
}
