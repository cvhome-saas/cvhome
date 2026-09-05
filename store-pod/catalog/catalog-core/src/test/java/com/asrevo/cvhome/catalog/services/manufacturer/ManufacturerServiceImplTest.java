package com.asrevo.cvhome.catalog.services.manufacturer;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.ReflectionUtils;

import com.asrevo.cvhome.catalog.entity.Category;
import com.asrevo.cvhome.catalog.entity.Manufacturer;
import com.asrevo.cvhome.catalog.entity.ManufacturerDescription;
import com.asrevo.cvhome.catalog.errors.CategoryNotFoundException;
import com.asrevo.cvhome.catalog.errors.ManufacturerNotFoundException;
import com.asrevo.cvhome.catalog.model.manufacturer.PersistableManufacturer;
import com.asrevo.cvhome.catalog.repositories.CategoryRepository;
import com.asrevo.cvhome.catalog.repositories.ManufacturerRepository;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Brands, and the one save that has to invalidate a search index.
 *
 * <p>
 * A brand name is part of the search document of every product carrying it, so renaming one makes all of them
 * stale. {@code renamed()} raises the event that rebuilds them — and it must fire only when a name actually
 * changed. A new brand has no products yet, and a save that moved only the code or the sort order has not changed
 * what anyone can search for; firing on either would rebuild the whole catalogue's index for nothing, and failing
 * to fire on a real rename leaves the old name searchable indefinitely.
 * </p>
 */
class ManufacturerServiceImplTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");
    private static final LanguageCode ENGLISH = new LanguageCode("en");
    private static final String CODE = "acme";
    private static final String OLD_NAME = "Acme";
    private static final String NEW_NAME = "Acme Corp";
    private static final String SUBTREE = "1/";

    private ManufacturerRepository manufacturers;
    private CategoryRepository categories;
    private ManufacturerServiceImpl service;

    @BeforeEach
    void setUp() {
        manufacturers = Mockito.mock(ManufacturerRepository.class);
        categories = Mockito.mock(CategoryRepository.class);
        when(manufacturers.save(any())).thenAnswer(it -> it.getArgument(0));
        service = new ManufacturerServiceImpl(manufacturers, categories);
    }

    /** {@code domainEvents()} is protected on Spring's AbstractAggregateRoot; the rename event is what it holds. */
    @SuppressWarnings("unchecked")
    private static Collection<Object> eventsOf(Manufacturer manufacturer) {
        Method method = ReflectionUtils.findMethod(AbstractAggregateRoot.class, "domainEvents");
        ReflectionUtils.makeAccessible(method);
        return (Collection<Object>) ReflectionUtils.invokeMethod(method, manufacturer);
    }

    private static Manufacturer existing(String name) {
        Manufacturer manufacturer = new Manufacturer();
        manufacturer.setId(1L);
        manufacturer.setStoreMerchantId(STORE);
        manufacturer.setCode(CODE);
        ManufacturerDescription description = new ManufacturerDescription(manufacturer);
        description.setLanguageCode(ENGLISH);
        description.setName(name);
        manufacturer.getDescriptions().add(description);
        return manufacturer;
    }

    private static PersistableManufacturer persistable(Long id, String name) {
        PersistableManufacturer source = new PersistableManufacturer();
        source.setId(id);
        source.setCode(CODE);
        var description = new com.asrevo.cvhome.catalog.model.manufacturer.ManufacturerDescription();
        description.setLanguage(ENGLISH);
        description.setName(name);
        source.setDescriptions(List.of(description));
        return source;
    }

    @Test
    void anEmptyNameFilterListsEveryBrandRatherThanSearchingForNothing() {
        when(manufacturers.findByStore(eq(STORE), any())).thenReturn(new PageImpl<>(List.of(existing(OLD_NAME))));

        assertThat(service.list(STORE, null, ENGLISH, Pageable.unpaged()).getContent()).hasSize(1);
        service.list(STORE, "   ", ENGLISH, Pageable.unpaged());

        verify(manufacturers, Mockito.times(2)).findByStore(eq(STORE), any());
        verify(manufacturers, Mockito.never()).findByStoreAndName(any(), any(), any());
    }

    @Test
    void aNameFilterIsTrimmedBeforeItReachesTheQuery() {
        when(manufacturers.findByStoreAndName(eq(STORE), eq(OLD_NAME), any()))
                .thenReturn(new PageImpl<>(List.of()));

        service.list(STORE, "  %s  ".formatted(OLD_NAME), ENGLISH, Pageable.unpaged());

        verify(manufacturers).findByStoreAndName(eq(STORE), eq(OLD_NAME), any());
    }

    @Test
    void everyReadIsScopedToTheStore() throws Exception {
        when(manufacturers.findByStoreAndId(STORE, 1L)).thenReturn(Optional.of(existing(OLD_NAME)));

        assertThat(service.get(STORE, 1L, ENGLISH).getCode()).isEqualTo(CODE);
        verify(manufacturers).findByStoreAndId(STORE, 1L);
    }

    @Test
    void anUnknownBrandIsATypedNotFound() {
        when(manufacturers.findByStoreAndId(STORE, 9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(STORE, 9L, ENGLISH))
                .isInstanceOf(ManufacturerNotFoundException.class);
        assertThatThrownBy(() -> service.delete(STORE, 9L))
                .isInstanceOf(ManufacturerNotFoundException.class);
    }

    @Test
    void listingByCategoryResolvesTheCategoryInThisStoreFirst() throws Exception {
        Category category = Mockito.mock(Category.class);
        when(category.subtreePrefix()).thenReturn(SUBTREE);
        when(categories.findByStoreAndId(STORE, 2L)).thenReturn(Optional.of(category));
        when(manufacturers.findByCategorySubtree(STORE, SUBTREE)).thenReturn(List.of(existing(OLD_NAME)));

        assertThat(service.listByCategory(STORE, 2L, ENGLISH)).hasSize(1);
    }

    @Test
    void anUnknownCategoryIsItsOwnTypedNotFound() {
        when(categories.findByStoreAndId(STORE, 9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.listByCategory(STORE, 9L, ENGLISH))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void aNewBrandIsStampedWithItsStoreAndRaisesNoRenameEvent() throws Exception {
        Long id = service.save(STORE, persistable(null, OLD_NAME));

        assertThat(id).isNull();
        // A new brand has no products carrying it, so there is nothing to reindex.
        verify(manufacturers).save(Mockito.argThat(m -> STORE.equals(m.getStoreMerchantId())
                && eventsOf(m).isEmpty()));
    }

    @Test
    void anIdOfZeroOrLessIsTreatedAsNewRatherThanLookedUp() throws Exception {
        service.save(STORE, persistable(0L, OLD_NAME));
        service.save(STORE, persistable(-1L, OLD_NAME));

        verify(manufacturers, Mockito.never()).findByStoreAndId(any(), any());
    }

    @Test
    void arenameRaisesTheEventThatRebuildsEveryCarryingProductsSearchDocument() throws Exception {
        when(manufacturers.findByStoreAndId(STORE, 1L)).thenReturn(Optional.of(existing(OLD_NAME)));

        service.save(STORE, persistable(1L, NEW_NAME));

        verify(manufacturers).save(Mockito.argThat(m -> !eventsOf(m).isEmpty()));
    }

    @Test
    void aSaveThatLeavesTheNamesAloneDoesNotRebuildAnything() throws Exception {
        when(manufacturers.findByStoreAndId(STORE, 1L)).thenReturn(Optional.of(existing(OLD_NAME)));

        // Same name, different sort order: nothing anyone can search for has changed.
        PersistableManufacturer source = persistable(1L, OLD_NAME);
        source.setOrder(9);
        service.save(STORE, source);

        verify(manufacturers).save(Mockito.argThat(m -> eventsOf(m).isEmpty()));
    }

    @Test
    void existenceIsCheckedByCodeWithinTheStore() {
        when(manufacturers.existsByStoreMerchantIdAndCode(STORE, CODE)).thenReturn(true);

        assertThat(service.exists(STORE, CODE)).isTrue();
    }

    @Test
    void deletingAKnownBrandRemovesTheRowItResolved() throws Exception {
        Manufacturer brand = existing(OLD_NAME);
        when(manufacturers.findByStoreAndId(STORE, 1L)).thenReturn(Optional.of(brand));

        service.delete(STORE, 1L);

        verify(manufacturers).delete(brand);
    }
}
