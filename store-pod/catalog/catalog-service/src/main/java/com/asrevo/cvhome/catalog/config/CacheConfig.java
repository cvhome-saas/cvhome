package com.asrevo.cvhome.catalog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.cache.CacheRegions;
import com.asrevo.cvhome.cache.eviction.EvictionRules;
import com.asrevo.cvhome.catalog.entity.Category;
import com.asrevo.cvhome.catalog.entity.CategoryDescription;
import com.asrevo.cvhome.catalog.entity.Manufacturer;
import com.asrevo.cvhome.catalog.entity.ManufacturerDescription;
import com.asrevo.cvhome.catalog.entity.Product;
import com.asrevo.cvhome.catalog.entity.ProductDescription;
import com.asrevo.cvhome.catalog.entity.ProductGroup;
import com.asrevo.cvhome.catalog.entity.ProductGroupDescription;
import com.asrevo.cvhome.catalog.entity.ProductImage;
import com.asrevo.cvhome.catalog.entity.ProductOption;
import com.asrevo.cvhome.catalog.entity.ProductOptionAssignment;
import com.asrevo.cvhome.catalog.entity.ProductOptionDescription;
import com.asrevo.cvhome.catalog.entity.ProductOptionValue;
import com.asrevo.cvhome.catalog.entity.ProductOptionValueDescription;
import com.asrevo.cvhome.catalog.entity.ProductSearchIndex;
import com.asrevo.cvhome.catalog.entity.ProductType;
import com.asrevo.cvhome.catalog.entity.ProductTypeDescription;
import com.asrevo.cvhome.catalog.entity.ProductVariant;
import com.asrevo.cvhome.catalog.entity.ProductVariantOptionValue;
import com.asrevo.cvhome.catalog.reads.CatalogRegions;

/**
 * What catalog caches ({@link CatalogRegions}) and which committed write drops which of it. The regions live on
 * the library's Caffeine provider with the enum's defaults unless {@code com.asrevo.cvhome.cache.regions.catalog}
 * says otherwise; the merchant client's region arrives with the client.
 */
@Configuration
public class CacheConfig {

    @Bean
    CacheRegions catalogRegions() {
        return CacheRegions.of(CatalogRegions.values());
    }

    /**
     * A product, its parts and its variants stale everything that shows a product; a category or a brand stales the
     * navigation and the listings; a group only what reads groups; the derived search rows only the search. An
     * option or a type is shown on a product page, so it stales the product reads too.
     */
    @Bean
    EvictionRules catalogEvictionRules() {
        return EvictionRules.in("com.asrevo.cvhome.catalog.entity")
                .on(Product.class, ProductDescription.class, ProductImage.class, ProductVariant.class,
                        ProductVariantOptionValue.class, ProductOptionAssignment.class)
                .evict(CatalogRegions.PRODUCT, CatalogRegions.LISTING, CatalogRegions.SEARCH, CatalogRegions.SUGGEST,
                        CatalogRegions.RELATED, CatalogRegions.GROUP, CatalogRegions.CART_LINE,
                        CatalogRegions.DETAILED_PRODUCT)
                .on(Category.class, CategoryDescription.class)
                .evict(CatalogRegions.HIERARCHY, CatalogRegions.CATEGORY, CatalogRegions.BRANDS, CatalogRegions.LISTING,
                        CatalogRegions.SEARCH, CatalogRegions.PRODUCT)
                .on(Manufacturer.class, ManufacturerDescription.class)
                .evict(CatalogRegions.BRANDS, CatalogRegions.LISTING, CatalogRegions.SEARCH, CatalogRegions.PRODUCT,
                        CatalogRegions.SUGGEST)
                .on(ProductGroup.class, ProductGroupDescription.class)
                .evict(CatalogRegions.GROUP, CatalogRegions.RELATED)
                .on(ProductType.class, ProductTypeDescription.class, ProductOption.class, ProductOptionDescription.class,
                        ProductOptionValue.class, ProductOptionValueDescription.class)
                .evict(CatalogRegions.PRODUCT, CatalogRegions.SEARCH, CatalogRegions.CART_LINE,
                        CatalogRegions.DETAILED_PRODUCT)
                .on(ProductSearchIndex.class)
                .evict(CatalogRegions.SEARCH, CatalogRegions.SUGGEST)
                .build();
    }
}
