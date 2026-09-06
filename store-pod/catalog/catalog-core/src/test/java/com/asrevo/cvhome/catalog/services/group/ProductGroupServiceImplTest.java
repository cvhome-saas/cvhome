package com.asrevo.cvhome.catalog.services.group;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.catalog.entity.Product;
import com.asrevo.cvhome.catalog.entity.ProductGroup;
import com.asrevo.cvhome.catalog.entity.ProductGroupDescription;
import com.asrevo.cvhome.catalog.errors.ProductGroupNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductNotFoundException;
import com.asrevo.cvhome.catalog.model.group.PersistableProductGroup;
import com.asrevo.cvhome.catalog.model.group.ReadableProductGroup;
import com.asrevo.cvhome.catalog.model.product.ReadableMinimalProduct;
import com.asrevo.cvhome.catalog.repositories.ProductGroupRepository;
import com.asrevo.cvhome.catalog.repositories.ProductRepository;
import com.asrevo.cvhome.catalog.services.product.ProductMapper;
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
 * The merchandising strips and the related-items group they share an entity with. The mapper is real here: how a
 * group reads is part of what the service promises, and only the product shape below it is stubbed.
 */
@ExtendWith(MockitoExtension.class)
class ProductGroupServiceImplTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    private static final LanguageCode EN = new LanguageCode("en");

    private static final LanguageCode FR = new LanguageCode("fr");

    private static final String HOME_PAGE = "HOME_PAGE";

    private static final String RELATED = "RELATED_ITEM";

    private static final String HOME_PAGE_NAME = "Home page";

    @Mock
    private ProductGroupRepository productGroupRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    private ProductGroupServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProductGroupServiceImpl(productGroupRepository, productRepository,
                new ProductGroupMapper(productMapper));
    }

    private static Product product(long id) {
        Product product = new Product();
        product.setId(id);
        product.setStore(STORE);
        return product;
    }

    private static ProductGroup group(String code, Product parent, Product... members) {
        ProductGroup group = new ProductGroup(STORE, code, parent);
        group.setId(1L);
        group.setProducts(new ArrayList<>(List.of(members)));
        ProductGroupDescription description = new ProductGroupDescription(group);
        description.setLanguageCode(EN);
        description.setName(HOME_PAGE_NAME);
        group.getDescriptions().add(description);
        return group;
    }

    private void minimalProduct() {
        when(productMapper.toMinimal(any(), any())).thenReturn(new ReadableMinimalProduct());
    }

    // ------------------------------------------------------------------------------------------------- reading

    @Test
    void theListIsASummaryAndTheSingleReadCarriesTheMembers() throws Exception {
        ProductGroup group = group(HOME_PAGE, null, product(3L));
        when(productGroupRepository.findByStore(eq(STORE), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(group)));

        assertThat(service.list(STORE, EN, PageRequest.of(0, 10)).getContent().getFirst().getProducts()).isEmpty();

        minimalProduct();
        when(productGroupRepository.findByStoreAndCode(STORE, HOME_PAGE)).thenReturn(Optional.of(group));
        ReadableProductGroup readable = service.get(STORE, HOME_PAGE, EN, true);
        assertThat(readable.getProducts()).hasSize(1);
        assertThat(readable.getDescriptions()).hasSize(1);
        assertThat(readable.getDescription().getName()).isEqualTo(HOME_PAGE_NAME);
        assertThat(readable.isActive()).isTrue();
    }

    @Test
    void aLanguageTheGroupHasNoCopyInLeavesTheDescriptionUnset() throws Exception {
        minimalProduct();
        when(productGroupRepository.findByStoreAndCode(STORE, HOME_PAGE))
                .thenReturn(Optional.of(group(HOME_PAGE, product(9L))));

        ReadableProductGroup readable = service.get(STORE, HOME_PAGE, FR, false);

        assertThat(readable.getDescription()).isNull();
        assertThat(readable.getDescriptions()).isEmpty();
        // the parent product is mapped too, so a related-items strip knows what it hangs off
        assertThat(readable.getParentProduct()).isNotNull();
    }

    @Test
    void anUnknownCodeIsNotFound() {
        when(productGroupRepository.findByStoreAndCode(STORE, HOME_PAGE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(STORE, HOME_PAGE, EN, false))
                .isInstanceOf(ProductGroupNotFoundException.class);
        assertThatThrownBy(() -> service.delete(STORE, HOME_PAGE))
                .isInstanceOf(ProductGroupNotFoundException.class);
    }

    @Test
    void theStorefrontReadsAnUnknownCodeAsAnEmptyStrip() {
        when(productGroupRepository.findByStoreAndCode(STORE, HOME_PAGE)).thenReturn(Optional.empty());

        ReadableProductGroup strip = service.storefront(STORE, HOME_PAGE, EN);

        assertThat(strip.getCode()).isEqualTo(HOME_PAGE);
        assertThat(strip.isActive()).isFalse();
        assertThat(strip.getProducts()).isEmpty();
        assertThat(strip.getParentProduct()).isNull();
    }

    @Test
    void theStorefrontReadsAKnownCodeInOneLanguage() {
        when(productGroupRepository.findByStoreAndCode(STORE, HOME_PAGE)).thenReturn(Optional.of(group(HOME_PAGE, null)));

        ReadableProductGroup strip = service.storefront(STORE, HOME_PAGE, EN);

        assertThat(strip.isActive()).isTrue();
        assertThat(strip.getDescriptions()).isEmpty();
    }

    // ------------------------------------------------------------------------------------------------- writing

    @Test
    void savingWithoutAnIdUpsertsOnTheCode() throws Exception {
        ProductGroup existing = group(HOME_PAGE, null);
        PersistableProductGroup source = new PersistableProductGroup();
        source.setCode(HOME_PAGE);
        source.setProductIds(List.of(3L));
        when(productGroupRepository.findByStoreAndCode(STORE, HOME_PAGE)).thenReturn(Optional.of(existing));
        when(productRepository.findByStoreAndId(STORE, 3L)).thenReturn(Optional.of(product(3L)));
        when(productGroupRepository.save(existing)).thenReturn(existing);

        assertThat(service.save(STORE, source).getId()).isEqualTo(1L);
        assertThat(existing.getProducts()).hasSize(1);
        // the body replaces the membership wholesale rather than adding to it
        assertThat(existing.getDescriptions()).isEmpty();
    }

    @Test
    void savingAnIdOfAnotherStoresGroupIsNotFound() {
        ProductGroup foreign = new ProductGroup(new StoreMerchantId("other"), HOME_PAGE, null);
        PersistableProductGroup source = new PersistableProductGroup();
        source.setId(1L);
        source.setCode(HOME_PAGE);
        when(productGroupRepository.findById(1L)).thenReturn(Optional.of(foreign));

        assertThatThrownBy(() -> service.save(STORE, source)).isInstanceOf(ProductGroupNotFoundException.class);
    }

    @Test
    void savingWithAProductOfAnotherStoreIsNotFound() {
        PersistableProductGroup source = new PersistableProductGroup();
        source.setCode(HOME_PAGE);
        source.setProductIds(List.of(3L));
        when(productGroupRepository.findByStoreAndCode(STORE, HOME_PAGE)).thenReturn(Optional.empty());
        when(productRepository.findByStoreAndId(STORE, 3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.save(STORE, source)).isInstanceOf(ProductNotFoundException.class);
        verify(productGroupRepository, never()).save(any());
    }

    @Test
    void addingAMemberTwiceLeavesOneRow() throws Exception {
        ProductGroup group = group(HOME_PAGE, null);
        Product member = product(3L);
        when(productGroupRepository.findByStoreAndCode(STORE, HOME_PAGE)).thenReturn(Optional.of(group));
        when(productRepository.findByStoreAndId(STORE, 3L)).thenReturn(Optional.of(member));

        service.addProduct(STORE, HOME_PAGE, 3L);
        service.addProduct(STORE, HOME_PAGE, 3L);

        assertThat(group.getProducts()).containsExactly(member);

        service.removeProduct(STORE, HOME_PAGE, 3L);
        assertThat(group.getProducts()).isEmpty();
    }

    // -------------------------------------------------------------------------------------------- related items

    @Test
    void theFirstRelatedItemCreatesTheProductsOwnGroup() throws Exception {
        Product parent = product(3L);
        Product related = product(4L);
        when(productRepository.findByStoreAndId(STORE, 3L)).thenReturn(Optional.of(parent));
        when(productRepository.findByStoreAndId(STORE, 4L)).thenReturn(Optional.of(related));
        when(productGroupRepository.findByStoreAndParentProductAndCode(STORE, 3L, RELATED))
                .thenReturn(Optional.empty());

        service.addRelated(STORE, 3L, 4L);

        verify(productGroupRepository).save(any(ProductGroup.class));
    }

    @Test
    void relatedItemsOfAProductThatHasNoneAreAnEmptyStrip() {
        when(productGroupRepository.findByStoreAndParentProductAndCode(STORE, 3L, RELATED))
                .thenReturn(Optional.empty());

        ReadableProductGroup strip = service.related(STORE, 3L, EN);

        assertThat(strip.getCode()).isEqualTo(RELATED);
        assertThat(strip.isActive()).isFalse();
        assertThat(strip.getProducts()).isEmpty();
        // removing from a strip that does not exist is still a console error, not a silent no-op
        assertThatThrownBy(() -> service.removeRelated(STORE, 3L, 4L))
                .isInstanceOf(ProductGroupNotFoundException.class);
    }

    @Test
    void removingARelatedItemLeavesTheGroupBehind() throws Exception {
        Product parent = product(3L);
        Product related = product(4L);
        ProductGroup group = group(RELATED, parent, related);
        when(productGroupRepository.findByStoreAndParentProductAndCode(STORE, 3L, RELATED))
                .thenReturn(Optional.of(group));

        service.removeRelated(STORE, 3L, 4L);

        // the strip stays, empty: a merchant who removes the last related item has not deleted the strip
        assertThat(group.getProducts()).isEmpty();
        verify(productGroupRepository, never()).delete(any());
    }

    @Test
    void existsAnswersByCode() {
        when(productGroupRepository.findByStoreAndCode(STORE, HOME_PAGE))
                .thenReturn(Optional.of(group(HOME_PAGE, null)), Optional.empty());

        assertThat(service.exists(STORE, HOME_PAGE)).isTrue();
        assertThat(service.exists(STORE, HOME_PAGE)).isFalse();
    }

}
