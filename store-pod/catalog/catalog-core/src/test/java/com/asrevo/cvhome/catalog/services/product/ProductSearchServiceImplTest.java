package com.asrevo.cvhome.catalog.services.product;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.asrevo.cvhome.catalog.entity.Category;
import com.asrevo.cvhome.catalog.entity.CategoryDescription;
import com.asrevo.cvhome.catalog.entity.Manufacturer;
import com.asrevo.cvhome.catalog.entity.ManufacturerDescription;
import com.asrevo.cvhome.catalog.entity.Product;
import com.asrevo.cvhome.catalog.entity.ProductDescription;
import com.asrevo.cvhome.catalog.entity.ProductType;
import com.asrevo.cvhome.catalog.entity.ProductVariant;
import com.asrevo.cvhome.catalog.model.product.ProductSearchCriteria;
import com.asrevo.cvhome.catalog.model.product.ProductSearchSort;
import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProductSearchResult;
import com.asrevo.cvhome.catalog.model.product.ReadableProductSuggestion;
import com.asrevo.cvhome.catalog.repositories.CategoryRepository;
import com.asrevo.cvhome.catalog.repositories.ManufacturerRepository;
import com.asrevo.cvhome.catalog.repositories.ProductFacetRepository;
import com.asrevo.cvhome.catalog.repositories.ProductOptionValueRepository;
import com.asrevo.cvhome.catalog.repositories.ProductRepository;
import com.asrevo.cvhome.catalog.repositories.ProductSearchIndexRepository;
import com.asrevo.cvhome.catalog.repositories.ProductTypeRepository;
import com.asrevo.cvhome.catalog.services.image.ImageMapper;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The decisions the search service makes that no SQL and no HTTP call can show on their own: what it does when
 * a query matches nothing, how facet buckets are assembled and ordered, and what the suggest path refuses.
 *
 * <p>
 * The predicates themselves are the database's to answer, and the integration test asks it. What is asserted
 * here is the order the fallbacks are tried in, and that each one only fires when the one before it failed —
 * a shopper who typed something real must never be shown a correction instead.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductSearchServiceImplTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    private static final String EN_CODE = "en";

    private static final LanguageCode EN = new LanguageCode(EN_CODE);

    private static final Pageable PAGE = PageRequest.of(0, 24);

    private static final float FLOOR = 0.3f;

    private static final String SKU_1 = "SKU-1";

    private static final String RUNNING_SHOES = "Running Shoes";

    private static final String RUNNING = "running";

    private static final String RUNING = "runing";

    private static final String AR_CODE = "ar";

    private static final String SHOE_AR = "حذاء";

    private static final String NONSENSE = "zzzz";

    private static final String SKU_2 = "SKU-2";

    private static final String SKU_3 = "SKU-3";

    private static final String ONE = "One";

    private static final String TWO = "Two";

    private static final String ANY_TERM = "x";

    private static final String MEN_SHOES = "MEN_SHOES";

    private static final String SUN_SKU = "SKU-CH-AC-SUN06";

    private static final String SUNGLASSES = "Chanel Butterfly Sunglasses";

    private static final String CHANEL = "Chanel";

    private static final String SUN = "sun";

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductSearchIndexRepository searchIndexRepository;

    @Mock
    private ProductFacetRepository facetRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ManufacturerRepository manufacturerRepository;

    @Mock
    private ProductTypeRepository productTypeRepository;

    @Mock
    private ProductOptionValueRepository optionValueRepository;

    @Mock
    private ProductSearchIndexer indexer;

    @Mock
    private ProductMapper productMapper;

    private ProductSearchServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProductSearchServiceImpl(productRepository, searchIndexRepository, facetRepository,
                categoryRepository, manufacturerRepository, productTypeRepository, optionValueRepository,
                indexer, productMapper, new ImageMapper("https://cdn.example/bucket"));
        when(productMapper.toReadable(any(), any())).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            ReadableProduct readable = new ReadableProduct();
            readable.setId(product.getId());
            product.defaultVariant().ifPresent(variant -> readable.setSku(variant.getSku()));
            return readable;
        });
    }

    // ------------------------------------------------------------------------------------------------ fixtures

    private static Product product(long id, String sku, String name) {
        Product product = new Product();
        product.setId(id);
        ProductVariant defaultVariant = new ProductVariant(product, sku);
        defaultVariant.setDefaultVariant(true);
        product.getVariants().add(defaultVariant);
        product.setStore(STORE);
        ProductDescription description = new ProductDescription(product);
        description.setLanguageCode(EN);
        description.setName(name);
        description.setSeUrl(sku.toLowerCase());
        product.setDescriptions(new java.util.HashSet<>(Set.of(description)));
        return product;
    }

    private static ProductSearchCriteria criteria(String q) {
        ProductSearchCriteria criteria = new ProductSearchCriteria();
        criteria.setQ(q);
        criteria.setFacets(false);
        return criteria;
    }

    private void hasProducts(Product... products) {
        Page<Product> page = new PageImpl<>(List.of(products), PAGE, products.length);
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(productRepository.findAllHydrated(anyList())).thenReturn(List.of(products));
    }

    private void hasNoProducts() {
        when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PAGE, 0));
    }

    // -------------------------------------------------------------------------------------------------- search

    @Test
    void aMatchIsReturnedWithoutTryingAnyFallback() {
        hasProducts(product(1L, SKU_1, RUNNING_SHOES));

        ReadableProductSearchResult result = service.search(STORE, criteria(RUNNING), EN, PAGE);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getDidYouMean()).isNull();
        assertThat(result.getLanguage()).isEqualTo(EN_CODE);
        verify(searchIndexRepository, never()).bestNearMiss(anyString(), anyString(), anyString(), any(Float.class));
        verify(searchIndexRepository, never()).richestLanguageOtherThan(anyString(), anyString());
    }

    /**
     * A blank query is a filtered listing, not a failed search — offering a correction for a term nobody typed
     * would be nonsense.
     */
    @Test
    void anEmptyResultForABlankQueryTriesNothing() {
        hasNoProducts();

        ReadableProductSearchResult result = service.search(STORE, criteria("  "), EN, PAGE);

        assertThat(result.getTotalElements()).isZero();
        verify(searchIndexRepository, never()).bestNearMiss(anyString(), anyString(), anyString(), any(Float.class));
    }

    @Test
    void nothingMatchedSoTheNearMissIsSearchedInstead() {
        Product corrected = product(1L, SKU_1, RUNNING_SHOES);
        when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PAGE, 0))
                .thenReturn(new PageImpl<>(List.of(corrected), PAGE, 1));
        when(productRepository.findAllHydrated(anyList())).thenReturn(List.of(corrected));
        when(searchIndexRepository.bestNearMiss(eq(STORE.getId()), eq(EN_CODE), eq(RUNING), eq(FLOOR)))
                .thenReturn(Optional.of(RUNNING_SHOES));

        ReadableProductSearchResult result = service.search(STORE, criteria(RUNING), EN, PAGE);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getDidYouMean()).isEqualTo(RUNNING_SHOES);
    }

    /**
     * A near miss that also finds nothing is not a correction worth showing — the shopper keeps their own
     * empty result rather than being told we meant something that is equally absent.
     */
    @Test
    void aNearMissThatAlsoFindsNothingIsNotOffered() {
        hasNoProducts();
        when(searchIndexRepository.bestNearMiss(anyString(), anyString(), anyString(), any(Float.class)))
                .thenReturn(Optional.of(RUNNING_SHOES));

        ReadableProductSearchResult result = service.search(STORE, criteria(RUNING), EN, PAGE);

        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getDidYouMean()).isNull();
    }

    @Test
    void withNoNearMissTheStoresRichestLanguageIsTriedNext() {
        Product arabic = product(1L, SKU_1, SHOE_AR);
        when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PAGE, 0))
                .thenReturn(new PageImpl<>(List.of(arabic), PAGE, 1));
        when(productRepository.findAllHydrated(anyList())).thenReturn(List.of(arabic));
        when(searchIndexRepository.bestNearMiss(anyString(), anyString(), anyString(), any(Float.class)))
                .thenReturn(Optional.empty());
        when(searchIndexRepository.richestLanguageOtherThan(STORE.getId(), EN_CODE)).thenReturn(Optional.of(AR_CODE));

        ReadableProductSearchResult result = service.search(STORE, criteria(SHOE_AR), EN, PAGE);

        assertThat(result.getTotalElements()).isEqualTo(1);
        // The theme needs to know it is showing another language, or the mix looks like a bug.
        assertThat(result.getLanguage()).isEqualTo(AR_CODE);
    }

    @Test
    void whenEveryFallbackFailsTheOriginalEmptyResultStands() {
        hasNoProducts();
        when(searchIndexRepository.bestNearMiss(anyString(), anyString(), anyString(), any(Float.class)))
                .thenReturn(Optional.empty());
        when(searchIndexRepository.richestLanguageOtherThan(anyString(), anyString())).thenReturn(Optional.empty());

        ReadableProductSearchResult result = service.search(STORE, criteria(NONSENSE), EN, PAGE);

        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getLanguage()).isEqualTo(EN_CODE);
    }

    /**
     * The page comes back in rank order and the hydrating query makes no promise about ordering, so the
     * service has to restore it. Losing this silently reorders every relevance-sorted page.
     */
    @Test
    void theRankedOrderSurvivesHydration() {
        Product first = product(1L, SKU_1, ONE);
        Product second = product(2L, SKU_2, TWO);
        Product third = product(3L, SKU_3, "Three");
        when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(third, first, second), PAGE, 3));
        // deliberately a different order, the way a database is free to answer an `in (...)`
        when(productRepository.findAllHydrated(anyList())).thenReturn(List.of(first, second, third));

        ReadableProductSearchResult result = service.search(STORE, criteria(ANY_TERM), EN, PAGE);

        assertThat(result.getContent()).extracting(ReadableProduct::getSku)
                .containsExactly(SKU_3, SKU_1, SKU_2);
    }

    @Test
    void aProductMissingFromTheHydrationIsDroppedRatherThanNull() {
        Product present = product(1L, SKU_1, ONE);
        when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(present, product(2L, SKU_2, TWO)), PAGE, 2));
        when(productRepository.findAllHydrated(anyList())).thenReturn(List.of(present));

        ReadableProductSearchResult result = service.search(STORE, criteria(ANY_TERM), EN, PAGE);

        assertThat(result.getContent()).extracting(ReadableProduct::getSku).containsExactly(SKU_1);
    }

    // -------------------------------------------------------------------------------------------------- facets

    @Test
    void facetBucketsAreLabelledCountedAndOrderedBySize() {
        Product product = product(1L, SKU_1, RUNNING_SHOES);
        hasProducts(product);
        ProductSearchCriteria criteria = criteria(RUNNING);
        criteria.setFacets(true);
        criteria.setManufacturerIds(List.of(7L));
        when(facetRepository.countByManufacturer(any())).thenReturn(Map.of(7L, 2L, 8L, 5L));
        when(manufacturerRepository.findAllById(anyList()))
                .thenReturn(List.of(manufacturer(7L, "NIKE", "Nike"), manufacturer(8L, "ZARA", "Zara")));

        ReadableProductSearchResult result = service.search(STORE, criteria, EN, PAGE);

        assertThat(result.getFacets().getBrands()).extracting(b -> "%s:%d".formatted(b.getName(), b.getCount()))
                .containsExactly("Zara:5", "Nike:2");
        assertThat(result.getFacets().getBrands()).filteredOn(b -> b.getId() == 7L)
                .allMatch(b -> b.isSelected());
    }

    /**
     * A bucket whose entity has no copy in this language still has to be clickable, so it falls back to the
     * code rather than vanishing or rendering blank.
     */
    @Test
    void aBucketWithNoCopyInThisLanguageFallsBackToItsCode() {
        hasProducts(product(1L, SKU_1, RUNNING_SHOES));
        ProductSearchCriteria criteria = criteria(RUNNING);
        criteria.setFacets(true);
        when(facetRepository.countByCategory(any())).thenReturn(Map.of(3L, 1L));
        when(categoryRepository.findAllById(anyList())).thenReturn(List.of(category(3L, MEN_SHOES, null)));

        ReadableProductSearchResult result = service.search(STORE, criteria, EN, PAGE);

        assertThat(result.getFacets().getCategories()).singleElement()
                .satisfies(bucket -> assertThat(bucket.getName()).isEqualTo(MEN_SHOES));
    }

    @Test
    void facetsAreNotCountedWhenTheCallerDidNotAskForThem() {
        hasProducts(product(1L, SKU_1, RUNNING_SHOES));

        ReadableProductSearchResult result = service.search(STORE, criteria(RUNNING), EN, PAGE);

        assertThat(result.getFacets()).isNull();
        verify(facetRepository, never()).countByCategory(any());
        verify(facetRepository, never()).countByManufacturer(any());
        verify(facetRepository, never()).countByType(any());
    }

    // ------------------------------------------------------------------------------------------------- sorting

    @Test
    void relevanceWithoutAQueryFallsBackToTheMerchantsOwnOrder() {
        hasProducts(product(1L, SKU_1, ONE));
        ProductSearchCriteria criteria = criteria("");
        criteria.setSort(ProductSearchSort.RELEVANCE);

        service.search(STORE, criteria, EN, PAGE);

        // A blank query has nothing to be relevant to, so the page must carry a real sort rather than none.
        verify(productRepository).findAll(any(Specification.class),
                org.mockito.ArgumentMatchers.argThat((Pageable p) -> p.getSort().isSorted()));
    }

    @Test
    void aNamedSortIsHonouredEvenWithAQuery() {
        hasProducts(product(1L, SKU_1, ONE));
        ProductSearchCriteria criteria = criteria("shoes");
        criteria.setSort(ProductSearchSort.NEWEST);

        service.search(STORE, criteria, EN, PAGE);

        verify(productRepository).findAll(any(Specification.class),
                org.mockito.ArgumentMatchers.argThat((Pageable p) ->
                        p.getSort().getOrderFor("dateAvailable") != null));
    }

    // ------------------------------------------------------------------------------------------------- suggest

    @Test
    void suggestReturnsRankedRowsWithWhatADropdownDraws() {
        Product product = product(6L, SUN_SKU, SUNGLASSES);
        product.setManufacturer(manufacturer(2L, "CHANEL", CHANEL));
        when(searchIndexRepository.suggestProductIds(STORE.getId(), EN_CODE, SUN, 8)).thenReturn(List.of(6L));
        when(productRepository.findAllHydrated(List.of(6L))).thenReturn(List.of(product));

        List<ReadableProductSuggestion> suggestions = service.suggest(STORE, SUN, EN, 8);

        assertThat(suggestions).singleElement().satisfies(suggestion -> {
            assertThat(suggestion.getName()).isEqualTo(SUNGLASSES);
            assertThat(suggestion.getSku()).isEqualTo(SUN_SKU);
            assertThat(suggestion.getFriendlyUrl()).isEqualTo("sku-ch-ac-sun06");
            assertThat(suggestion.getBrand()).isEqualTo(CHANEL);
        });
    }

    @Test
    void suggestKeepsTheRankedOrderTheIndexReturned() {
        Product one = product(1L, SKU_1, ONE);
        Product two = product(2L, SKU_2, TWO);
        when(searchIndexRepository.suggestProductIds(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anyInt()))
                .thenReturn(List.of(2L, 1L));
        when(productRepository.findAllHydrated(anyList())).thenReturn(List.of(one, two));

        assertThat(service.suggest(STORE, ANY_TERM, EN, 8))
                .extracting(ReadableProductSuggestion::getSku).containsExactly(SKU_2, SKU_1);
    }

    @Test
    void suggestRefusesABlankTermWithoutTouchingTheIndex() {
        assertThat(service.suggest(STORE, "   ", EN, 8)).isEmpty();
        assertThat(service.suggest(STORE, null, EN, 8)).isEmpty();

        verify(searchIndexRepository, never())
                .suggestProductIds(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anyInt());
    }

    /**
     * The limit is the shopper's to ask for and ours to bound — this endpoint answers every keystroke.
     */
    @Test
    void suggestClampsTheLimitAtBothEnds() {
        when(searchIndexRepository.suggestProductIds(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anyInt()))
                .thenReturn(List.of());

        service.suggest(STORE, ANY_TERM, EN, 5000);
        verify(searchIndexRepository).suggestProductIds(STORE.getId(), EN_CODE, ANY_TERM, 10);

        service.suggest(STORE, ANY_TERM, EN, 0);
        verify(searchIndexRepository).suggestProductIds(STORE.getId(), EN_CODE, ANY_TERM, 1);
    }

    @Test
    void suggestWithNoMatchesNeverAsksForHydration() {
        when(searchIndexRepository.suggestProductIds(anyString(), anyString(), anyString(), org.mockito.ArgumentMatchers.anyInt()))
                .thenReturn(List.of());

        assertThat(service.suggest(STORE, NONSENSE, EN, 8)).isEmpty();
        verify(productRepository, never()).findAllHydrated(anyList());
    }

    // ------------------------------------------------------------------------------------------------- rebuild

    @Test
    void rebuildDelegatesToTheIndexer() {
        service.rebuildIndex(STORE);

        verify(indexer).rebuild(STORE);
    }

    // ------------------------------------------------------------------------------------------------ helpers

    private static Manufacturer manufacturer(long id, String code, String name) {
        Manufacturer manufacturer = new Manufacturer();
        manufacturer.setId(id);
        manufacturer.setCode(code);
        manufacturer.setStoreMerchantId(STORE);
        if (name != null) {
            ManufacturerDescription description = new ManufacturerDescription();
            description.setLanguageCode(EN);
            description.setName(name);
            description.setManufacturer(manufacturer);
            manufacturer.setDescriptions(new java.util.HashSet<>(Set.of(description)));
        }
        return manufacturer;
    }

    private static Category category(long id, String code, String name) {
        Category category = new Category();
        category.setId(id);
        category.setCode(code);
        category.setStoreMerchantId(STORE);
        if (name != null) {
            CategoryDescription description = new CategoryDescription();
            description.setLanguageCode(EN);
            description.setName(name);
            description.setCategory(category);
            category.setDescriptions(new java.util.HashSet<>(Set.of(description)));
        }
        return category;
    }

    @SuppressWarnings("unused")
    private static ProductType type(long id, String code) {
        ProductType productType = new ProductType();
        productType.setId(id);
        productType.setCode(code);
        return productType;
    }
}
