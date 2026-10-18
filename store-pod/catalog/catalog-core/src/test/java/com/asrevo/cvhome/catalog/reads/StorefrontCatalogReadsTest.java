package com.asrevo.cvhome.catalog.reads;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.cache.QueryKey;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Each read hands its typed arguments to the service behind it, unwrapping a criteria or a query, and every one is
 * declared on a catalog region with the store-scoped key generator.
 */
@ExtendWith(MockitoExtension.class)
class StorefrontCatalogReadsTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b75f");

    private static final LanguageCode EN = LanguageCode.defaultLanguage();

    private static final Pageable PAGE = PageRequest.of(0, 10);

    private static final String SHOES = "shoes";

    private static final String FEATURED = "FEATURED";

    @Mock
    private ProductGroupService groups;

    @Mock
    private CategoryService categories;

    @Mock
    private ManufacturerService manufacturers;

    @Mock
    private ProductService products;

    @Mock
    private ProductSearchService search;

    @InjectMocks
    private StorefrontCatalogReads reads;

    @Test
    void eachReadUnwrapsItsArgumentsForTheService() throws Exception {
        ReadableProductGroup group = new ReadableProductGroup();
        ReadableCategory category = new ReadableCategory();
        ReadableProduct product = new ReadableProduct();
        ReadableEntityList<ReadableCategory> tree = new ReadableEntityList<>();
        ReadableEntityList<ReadableProduct> listing = new ReadableEntityList<>();
        ReadableProductSearchResult result = new ReadableProductSearchResult();
        ProductFilter filter = new ProductFilter();
        ProductSearchCriteria criteria = new ProductSearchCriteria();
        when(groups.storefront(STORE, FEATURED, EN)).thenReturn(group);
        when(groups.related(STORE, 7L, EN)).thenReturn(group);
        when(categories.hierarchy(STORE, null, EN, false, PAGE)).thenReturn(tree);
        when(categories.getByFriendlyUrl(STORE, SHOES, EN)).thenReturn(category);
        when(manufacturers.listByCategory(STORE, 3L, EN)).thenReturn(List.of(new ReadableManufacturer()));
        when(products.getByFriendlyUrl(STORE, SHOES, EN)).thenReturn(product);
        when(products.list(STORE, filter, EN, PAGE)).thenReturn(listing);
        when(search.search(STORE, criteria, EN, PAGE)).thenReturn(result);
        when(search.suggest(STORE, SHOES, EN, 5)).thenReturn(List.of(new ReadableProductSuggestion()));

        assertThat(reads.group(STORE, EN, FEATURED)).isSameAs(group);
        assertThat(reads.related(STORE, EN, ProductId.of(7))).isSameAs(group);
        assertThat(reads.hierarchy(STORE, EN, PAGE)).isSameAs(tree);
        assertThat(reads.category(STORE, EN, SHOES)).isSameAs(category);
        assertThat(reads.brands(STORE, EN, CategoryId.of(3))).hasSize(1);
        assertThat(reads.product(STORE, EN, SHOES)).isSameAs(product);
        assertThat(reads.list(STORE, EN, QueryKey.of(filter.normalised(), filter), PAGE)).isSameAs(listing);
        assertThat(reads.search(STORE, EN, QueryKey.of(criteria.normalised(), criteria), PAGE)).isSameAs(result);
        assertThat(reads.suggest(STORE, EN, SuggestQuery.of(" Shoes", 5))).hasSize(1);
        verify(search).suggest(STORE, SHOES, EN, 5);
    }

    @Test
    void everyReadIsDeclaredOnACatalogRegion() {
        for (Method method : StorefrontCatalogReads.class.getDeclaredMethods()) {
            if (!java.lang.reflect.Modifier.isPublic(method.getModifiers()) || method.isSynthetic()) {
                continue;
            }
            Cacheable cacheable = AnnotatedElementUtils.findMergedAnnotation(method, Cacheable.class);
            assertThat(cacheable).as(method.getName()).isNotNull();
            assertThat(cacheable.cacheNames()).hasSize(1);
            assertThat(cacheable.cacheNames()[0]).startsWith("catalog.");
        }
    }
}
