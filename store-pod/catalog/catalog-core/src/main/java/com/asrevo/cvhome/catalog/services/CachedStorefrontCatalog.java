package com.asrevo.cvhome.catalog.services;

import java.time.Duration;
import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.catalog.errors.CategoryFriendlyUrlNotFoundException;
import com.asrevo.cvhome.catalog.errors.CategoryNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductNotFoundException;
import com.asrevo.cvhome.catalog.model.category.ReadableCategory;
import com.asrevo.cvhome.catalog.model.group.ReadableProductGroup;
import com.asrevo.cvhome.catalog.model.manufacturer.ReadableManufacturer;
import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProductSuggestion;
import com.asrevo.cvhome.catalog.services.category.CategoryService;
import com.asrevo.cvhome.catalog.services.group.ProductGroupService;
import com.asrevo.cvhome.catalog.services.manufacturer.ManufacturerService;
import com.asrevo.cvhome.catalog.services.product.ProductSearchService;
import com.asrevo.cvhome.catalog.services.product.ProductService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;

import lombok.RequiredArgsConstructor;

/**
 * The storefront's public catalog reads, each held for {@link #TTL} per store, language and arguments.
 *
 * <p>
 * Every storefront render asks for them and every shopper of a store gets the same answer: a product group (28 % of
 * catalog's requests in the 2026-09-14 load test), the category tree, a category or product by its slug, a category's
 * brands and the search box's suggestions. With nothing cached, catalog spent 90 s of the production mix at its CPU
 * cap. Price and stock are not in any of these answers (inventory owns them), so nothing a shopper pays is served
 * stale. The console's private reads never come through here.
 * </p>
 *
 * <p>
 * A merchant's change clears every cache when it commits (catalog-service's {@code CacheConfig}), so the task that took
 * it serves it at once and another task within {@link #TTL}. An absent category or product is not cached: it throws.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class CachedStorefrontCatalog {

    public static final String GROUP = "CATALOG_GROUP";

    public static final String RELATED = "CATALOG_RELATED";

    public static final String HIERARCHY = "CATALOG_HIERARCHY";

    public static final String CATEGORY = "CATALOG_CATEGORY";

    public static final String BRANDS = "CATALOG_BRANDS";

    public static final String PRODUCT = "CATALOG_PRODUCT";

    public static final String SUGGEST = "CATALOG_SUGGEST";

    public static final List<String> CACHES = List.of(GROUP, RELATED, HIERARCHY, CATEGORY, BRANDS, PRODUCT, SUGGEST);

    public static final Duration TTL = Duration.ofSeconds(30);

    private final ProductGroupService groups;

    private final CategoryService categories;

    private final ManufacturerService manufacturers;

    private final ProductService products;

    private final ProductSearchService search;

    @Cacheable(GROUP)
    public ReadableProductGroup group(StoreMerchantId store, String code, LanguageCode language) {
        return groups.storefront(store, code, language);
    }

    @Cacheable(RELATED)
    public ReadableProductGroup related(StoreMerchantId store, Long productId, LanguageCode language) {
        return groups.related(store, productId, language);
    }

    /** The storefront's tree: shopper languages only; the console's every-language tree is read live. */
    @Cacheable(HIERARCHY)
    public ReadableEntityList<ReadableCategory> hierarchy(StoreMerchantId store, String name, LanguageCode language,
                                                          Pageable pageable) {
        return categories.hierarchy(store, name, language, false, pageable);
    }

    @Cacheable(CATEGORY)
    public ReadableCategory category(StoreMerchantId store, String friendlyUrl, LanguageCode language)
            throws CategoryFriendlyUrlNotFoundException {
        return categories.getByFriendlyUrl(store, friendlyUrl, language);
    }

    @Cacheable(BRANDS)
    public List<ReadableManufacturer> brands(StoreMerchantId store, Long categoryId, LanguageCode language)
            throws CategoryNotFoundException {
        return manufacturers.listByCategory(store, categoryId, language);
    }

    @Cacheable(PRODUCT)
    public ReadableProduct product(StoreMerchantId store, String friendlyUrl, LanguageCode language)
            throws ProductNotFoundException {
        return products.getByFriendlyUrl(store, friendlyUrl, language);
    }

    @Cacheable(SUGGEST)
    public List<ReadableProductSuggestion> suggest(StoreMerchantId store, String query, LanguageCode language,
                                                   int limit) {
        return search.suggest(store, query, language, limit);
    }
}
