package com.asrevo.cvhome.cache.event;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.cache.CacheKey;
import com.asrevo.cvhome.cache.CacheRegions;
import com.asrevo.cvhome.cache.CacheRegistry;
import com.asrevo.cvhome.cache.RegionCache;
import com.asrevo.cvhome.cache.Stores;
import com.asrevo.cvhome.cache.TestRegions;
import com.asrevo.cvhome.cache.config.CacheProperties;
import com.asrevo.cvhome.cache.eviction.EvictionRules;
import com.asrevo.cvhome.cache.provider.caffeine.CaffeineCacheProvider;
import com.asrevo.cvhome.commons.domain.ProductId;
import com.asrevo.cvhome.commons.domain.Sku;

import static org.assertj.core.api.Assertions.assertThat;

/** A received event drops the store's entries of the regions its rule names, and nothing else. */
class CacheEventApplierTest {

    private static final String SHOES = "shoes";

    private static final String SKU = "SKU-1";

    private final CacheRegistry registry = new CacheRegistry(CacheRegions.of(TestRegions.values()),
            List.of(new CaffeineCacheProvider()), CacheProperties.defaults());

    private final RegionCache<String> product = registry.region(TestRegions.PRODUCT, String.class);

    private final RegionCache<String> listing = registry.region(TestRegions.LISTING, String.class);

    private final CacheEventApplier applier = new CacheEventApplier(EvictionRules.onEvents()
            .onEvent(StockChanged.class, PriceChanged.class).evict(TestRegions.PRODUCT).build(), registry);

    @Test
    void aMappedEventDropsTheStoresEntriesOfItsRegionsOnly() {
        CacheKey keyA = CacheKey.sku(Stores.A, Stores.EN, new Sku(SKU));
        CacheKey keyB = CacheKey.sku(Stores.B, Stores.EN, new Sku(SKU));
        product.put(keyA, SHOES);
        product.put(keyB, SHOES);
        listing.put(keyA, SHOES);

        assertThat(applier.apply(new StockChanged(Stores.A, new Sku(SKU)))).containsExactly(TestRegions.PRODUCT);

        assertThat(product.getIfPresent(keyA)).isEmpty();
        assertThat(product.getIfPresent(keyB)).contains(SHOES);
        assertThat(listing.getIfPresent(keyA)).contains(SHOES);
    }

    @Test
    void anEventNoRuleNamesDropsNothing() {
        CacheKey keyA = CacheKey.sku(Stores.A, Stores.EN, new Sku(SKU));
        product.put(keyA, SHOES);

        assertThat(applier.apply(new ProductChanged(Stores.A, new ProductId(1L)))).isEmpty();

        assertThat(product.getIfPresent(keyA)).contains(SHOES);
    }
}
