package com.asrevo.cvhome.catalog.reads;

import java.time.Duration;
import java.util.List;

import com.asrevo.cvhome.cache.CacheRegion;
import com.asrevo.cvhome.catalog.model.category.ReadableCategory;
import com.asrevo.cvhome.catalog.model.group.ReadableProductGroup;
import com.asrevo.cvhome.catalog.model.product.ReadableCartLineProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableMinimalProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProductSearchResult;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;

/**
 * What catalog caches: the storefront's public reads, a minute each per store, and the two per-sku reads checkout
 * makes on every cart operation. The console's private reads are never cached.
 */
public enum CatalogRegions implements CacheRegion {

    GROUP(Names.GROUP, ReadableProductGroup.class, 2_000),
    RELATED(Names.RELATED, ReadableProductGroup.class, 5_000),
    HIERARCHY(Names.HIERARCHY, ReadableEntityList.class, 1_000),
    CATEGORY(Names.CATEGORY, ReadableCategory.class, 5_000),
    BRANDS(Names.BRANDS, List.class, 2_000),
    PRODUCT(Names.PRODUCT, ReadableProduct.class, 10_000),
    LISTING(Names.LISTING, ReadableEntityList.class, 5_000),
    SEARCH(Names.SEARCH, ReadableProductSearchResult.class, 5_000),
    SUGGEST(Names.SUGGEST, List.class, 5_000),
    CART_LINE(Names.CART_LINE, ReadableCartLineProduct.class, 20_000),
    DETAILED_PRODUCT(Names.DETAILED_PRODUCT, ReadableMinimalProduct.class, 10_000);

    private static final Duration TTL = Duration.ofSeconds(60);

    private final String name;

    private final Class<?> valueType;

    private final long maxSize;

    CatalogRegions(String name, Class<?> valueType, long maxSize) {
        this.name = name;
        this.valueType = valueType;
        this.maxSize = maxSize;
    }

    @Override
    public String regionName() {
        return name;
    }

    @Override
    public Class<?> valueType() {
        return valueType;
    }

    @Override
    public Duration ttl() {
        return TTL;
    }

    @Override
    public long maxSize() {
        return maxSize;
    }

    /** The names, as {@code @Cacheable} needs a constant. */
    public static final class Names {

        public static final String GROUP = "catalog.group";

        public static final String RELATED = "catalog.related";

        public static final String HIERARCHY = "catalog.hierarchy";

        public static final String CATEGORY = "catalog.category";

        public static final String BRANDS = "catalog.brands";

        public static final String PRODUCT = "catalog.product";

        public static final String LISTING = "catalog.listing";

        public static final String SEARCH = "catalog.search";

        public static final String SUGGEST = "catalog.suggest";

        public static final String CART_LINE = "catalog.cart-line";

        public static final String DETAILED_PRODUCT = "catalog.detailed-product";

        private Names() {
        }
    }
}
