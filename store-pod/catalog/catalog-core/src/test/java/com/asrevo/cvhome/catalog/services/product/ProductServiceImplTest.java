package com.asrevo.cvhome.catalog.services.product;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.billing.commons.EntitlementKey;
import com.asrevo.cvhome.billing.commons.EntitlementValue;
import com.asrevo.cvhome.billing.commons.SubscriptionStatus;
import com.asrevo.cvhome.billing.commons.dto.EntitlementSnapshot;
import com.asrevo.cvhome.billing.commons.errors.EntitlementExceededException;
import com.asrevo.cvhome.billing.guard.StoreEntitlements;
import com.asrevo.cvhome.billing.services.entitlement.ExternalEntitlementService;
import com.asrevo.cvhome.catalog.entity.Category;
import com.asrevo.cvhome.catalog.entity.Manufacturer;
import com.asrevo.cvhome.catalog.entity.Product;
import com.asrevo.cvhome.catalog.entity.ProductType;
import com.asrevo.cvhome.catalog.errors.CategoryAlreadyAttachedException;
import com.asrevo.cvhome.catalog.errors.CategoryNotFoundException;
import com.asrevo.cvhome.catalog.errors.CategoryReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ManufacturerReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductTypeReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.model.category.CategoryReference;
import com.asrevo.cvhome.catalog.model.product.LightPersistableProduct;
import com.asrevo.cvhome.catalog.model.product.PersistableProductDefinition;
import com.asrevo.cvhome.catalog.model.product.ProductDescription;
import com.asrevo.cvhome.catalog.model.product.ProductFilter;
import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.catalog.repositories.CategoryRepository;
import com.asrevo.cvhome.catalog.repositories.ManufacturerRepository;
import com.asrevo.cvhome.catalog.repositories.ProductRepository;
import com.asrevo.cvhome.catalog.repositories.ProductTypeRepository;
import com.asrevo.cvhome.catalog.services.image.ProductImageService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The branches of the product service the HTTP layer cannot steer: the subtree widening a one-category filter
 * performs, the reference resolution rules, the entitlement ceiling on create, and the file cleanup on delete.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    private static final LanguageCode EN = new LanguageCode("en");

    private static final String SKU = "SKU-1";

    private static final String NIKE = "NIKE";

    private static final String SHOES = "SHOES";

    private static final String MEN = "MEN";

    private static final String ROOT_LINEAGE = "/1/";

    private static final String SLUG = "slug";

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ManufacturerRepository manufacturerRepository;

    @Mock
    private ProductTypeRepository productTypeRepository;

    @Mock
    private ProductImageService productImageService;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ExternalEntitlementService entitlementService;

    private StoreEntitlements storeEntitlements;

    private ProductServiceImpl service;

    @BeforeEach
    void setUp() {
        // The real guard, over a stubbed billing client: its "no ceiling means unlimited" rule is part of what
        // create() promises, and a mock of the guard would assert nothing about it.
        storeEntitlements = new StoreEntitlements(entitlementService, Duration.ofMinutes(1));
        service = new ProductServiceImpl(productRepository, categoryRepository, manufacturerRepository,
                productTypeRepository, productImageService, productMapper, storeEntitlements);
    }

    private void ceiling(Integer maxProducts) throws Exception {
        when(entitlementService.snapshot(any())).thenReturn(new EntitlementSnapshot(STORE,
                SubscriptionStatus.ACTIVE, true, "plan", null,
                Map.of(EntitlementKey.MAX_PRODUCTS, EntitlementValue.limit(EntitlementKey.MAX_PRODUCTS,
                        maxProducts))));
    }

    private static Product product(long id) {
        Product product = new Product();
        product.setId(id);
        product.setStore(STORE);
        product.setSku(SKU);
        return product;
    }

    private static Category category(long id, String lineage) {
        Category category = new Category();
        category.setId(id);
        category.setCode(MEN);
        category.setLineage(lineage);
        category.setStoreMerchantId(STORE);
        return category;
    }

    private static PersistableProductDefinition definition() {
        PersistableProductDefinition source = new PersistableProductDefinition();
        source.setSku(SKU);
        ProductDescription copy = new ProductDescription();
        copy.setLanguage(EN);
        copy.setName("Shoe");
        source.setDescriptions(List.of(copy));
        return source;
    }

    // ------------------------------------------------------------------------------------------------- reading

    @Test
    void askingForOneCategoryAsksForItsWholeSubtree() {
        // A shopper who clicks "Men" expects the shoes under it too, so a single category id is replaced by the
        // ids of its subtree before the search runs. More than one id is taken literally.
        ProductFilter filter = new ProductFilter();
        filter.setCategoryIds(List.of(1L));
        Category men = category(1L, ROOT_LINEAGE);
        when(categoryRepository.findByStoreAndId(STORE, 1L)).thenReturn(Optional.of(men));
        when(categoryRepository.findSubtree(STORE, ROOT_LINEAGE))
                .thenReturn(List.of(men, category(7L, "/1/7/")));
        when(productRepository.search(eq(STORE), eq(filter), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(product(3L))));
        when(productMapper.toReadable(any(), eq(EN))).thenReturn(new ReadableProduct());

        var page = service.list(STORE, filter, EN, PageRequest.of(0, 10));

        assertThat(filter.getCategoryIds()).containsExactly(1L, 7L);
        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    void twoCategoriesAreTakenLiterally() {
        ProductFilter filter = new ProductFilter();
        filter.setCategoryIds(List.of(1L, 4L));
        when(productRepository.search(eq(STORE), eq(filter), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        service.list(STORE, filter, EN, PageRequest.of(0, 10));

        assertThat(filter.getCategoryIds()).containsExactly(1L, 4L);
        verify(categoryRepository, never()).findSubtree(any(), any());
    }

    @Test
    void anUnknownSkuOrSlugIsNotFound() {
        when(productRepository.findByStoreAndSku(STORE, SKU)).thenReturn(Optional.empty());
        when(productRepository.findByStoreAndFriendlyUrl(STORE, SLUG, EN)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getBySku(STORE, SKU, EN)).isInstanceOf(ProductNotFoundException.class);
        assertThatThrownBy(() -> service.getByFriendlyUrl(STORE, SLUG, EN))
                .isInstanceOf(ProductNotFoundException.class);
    }

    // ------------------------------------------------------------------------------------------------- writing

    @Test
    void createIsRefusedWhenThePlansProductCeilingIsReached() throws Exception {
        ceiling(2);
        when(productRepository.countByStore(STORE)).thenReturn(2);

        assertThatThrownBy(() -> service.create(STORE, definition()))
                .isInstanceOf(EntitlementExceededException.class);

        // nothing is written when the ceiling refuses the create
        verify(productRepository, never()).save(any());
    }

    @Test
    void aPlanWithNoProductCeilingNeverCountsTheRows() throws Exception {
        // The count is the expensive part, so it sits behind a supplier the guard only calls when a ceiling
        // actually applies.
        ceiling(null);
        when(productRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.create(STORE, definition());

        verify(productRepository, never()).countByStore(any());
        verify(productRepository).save(any());
    }

    @Test
    void aBlankBrandOrTypeIsNoReferenceAtAll() throws Exception {
        PersistableProductDefinition source = definition();
        source.setManufacturer("  ");
        source.setType(null);
        when(productRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.create(STORE, source);

        // an empty string is the console's way of clearing the relation, not a code to look up
        verify(manufacturerRepository, never()).findByStoreAndCode(any(), any());
        verify(productTypeRepository, never()).findByStoreAndCode(any(), any());
    }

    @Test
    void aBrandOrTypeThatDoesNotResolveIsRefused() {
        PersistableProductDefinition brandSource = definition();
        brandSource.setManufacturer(NIKE);
        when(manufacturerRepository.findByStoreAndCode(STORE, NIKE)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.create(STORE, brandSource))
                .isInstanceOf(ManufacturerReferenceUnresolvableException.class);

        PersistableProductDefinition typeSource = definition();
        typeSource.setType(SHOES);
        when(productTypeRepository.findByStoreAndCode(STORE, SHOES)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.create(STORE, typeSource))
                .isInstanceOf(ProductTypeReferenceUnresolvableException.class);
    }

    @Test
    void aCategoryReferenceResolvesByIdOrByCode() throws Exception {
        Manufacturer brand = new Manufacturer();
        brand.setCode(NIKE);
        ProductType type = new ProductType();
        type.setCode(SHOES);
        PersistableProductDefinition source = definition();
        source.setManufacturer(NIKE);
        source.setType(SHOES);
        source.setCategories(List.of(new CategoryReference(1L, null), new CategoryReference(null, MEN)));
        when(manufacturerRepository.findByStoreAndCode(STORE, NIKE)).thenReturn(Optional.of(brand));
        when(productTypeRepository.findByStoreAndCode(STORE, SHOES)).thenReturn(Optional.of(type));
        when(categoryRepository.findByStoreAndId(STORE, 1L)).thenReturn(Optional.of(category(1L, ROOT_LINEAGE)));
        when(categoryRepository.findByStoreAndCode(STORE, MEN)).thenReturn(Optional.of(category(2L, "/2/")));
        Product saved = product(5L);
        when(productRepository.save(any())).thenReturn(saved);

        assertThat(service.create(STORE, source)).isEqualTo(5L);
    }

    @Test
    void aCategoryReferenceThatResolvesToNothingIsRefused() {
        PersistableProductDefinition source = definition();
        source.setCategories(List.of(new CategoryReference(null, MEN)));
        when(categoryRepository.findByStoreAndCode(STORE, MEN)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(STORE, source))
                .isInstanceOf(CategoryReferenceUnresolvableException.class);
    }

    @Test
    void patchOnlyMovesTheTwoInlineSwitches() throws Exception {
        Product product = product(3L);
        product.setSku(SKU);
        when(productRepository.findByStoreAndId(STORE, 3L)).thenReturn(Optional.of(product));
        LightPersistableProduct source = new LightPersistableProduct();
        source.setAvailable(false);
        source.setProductShipeable(true);

        service.patch(STORE, 3L, source);

        assertThat(product.isAvailable()).isFalse();
        assertThat(product.isProductShipeable()).isTrue();
        assertThat(product.getSku()).isEqualTo(SKU);
    }

    // ------------------------------------------------------------------------------------------- category members

    @Test
    void attachingACategoryTwiceIsAConflict() throws Exception {
        Product product = product(3L);
        Category men = category(1L, ROOT_LINEAGE);
        when(productRepository.findByStoreAndId(STORE, 3L)).thenReturn(Optional.of(product));
        when(categoryRepository.findByStoreAndId(STORE, 1L)).thenReturn(Optional.of(men));

        service.addToCategory(STORE, 3L, 1L);
        assertThat(product.getCategories()).containsExactly(men);

        assertThatThrownBy(() -> service.addToCategory(STORE, 3L, 1L))
                .isInstanceOf(CategoryAlreadyAttachedException.class);
    }

    @Test
    void removingACategoryNeedsBothToExist() throws Exception {
        Product product = product(3L);
        Category men = category(1L, ROOT_LINEAGE);
        product.setCategories(new HashSet<>(Set.of(men)));
        when(productRepository.findByStoreAndId(STORE, 3L)).thenReturn(Optional.of(product));
        when(categoryRepository.findByStoreAndId(STORE, 1L)).thenReturn(Optional.of(men));

        service.removeFromCategory(STORE, 3L, 1L);
        assertThat(product.getCategories()).isEmpty();

        when(categoryRepository.findByStoreAndId(STORE, 2L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.removeFromCategory(STORE, 3L, 2L))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    // -------------------------------------------------------------------------------------------------- deleting

    @Test
    void deletingAProductTakesItsFilesWithIt() throws Exception {
        Product product = product(3L);
        when(productRepository.findByStoreAndId(STORE, 3L)).thenReturn(Optional.of(product));

        service.delete(STORE, 3L);

        // the rows cascade with the product; the files on the CDN do not, so they are removed first
        verify(productImageService).removeFiles(product);
        verify(productRepository).delete(product);
    }

    @Test
    void deletingAProductOfAnotherStoreIsNotFound() {
        when(productRepository.findByStoreAndId(STORE, 3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(STORE, 3L)).isInstanceOf(ProductNotFoundException.class);
        verify(productRepository, never()).delete(any(Product.class));
    }

}
