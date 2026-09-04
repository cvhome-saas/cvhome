package com.asrevo.cvhome.catalog.repositories;

import java.util.List;
import java.util.Map;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.jpa.domain.Specification;

import com.asrevo.cvhome.catalog.entity.Product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The facet counts beside a search result, and the two things they have to get right.
 *
 * <p>
 * They count <em>distinct products</em>: a product joined to three categories produces three rows, so a plain
 * count would report it three times and show a shopper "Shoes (3)" for one pair. And they apply the same
 * specification the result list used, so the numbers describe the search the shopper is looking at rather than
 * the whole catalogue — a specification that yields no predicate (every filter absent) has to be skipped rather
 * than passed as a null where clause.
 * </p>
 */
class ProductFacetRepositoryTest {

    private EntityManager entityManager;
    private CriteriaBuilder builder;
    private CriteriaQuery<Object[]> query;
    private Root<Product> root;
    private ProductFacetRepository repository;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        entityManager = Mockito.mock(EntityManager.class, Mockito.RETURNS_DEEP_STUBS);
        builder = Mockito.mock(CriteriaBuilder.class, Mockito.RETURNS_DEEP_STUBS);
        query = Mockito.mock(CriteriaQuery.class, Mockito.RETURNS_DEEP_STUBS);
        root = Mockito.mock(Root.class, Mockito.RETURNS_DEEP_STUBS);
        TypedQuery<Object[]> typed = Mockito.mock(TypedQuery.class);

        when(entityManager.getCriteriaBuilder()).thenReturn(builder);
        when(builder.createQuery(Object[].class)).thenReturn(query);
        when(query.from(Product.class)).thenReturn(root);
        when(entityManager.createQuery(query)).thenReturn(typed);
        when(typed.getResultList()).thenReturn(List.of(new Object[]{1L, 2L}, new Object[]{2L, 3L}));

        repository = new ProductFacetRepository(entityManager);
    }

    private static Specification<Product> matching() {
        return (r, q, cb) -> Mockito.mock(Predicate.class);
    }

    private static Specification<Product> noFilter() {
        return (r, q, cb) -> null;
    }

    @Test
    void eachFacetGroupsByItsOwnJoinedId() {
        assertThat(repository.countByCategory(matching())).containsEntry(1L, 2L).containsEntry(2L, 3L);
        repository.countByManufacturer(matching());
        repository.countByType(matching());
        repository.countByOptionValue(matching());

        verify(root).join("categories");
        verify(root).join("manufacturer");
        verify(root).join("type");
        verify(root).join("variants");
    }

    @Test
    void productsAreCountedDistinctSoAMultiCategoryProductIsNotCountedTwice() {
        repository.countByCategory(matching());

        verify(builder).countDistinct(any());
        verify(builder, Mockito.never()).count(any());
    }

    @Test
    void aSpecificationThatYieldsNoPredicateIsSkippedRatherThanPassedAsANullWhereClause() {
        repository.countByCategory(noFilter());

        verify(query, Mockito.never()).where(any(Predicate.class));
    }

    @Test
    void aSpecificationThatYieldsAPredicateNarrowsTheCountsToTheSearchInView() {
        repository.countByCategory(matching());

        verify(query).where(any(Predicate.class));
    }

    @Test
    void rowsSharingABucketAreSummedRatherThanColliding() {
        TypedQuery<Object[]> typed = Mockito.mock(TypedQuery.class);
        when(entityManager.createQuery(query)).thenReturn(typed);
        when(typed.getResultList()).thenReturn(List.of(new Object[]{1L, 2L}, new Object[]{1L, 5L}));

        Map<Long, Long> counts = repository.countByType(matching());

        assertThat(counts).containsExactly(Map.entry(1L, 7L));
    }
}
