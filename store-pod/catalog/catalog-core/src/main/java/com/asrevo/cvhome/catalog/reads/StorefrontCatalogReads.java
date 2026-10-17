package com.asrevo.cvhome.catalog.reads;

import java.util.List;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.cache.QueryKey;
import com.asrevo.cvhome.cache.spring.StoreScopedKeyGenerator;
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
import com.asrevo.cvhome.commons.domain.CategoryId;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.ProductId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;

import lombok.RequiredArgsConstructor;

/**
 * The storefront's public catalog reads, each held for a minute per store, language and arguments
 * ({@link CatalogRegions}); a merchant's committed write drops their store's entries at once on the task that took
 * it, and another task lags by the minute.
 *
 * <p>
 * A listing or a search is keyed by its criteria's canonical text ({@link QueryKey}); what a shopper typed is keyed
 * normalised ({@link SuggestQuery}). Only the storefront's shape of each read is here: the console's every-language
 * tree and its private reads go to the services directly.
 * </p>
 */
@Component
@CacheConfig(keyGenerator = StoreScopedKeyGenerator.BEAN)
@RequiredArgsConstructor
public class StorefrontCatalogReads {

    private final ProductGroupService groups;

    private final CategoryService categories;

    private final ManufacturerService manufacturers;

    private final ProductService products;

    private final ProductSearchService search;

    @Cacheable(CatalogRegions.Names.GROUP)
    public ReadableProductGroup group(StoreMerchantId store, LanguageCode language, String code) {
        return groups.storefront(store, code, language);
    }

    @Cacheable(CatalogRegions.Names.RELATED)
    public ReadableProductGroup related(StoreMerchantId store, LanguageCode language, ProductId product) {
        return groups.related(store, product.value(), language);
    }

    /** The storefront's tree, shopper languages only, unfiltered: what every page's navigation reads. */
    @Cacheable(CatalogRegions.Names.HIERARCHY)
    public ReadableEntityList<ReadableCategory> hierarchy(StoreMerchantId store, LanguageCode language,
                                                          Pageable pageable) {
        return categories.hierarchy(store, null, language, false, pageable);
    }

    @Cacheable(CatalogRegions.Names.CATEGORY)
    public ReadableCategory category(StoreMerchantId store, LanguageCode language, String friendlyUrl)
            throws CategoryFriendlyUrlNotFoundException {
        return categories.getByFriendlyUrl(store, friendlyUrl, language);
    }

    @Cacheable(CatalogRegions.Names.BRANDS)
    public List<ReadableManufacturer> brands(StoreMerchantId store, LanguageCode language, CategoryId category)
            throws CategoryNotFoundException {
        return manufacturers.listByCategory(store, category.value(), language);
    }

    @Cacheable(CatalogRegions.Names.PRODUCT)
    public ReadableProduct product(StoreMerchantId store, LanguageCode language, String friendlyUrl)
            throws ProductNotFoundException {
        return products.getByFriendlyUrl(store, friendlyUrl, language);
    }

    /** The category page's listing, keyed by its filter's canonical text and its page. */
    @Cacheable(CatalogRegions.Names.LISTING)
    public ReadableEntityList<ReadableProduct> list(StoreMerchantId store, LanguageCode language,
                                                    QueryKey<ProductFilter> filter, Pageable pageable) {
        return products.list(store, filter.criteria(), language, pageable);
    }

    /** The results page with its rail, and the rail alone ({@code rows=false}), keyed by the criteria's text. */
    @Cacheable(CatalogRegions.Names.SEARCH)
    public ReadableProductSearchResult search(StoreMerchantId store, LanguageCode language,
                                              QueryKey<ProductSearchCriteria> criteria, Pageable pageable) {
        return search.search(store, criteria.criteria(), language, pageable);
    }

    @Cacheable(CatalogRegions.Names.SUGGEST)
    public List<ReadableProductSuggestion> suggest(StoreMerchantId store, LanguageCode language, SuggestQuery query) {
        return search.suggest(store, query.text(), language, query.limit());
    }
}
