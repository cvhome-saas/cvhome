package com.asrevo.cvhome.catalog.services.type;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.catalog.entity.ProductType;
import com.asrevo.cvhome.catalog.entity.ProductTypeDescription;
import com.asrevo.cvhome.catalog.errors.DuplicateProductTypeException;
import com.asrevo.cvhome.catalog.errors.ProductTypeNotFoundException;
import com.asrevo.cvhome.catalog.model.type.PersistableProductType;
import com.asrevo.cvhome.catalog.repositories.ProductTypeRepository;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Product types, and how their per-language descriptions are replaced.
 *
 * <p>
 * {@code ProductTypeMapper.apply} clears the description set and rebuilds it, reusing the existing row for a
 * language it already had. That reuse is the point: replacing the row instead would orphan the old one and, with
 * {@code orphanRemoval}, delete and re-insert on every save — churning ids that the console holds. A language
 * dropped from the request is meant to disappear, and one added is meant to arrive, so both are asserted.
 * </p>
 */
class ProductTypeServiceImplTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");
    private static final LanguageCode ENGLISH = new LanguageCode("en");
    private static final LanguageCode ARABIC = new LanguageCode("ar");
    private static final String CODE = "simple";
    private static final String NAME = "Simple product";

    private ProductTypeRepository types;
    private ProductTypeServiceImpl service;

    @BeforeEach
    void setUp() {
        types = Mockito.mock(ProductTypeRepository.class);
        when(types.save(any())).thenAnswer(it -> it.getArgument(0));
        service = new ProductTypeServiceImpl(types);
    }

    private static ProductType existing() {
        ProductType type = new ProductType();
        type.setId(1L);
        type.setStoreMerchantId(STORE);
        type.setCode(CODE);
        ProductTypeDescription description = new ProductTypeDescription(type);
        description.setLanguageCode(ENGLISH);
        description.setName(NAME);
        type.getDescriptions().add(description);
        return type;
    }

    private static PersistableProductType persistable(LanguageCode... languages) {
        PersistableProductType source = new PersistableProductType();
        source.setCode(CODE);
        source.setDescriptions(List.of(languages).stream().map(language -> {
            var description = new com.asrevo.cvhome.catalog.model.type.ProductTypeDescription();
            description.setLanguage(language);
            description.setName(NAME);
            return description;
        }).toList());
        return source;
    }

    @Test
    void listingAndReadingAreBothScopedToTheStore() throws Exception {
        when(types.findByStoreMerchantId(eq(STORE), any())).thenReturn(new PageImpl<>(List.of(existing())));
        when(types.findByStoreAndId(STORE, 1L)).thenReturn(Optional.of(existing()));

        assertThat(service.list(STORE, ENGLISH, Pageable.unpaged()).getContent()).hasSize(1);
        assertThat(service.get(STORE, 1L, ENGLISH).getCode()).isEqualTo(CODE);
    }

    @Test
    void anUnknownTypeIsATypedNotFoundOnEveryPathThatResolvesOne() {
        when(types.findByStoreAndId(STORE, 9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(STORE, 9L, ENGLISH))
                .isInstanceOf(ProductTypeNotFoundException.class);
        assertThatThrownBy(() -> service.update(STORE, 9L, persistable(ENGLISH)))
                .isInstanceOf(ProductTypeNotFoundException.class);
        assertThatThrownBy(() -> service.delete(STORE, 9L))
                .isInstanceOf(ProductTypeNotFoundException.class);
    }

    @Test
    void aCodeAlreadyUsedInThisStoreIsRefusedBeforeTheInsert() {
        when(types.existsByStoreMerchantIdAndCode(STORE, CODE)).thenReturn(true);

        assertThatThrownBy(() -> service.create(STORE, persistable(ENGLISH)))
                .isInstanceOf(DuplicateProductTypeException.class);
        verify(types, Mockito.never()).save(any());
    }

    @Test
    void aNewTypeIsStampedWithItsStoreAndItsCode() throws Exception {
        when(types.existsByStoreMerchantIdAndCode(STORE, CODE)).thenReturn(false);

        service.create(STORE, persistable(ENGLISH));

        verify(types).save(Mockito.argThat(t -> STORE.equals(t.getStoreMerchantId()) && CODE.equals(t.getCode())));
    }

    @Test
    void anExistingLanguagesRowIsReusedRatherThanReplaced() throws Exception {
        ProductType type = existing();
        ProductTypeDescription before = type.getDescriptions().iterator().next();
        when(types.findByStoreAndId(STORE, 1L)).thenReturn(Optional.of(type));

        service.update(STORE, 1L, persistable(ENGLISH));

        // Same instance: replacing it would orphan the old row and, with orphanRemoval, churn its id on every save.
        assertThat(type.getDescriptions()).hasSize(1).containsExactly(before);
    }

    @Test
    void aLanguageAddedToTheRequestArrivesAndOneDroppedDisappears() throws Exception {
        ProductType type = existing();
        when(types.findByStoreAndId(STORE, 1L)).thenReturn(Optional.of(type));

        service.update(STORE, 1L, persistable(ENGLISH, ARABIC));
        assertThat(type.getDescriptions()).hasSize(2);

        service.update(STORE, 1L, persistable(ARABIC));
        assertThat(type.getDescriptions()).hasSize(1)
                .allSatisfy(d -> assertThat(d.getLanguageCode()).isEqualTo(ARABIC));
    }

    @Test
    void aReadableCarriesOnlyTheAskedLanguageWhenAllLanguagesIsOff() {
        ProductType type = existing();

        assertThat(ProductTypeMapper.toReadable(type, ENGLISH, false).getDescriptions()).isNullOrEmpty();
        assertThat(ProductTypeMapper.toReadable(type, ENGLISH, true).getDescriptions()).hasSize(1);
    }

    @Test
    void aNonLanguageSentinelSelectsNoSingleDescription() {
        // "_all" and "_non" are the language-agnostic rows, not a language anybody reads in.
        assertThat(ProductTypeMapper.toReadable(existing(), LanguageCode.allLanguage(), false).getDescription())
                .isNull();
    }

    @Test
    void existenceIsCheckedByCodeWithinTheStore() {
        when(types.existsByStoreMerchantIdAndCode(STORE, CODE)).thenReturn(true);

        assertThat(service.exists(STORE, CODE)).isTrue();
    }

    @Test
    void deletingAKnownTypeRemovesTheRowItResolved() throws Exception {
        ProductType type = existing();
        when(types.findByStoreAndId(STORE, 1L)).thenReturn(Optional.of(type));

        service.delete(STORE, 1L);

        verify(types).delete(type);
    }
}
