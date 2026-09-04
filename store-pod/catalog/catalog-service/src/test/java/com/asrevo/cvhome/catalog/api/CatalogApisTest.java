package com.asrevo.cvhome.catalog.api;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;

import com.asrevo.cvhome.catalog.api.v1.CategoryApi;
import com.asrevo.cvhome.catalog.api.v1.ManufacturerApi;
import com.asrevo.cvhome.catalog.api.v1.ProductApi;
import com.asrevo.cvhome.catalog.api.v1.ProductGroupApi;
import com.asrevo.cvhome.catalog.api.v1.ProductOptionApi;
import com.asrevo.cvhome.catalog.api.v1.ProductTypeApi;
import com.asrevo.cvhome.catalog.api.v2.ProductApiV2;
import com.asrevo.cvhome.catalog.model.manufacturer.PersistableManufacturer;
import com.asrevo.cvhome.catalog.services.category.CategoryService;
import com.asrevo.cvhome.catalog.services.group.ProductGroupService;
import com.asrevo.cvhome.catalog.services.manufacturer.ManufacturerService;
import com.asrevo.cvhome.catalog.services.option.ProductOptionService;
import com.asrevo.cvhome.catalog.services.product.ProductSearchService;
import com.asrevo.cvhome.catalog.services.product.ProductService;
import com.asrevo.cvhome.catalog.services.type.ProductTypeService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The catalog endpoints: their store scoping, their gates, and the public/private split.
 *
 * <p>
 * Catalog is the one pod where the same resource is read by two audiences. {@code /category-hierarchy} answers a
 * shopper and {@code /private/category-hierarchy} answers the console, and they differ only in a boolean handed to
 * the same service method — the private one asks for hidden categories too. Passing the wrong one publishes an
 * unfinished category tree to every shopper, and nothing about the call site makes that visible, so both are
 * asserted with the flag they carry.
 * </p>
 */
class CatalogApisTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");
    private static final LanguageCode ENGLISH = new LanguageCode("en");
    private static final String CODE = "a-code";
    private static final String SLUG = "a-slug";
    private static final String MANAGE = "STORE-POD.CATALOG.*";
    private static final String TERM = "sho";

    private final CategoryService categoryService = Mockito.mock(CategoryService.class);
    private final ManufacturerService manufacturerService = Mockito.mock(ManufacturerService.class);
    private final ProductGroupService productGroupService = Mockito.mock(ProductGroupService.class);
    private final ProductTypeService productTypeService = Mockito.mock(ProductTypeService.class);
    private final ProductOptionService productOptionService = Mockito.mock(ProductOptionService.class);
    private final ProductService productService = Mockito.mock(ProductService.class);
    private final ProductSearchService productSearchService = Mockito.mock(ProductSearchService.class);

    private final CategoryApi categoryApi = new CategoryApi(categoryService);
    private final ManufacturerApi manufacturerApi = new ManufacturerApi(manufacturerService);
    private final ProductGroupApi productGroupApi = new ProductGroupApi(productGroupService);
    private final ProductTypeApi productTypeApi = new ProductTypeApi(productTypeService);
    private final ProductOptionApi productOptionApi = new ProductOptionApi(productOptionService);
    private final ProductApi productApi = new ProductApi(productService);
    private final ProductApiV2 productApiV2 = new ProductApiV2(productService, productSearchService);

    @Test
    void theShopperAndConsoleHierarchiesDifferOnlyInWhetherHiddenCategoriesAreIncluded() {
        categoryApi.hierarchy(null, STORE, ENGLISH, PageRequest.of(0, 20));
        categoryApi.privateHierarchy(null, STORE, ENGLISH, PageRequest.of(0, 20));

        // false is the shopper's view. Handing true to the public endpoint publishes an unfinished category tree.
        verify(categoryService).hierarchy(eq(STORE), eq(null), eq(ENGLISH), eq(false), any());
        verify(categoryService).hierarchy(eq(STORE), eq(null), eq(ENGLISH), eq(true), any());
    }

    @Test
    void everyCategoryReadAndWriteCarriesTheStore() throws Exception {
        categoryApi.getByFriendlyUrl(SLUG, STORE, ENGLISH);
        categoryApi.list(null, STORE, ENGLISH, PageRequest.of(0, 20));
        categoryApi.get(1L, STORE, ENGLISH);
        categoryApi.listByProduct(2L, STORE, ENGLISH);
        categoryApi.move(1L, 2L, STORE);
        categoryApi.delete(1L, STORE);

        verify(categoryService).getByFriendlyUrl(STORE, SLUG, ENGLISH);
        verify(categoryService).get(STORE, 1L, ENGLISH);
        verify(categoryService).move(STORE, 1L, 2L);
        verify(categoryService).delete(STORE, 1L);
    }

    @Test
    void anExistenceCheckAnswersAsTheEntityExistsShapeTheConsoleAlreadyKnows() {
        when(categoryService.exists(STORE, CODE)).thenReturn(true);
        when(manufacturerService.exists(STORE, CODE)).thenReturn(false);
        when(productGroupService.exists(STORE, CODE)).thenReturn(true);
        when(productTypeService.exists(STORE, CODE)).thenReturn(true);
        when(productOptionService.exists(STORE, CODE)).thenReturn(true);
        when(productService.exists(STORE, CODE)).thenReturn(true);

        assertThat(categoryApi.exists(CODE, STORE).isExists()).isTrue();
        assertThat(manufacturerApi.exists(CODE, STORE).isExists()).isFalse();
        assertThat(productGroupApi.exists(CODE, STORE).isExists()).isTrue();
        assertThat(productTypeApi.exists(CODE, STORE).isExists()).isTrue();
        assertThat(productOptionApi.exists(CODE, STORE).isExists()).isTrue();
        assertThat(productApi.exists(CODE, STORE).isExists()).isTrue();
    }

    @Test
    void theBrandAndTypeEndpointsPassTheirIdentityThrough() throws Exception {
        manufacturerApi.listByCategory(1L, STORE, ENGLISH);
        manufacturerApi.list(null, STORE, ENGLISH, PageRequest.of(0, 20));
        manufacturerApi.get(1L, STORE, ENGLISH);
        manufacturerApi.update(1L, new PersistableManufacturer(), STORE);
        manufacturerApi.delete(1L, STORE);
        productTypeApi.list(STORE, ENGLISH, PageRequest.of(0, 20));
        productTypeApi.get(1L, STORE, ENGLISH);
        productTypeApi.update(1L, null, STORE);
        productTypeApi.delete(1L, STORE);

        verify(manufacturerService).listByCategory(STORE, 1L, ENGLISH);
        verify(manufacturerService).get(STORE, 1L, ENGLISH);
        verify(manufacturerService).delete(STORE, 1L);
        verify(productTypeService).get(STORE, 1L, ENGLISH);
        verify(productTypeService).update(STORE, 1L, null);
        verify(productTypeService).delete(STORE, 1L);
    }

    @Test
    void theOptionAndGroupEndpointsPassTheirIdentityThrough() throws Exception {
        productOptionApi.list(STORE, ENGLISH, PageRequest.of(0, 20));
        productOptionApi.get(1L, STORE, ENGLISH);
        productOptionApi.update(1L, null, STORE);
        productOptionApi.delete(1L, STORE);
        productGroupApi.get(CODE, STORE, ENGLISH);
        productGroupApi.getPrivate(CODE, STORE, ENGLISH);
        productGroupApi.list(STORE, ENGLISH, PageRequest.of(0, 20));

        verify(productOptionService).get(STORE, 1L, ENGLISH);
        verify(productOptionService).delete(STORE, 1L);
        verify(productGroupService, Mockito.atLeastOnce()).get(eq(STORE), eq(CODE), eq(ENGLISH), Mockito.anyBoolean());
    }

    @Test
    void theProductWritesAreAllScopedToTheStore() throws Exception {
        productApi.patch(1L, null, STORE);
        productApi.delete(1L, STORE);
        productApi.removeFromCategory(1L, 2L, STORE);
        productApiV2.update(1L, null, STORE);

        verify(productService).delete(STORE, 1L);
        verify(productService).removeFromCategory(STORE, 1L, 2L);
    }

    @Test
    void suggestionsAreCachedBrieflyAndPubliclyBecauseTheyAreTypedAheadOfEveryKeystroke() {
        when(productSearchService.suggest(STORE, TERM, ENGLISH, 8)).thenReturn(List.of());

        var response = productApiV2.suggest(TERM, 8, STORE, ENGLISH);

        assertThat(response.getHeaders().getCacheControl()).contains("max-age=30", "public");
        verify(productSearchService).suggest(STORE, TERM, ENGLISH, 8);
    }

    private static Stream<Method> privateEndpoints() {
        return Stream.of(CategoryApi.class, ManufacturerApi.class, ProductTypeApi.class, ProductOptionApi.class,
                        ProductApi.class, ProductApiV2.class)
                .flatMap(type -> Stream.of(type.getDeclaredMethods()))
                .filter(m -> m.isAnnotationPresent(PreAuthorize.class))
                .sorted((a, b) -> (a.getDeclaringClass().getSimpleName() + a.getName())
                        .compareTo(b.getDeclaringClass().getSimpleName() + b.getName()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("privateEndpoints")
    void everyGatedEndpointNamesTheCatalogPermissionAgainstTheRequestedStore(Method endpoint) {
        PreAuthorize gate = endpoint.getAnnotation(PreAuthorize.class);

        assertThat(gate.value()).contains("hasPermission(#merchantStore,'StoreMerchantId'").contains(MANAGE);
    }

    @Test
    void theShopperFacingReadsAreDeliberatelyUngated() {
        // A storefront request carries no staff token; these are the endpoints a shop's own pages call.
        assertThat(Stream.of(CategoryApi.class.getDeclaredMethods())
                .filter(m -> "hierarchy".equals(m.getName()) || "getByFriendlyUrl".equals(m.getName()))
                .noneMatch(m -> m.isAnnotationPresent(PreAuthorize.class))).isTrue();
    }
}
