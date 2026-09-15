package com.asrevo.cvhome.catalog.services;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.cache.StoreScopedKeyGenerator;
import com.asrevo.cvhome.catalog.errors.CategoryFriendlyUrlNotFoundException;
import com.asrevo.cvhome.catalog.errors.CategoryNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductNotFoundException;
import com.asrevo.cvhome.catalog.model.category.ReadableCategory;
import com.asrevo.cvhome.catalog.model.group.ReadableProductGroup;
import com.asrevo.cvhome.catalog.model.manufacturer.ReadableManufacturer;
import com.asrevo.cvhome.catalog.model.product.ProductFilter;
import com.asrevo.cvhome.catalog.model.product.ProductSearchCriteria;
import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProductSearchResult;
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
 * brands, the listing and the search results page, and the search box's suggestions. With nothing cached, catalog
 * spent 90 s of the production mix at its CPU cap; with everything but the listing and the search cached it was still
 * pinned at 99 % of its cap in the re-run, those two being the reads left. Price and stock are not in any of these
 * answers (inventory owns them), so nothing a shopper pays is served stale. The console's product table reads the same
 * listing, so a merchant on another task than the one that took their save sees it within {@link #TTL}.
 * </p>
 *
 * <p>
 * Keys are {@link StoreScopedKeyGenerator store-scoped}: a merchant's change drops their own store's entries when it
 * commits (catalog-service's {@code CacheConfig}) and leaves every other store's warm. An absent category or product
 * is not cached: it throws. The suggest key is the typed text lowered, trimmed and cut at {@link #SUGGEST_KEY_LENGTH},
 * so one shopper's typing cannot fill the cache with keys nobody else will hit.
 * </p>
 */
@Component
@CacheConfig(keyGenerator = StoreScopedKeyGenerator.BEAN)
@RequiredArgsConstructor
public class CachedStorefrontCatalog {

    public static final String GROUP = "CATALOG_GROUP";

    public static final String RELATED = "CATALOG_RELATED";

    public static final String HIERARCHY = "CATALOG_HIERARCHY";

    public static final String CATEGORY = "CATALOG_CATEGORY";

    public static final String BRANDS = "CATALOG_BRANDS";

    public static final String PRODUCT = "CATALOG_PRODUCT";

    public static final String LISTING = "CATALOG_LISTING";

    public static final String SEARCH = "CATALOG_SEARCH";

    public static final String SUGGEST = "CATALOG_SUGGEST";

    /** Checkout's cart-line read, one entry per sku ({@link CachedCartLines}). */
    public static final String CART_LINE = "CATALOG_CART_LINE";

    public static final List<String> CACHES = List.of(GROUP, RELATED, HIERARCHY, CATEGORY, BRANDS, PRODUCT, LISTING,
            SEARCH, SUGGEST, CART_LINE);

    public static final Duration TTL = Duration.ofSeconds(60);

    /** Longer than any product name a shopper types before the suggestions have answered. */
    public static final int SUGGEST_KEY_LENGTH = 64;

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

    /** The category page's listing, keyed by its filter and page: what the re-run's shoppers hit catalog with most. */
    @Cacheable(LISTING)
    public ReadableEntityList<ReadableProduct> list(StoreMerchantId store, ProductFilter filter, LanguageCode language,
                                                    Pageable pageable) {
        return products.list(store, filter, language, pageable);
    }

    /** The results page with its rail, and the category rail alone ({@code rows=false}), keyed by the criteria. */
    @Cacheable(SEARCH)
    public ReadableProductSearchResult search(StoreMerchantId store, ProductSearchCriteria criteria,
                                              LanguageCode language, Pageable pageable) {
        return search.search(store, criteria, language, pageable);
    }

    /** The typed text as the suggest cache keys it: lowered, trimmed and cut at {@link #SUGGEST_KEY_LENGTH}. */
    public static String suggestKey(String query) {
        String typed = query == null ? "" : query.strip().toLowerCase(Locale.ROOT);
        return typed.length() > SUGGEST_KEY_LENGTH ? typed.substring(0, SUGGEST_KEY_LENGTH) : typed;
    }

    /** The limit the suggest cache keys by: what the search would cap it to anyway. */
    public static int suggestLimit(int limit) {
        return Math.clamp(limit, 1, ProductSearchService.MAX_SUGGESTIONS);
    }

    /** Called with {@link #suggestKey} and {@link #suggestLimit}: a raw query would key a cache entry per keystroke. */
    @Cacheable(SUGGEST)
    public List<ReadableProductSuggestion> suggest(StoreMerchantId store, String query, LanguageCode language,
                                                   int limit) {
        return search.suggest(store, query, language, limit);
    }
}
