package com.asrevo.cvhome.catalog.services.option;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.catalog.entity.ProductOption;
import com.asrevo.cvhome.catalog.entity.ProductOptionValue;
import com.asrevo.cvhome.catalog.errors.DuplicateProductOptionException;
import com.asrevo.cvhome.catalog.errors.ProductOptionInUseException;
import com.asrevo.cvhome.catalog.errors.ProductOptionNotFoundException;
import com.asrevo.cvhome.catalog.model.option.PersistableProductOption;
import com.asrevo.cvhome.catalog.model.option.PersistableProductOptionValue;
import com.asrevo.cvhome.catalog.repositories.ProductOptionRepository;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Product options, and the two ways removing one can break a shop that is already selling.
 *
 * <p>
 * An option value is what a variant is addressed by, so dropping one out of an option's value list has to be
 * refused while any variant still carries it — otherwise the variant survives with a dangling reference and the
 * shopper is offered a size that no longer exists. Deleting the whole option is refused on two counts, assigned to
 * a product or used by a variant, and both are checked because a product can hold an option it has not built
 * variants for yet.
 * </p>
 *
 * <p>
 * Duplicate value codes are refused up front on both create and update: two values sharing a code make the variant
 * matrix ambiguous, and the failure would otherwise surface as a constraint violation halfway through a save.
 * </p>
 */
class ProductOptionServiceImplTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");
    private static final LanguageCode ENGLISH = new LanguageCode("en");
    private static final String CODE = "size";
    private static final String SMALL = "s";
    private static final String LARGE = "l";
    private static final String VALUE_CODE = "v%d";

    private ProductOptionRepository options;
    private ProductOptionServiceImpl service;

    @BeforeEach
    void setUp() {
        options = Mockito.mock(ProductOptionRepository.class);
        when(options.save(any())).thenAnswer(it -> it.getArgument(0));
        service = new ProductOptionServiceImpl(options);
    }

    private static ProductOption existing(Long... valueIds) {
        ProductOption option = new ProductOption();
        option.setId(1L);
        option.setStoreMerchantId(STORE);
        option.setCode(CODE);
        for (Long valueId : valueIds) {
            ProductOptionValue value = new ProductOptionValue(option);
            value.setId(valueId);
            value.setCode(VALUE_CODE.formatted(valueId));
            option.getValues().add(value);
        }
        return option;
    }

    private static PersistableProductOption persistable(String... valueCodes) {
        PersistableProductOption source = new PersistableProductOption();
        source.setCode(CODE);
        source.setValues(List.of(valueCodes).stream().map(code -> {
            PersistableProductOptionValue value = new PersistableProductOptionValue();
            value.setCode(code);
            return value;
        }).toList());
        return source;
    }

    private static PersistableProductOption keeping(Long... valueIds) {
        PersistableProductOption source = new PersistableProductOption();
        source.setCode(CODE);
        source.setValues(List.of(valueIds).stream().map(id -> {
            PersistableProductOptionValue value = new PersistableProductOptionValue();
            value.setId(id);
            value.setCode(VALUE_CODE.formatted(id));
            return value;
        }).toList());
        return source;
    }

    @Test
    void listingAndReadingAreBothScopedToTheStore() throws Exception {
        when(options.findByStoreMerchantId(eq(STORE), any())).thenReturn(new PageImpl<>(List.of(existing())));
        when(options.findByStoreAndId(STORE, 1L)).thenReturn(Optional.of(existing()));

        assertThat(service.list(STORE, ENGLISH, Pageable.unpaged()).getContent()).hasSize(1);
        assertThat(service.get(STORE, 1L, ENGLISH).getCode()).isEqualTo(CODE);
    }

    @Test
    void anUnknownOptionIsATypedNotFoundOnEveryPathThatResolvesOne() {
        when(options.findByStoreAndId(STORE, 9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(STORE, 9L, ENGLISH))
                .isInstanceOf(ProductOptionNotFoundException.class);
        assertThatThrownBy(() -> service.update(STORE, 9L, persistable(SMALL)))
                .isInstanceOf(ProductOptionNotFoundException.class);
        assertThatThrownBy(() -> service.delete(STORE, 9L))
                .isInstanceOf(ProductOptionNotFoundException.class);
    }

    @Test
    void anOptionCodeAlreadyUsedInThisStoreIsRefusedBeforeTheInsert() {
        when(options.existsByStoreMerchantIdAndCode(STORE, CODE)).thenReturn(true);

        assertThatThrownBy(() -> service.create(STORE, persistable(SMALL)))
                .isInstanceOf(DuplicateProductOptionException.class);
        verify(options, Mockito.never()).save(any());
    }

    @Test
    void twoValuesSharingACodeAreRefusedOnBothCreateAndUpdate() {
        when(options.existsByStoreMerchantIdAndCode(STORE, CODE)).thenReturn(false);
        when(options.findByStoreAndId(STORE, 1L)).thenReturn(Optional.of(existing()));

        // Ambiguous variant matrix; the alternative is a constraint violation halfway through a save.
        assertThatThrownBy(() -> service.create(STORE, persistable(SMALL, SMALL)))
                .isInstanceOf(DuplicateProductOptionException.class);
        assertThatThrownBy(() -> service.update(STORE, 1L, persistable(SMALL, SMALL)))
                .isInstanceOf(DuplicateProductOptionException.class);
        verify(options, Mockito.never()).save(any());
    }

    @Test
    void distinctValueCodesAreAccepted() throws Exception {
        when(options.existsByStoreMerchantIdAndCode(STORE, CODE)).thenReturn(false);

        service.create(STORE, persistable(SMALL, LARGE));

        verify(options).save(Mockito.argThat(o -> STORE.equals(o.getStoreMerchantId())));
    }

    @Test
    void droppingAValueNoVariantCarriesIsAllowed() throws Exception {
        when(options.findByStoreAndId(STORE, 1L)).thenReturn(Optional.of(existing(10L, 11L)));
        when(options.valueIdsUsedByVariants(List.of(11L))).thenReturn(List.of());

        assertThatCode(() -> service.update(STORE, 1L, keeping(10L))).doesNotThrowAnyException();
    }

    @Test
    void droppingAValueAVariantStillCarriesIsRefused() {
        when(options.findByStoreAndId(STORE, 1L)).thenReturn(Optional.of(existing(10L, 11L)));
        when(options.valueIdsUsedByVariants(List.of(11L))).thenReturn(List.of(11L));

        // The variant would survive with a dangling reference and offer a size that no longer exists.
        assertThatThrownBy(() -> service.update(STORE, 1L, keeping(10L)))
                .isInstanceOf(ProductOptionInUseException.class);
    }

    @Test
    void keepingEveryValueSkipsTheUsageQueryEntirely() throws Exception {
        when(options.findByStoreAndId(STORE, 1L)).thenReturn(Optional.of(existing(10L, 11L)));

        service.update(STORE, 1L, keeping(10L, 11L));

        verify(options, Mockito.never()).valueIdsUsedByVariants(any());
    }

    @Test
    void anOptionAssignedToAProductOrUsedByAVariantCannotBeDeleted() {
        when(options.findByStoreAndId(STORE, 1L)).thenReturn(Optional.of(existing()));
        when(options.isAssignedToProducts(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(STORE, 1L)).isInstanceOf(ProductOptionInUseException.class);

        when(options.isAssignedToProducts(1L)).thenReturn(false);
        when(options.isUsedByVariants(1L)).thenReturn(true);

        // Both are checked: a product can hold an option it has not built variants for yet.
        assertThatThrownBy(() -> service.delete(STORE, 1L)).isInstanceOf(ProductOptionInUseException.class);
        verify(options, Mockito.never()).delete(any());
    }

    @Test
    void anUnusedOptionIsDeleted() throws Exception {
        ProductOption option = existing();
        when(options.findByStoreAndId(STORE, 1L)).thenReturn(Optional.of(option));
        when(options.isAssignedToProducts(anyLong())).thenReturn(false);
        when(options.isUsedByVariants(anyLong())).thenReturn(false);

        service.delete(STORE, 1L);

        verify(options).delete(option);
    }

    @Test
    void existenceIsCheckedByCodeWithinTheStore() {
        when(options.existsByStoreMerchantIdAndCode(STORE, CODE)).thenReturn(true);

        assertThat(service.exists(STORE, CODE)).isTrue();
    }
}
