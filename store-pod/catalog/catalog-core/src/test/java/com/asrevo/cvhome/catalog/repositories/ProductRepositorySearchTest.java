package com.asrevo.cvhome.catalog.repositories;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.asrevo.cvhome.catalog.entity.Product;
import com.asrevo.cvhome.catalog.model.product.ProductFilter;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The console's product listing, assembled as one specification.
 *
 * <p>
 * The store predicate is combined unconditionally with whatever the filter asked for, so it holds no matter which
 * facets are set — the same guarantee the search endpoint relies on. A single manufacturer id is wrapped into a
 * one-element list rather than passed as a bare value, and a null one has to become a null list so the filter is
 * skipped entirely rather than becoming "manufacturer in (null)".
 * </p>
 */
class ProductRepositorySearchTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    private ProductRepository repository;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        repository = Mockito.mock(ProductRepository.class, Mockito.CALLS_REAL_METHODS);
        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());
    }

    @SuppressWarnings("unchecked")
    private Specification<Product> specificationFor(ProductFilter filter, Map<Long, List<Long>> valuesByOption) {
        repository.search(STORE, filter, valuesByOption, PageRequest.of(0, 20));

        ArgumentCaptor<Specification<Product>> captor = ArgumentCaptor.captor();
        verify(repository).findAll(captor.capture(), any(Pageable.class));
        return captor.getValue();
    }

    @Test
    void everyListingIsScopedToTheStoreWhateverTheFilterAsked() {
        assertThat(specificationFor(new ProductFilter(), Map.of())).isNotNull();

        verify(repository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void aSingleManufacturerIdIsWrappedIntoAOneElementList() {
        ProductFilter filter = new ProductFilter();
        filter.setManufacturerId(7L);

        assertThat(specificationFor(filter, Map.of())).isNotNull();
    }

    @Test
    void thePageableIsPassedThroughUnchanged() {
        repository.search(STORE, new ProductFilter(), Map.of(), PageRequest.of(2, 50));

        verify(repository).findAll(any(Specification.class), Mockito.eq(PageRequest.of(2, 50)));
    }

    @Test
    void everyFilterFieldReachesTheSpecificationTogether() {
        ProductFilter filter = new ProductFilter();
        filter.setAvailable(true);
        filter.setSku("ABC");
        filter.setManufacturerId(7L);
        filter.setCategoryIds(List.of(1L, 2L));

        assertThat(specificationFor(filter, Map.of(1L, List.of(10L)))).isNotNull();
    }
}
