package com.asrevo.cvhome.catalog.repositories;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import com.asrevo.cvhome.catalog.entity.Product;

import lombok.RequiredArgsConstructor;

/**
 * Counts how the current results break down, so a filter rail can show numbers instead of guesses.
 *
 * <p>
 * Each dimension is one grouped query over the very same {@link Specification} that produced the page — which is
 * the only way the counts and the results can agree. Written against the Criteria API rather than as JPQL because
 * the predicate arrives as a composed {@code Specification}, not as text.
 * </p>
 */
@Repository
@RequiredArgsConstructor
public class ProductFacetRepository {

    private static final String ID = "id";

    private final EntityManager entityManager;

    public Map<Long, Long> countByCategory(Specification<Product> spec) {
        return countGroupedBy(spec, root -> root.join("categories").get(ID));
    }

    public Map<Long, Long> countByManufacturer(Specification<Product> spec) {
        return countGroupedBy(spec, root -> root.join("manufacturer").get(ID));
    }

    public Map<Long, Long> countByType(Specification<Product> spec) {
        return countGroupedBy(spec, root -> root.join("type").get(ID));
    }

    /**
     * Bucket by option value across the results' variants. {@code countDistinct} keeps a product with several
     * matching variants (two Red combinations, say) a single count.
     */
    public Map<Long, Long> countByOptionValue(Specification<Product> spec) {
        return countGroupedBy(spec, root -> root.join("variants").join("optionValues")
                .join("optionValue").get(ID));
    }

    /**
     * @param group picks the column to bucket by; an inner join, so products without a brand or a type simply do
     *              not appear in that dimension rather than forming a null bucket nobody can click.
     */
    private Map<Long, Long> countGroupedBy(Specification<Product> spec, Function<Root<Product>, Expression<Long>> group) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<Product> root = query.from(Product.class);
        Expression<Long> bucket = group.apply(root);

        Predicate predicate = spec.toPredicate(root, query, cb);
        if (predicate != null) {
            query.where(predicate);
        }
        // countDistinct, because a product joined to several categories would otherwise be counted once per
        // category row the join produced.
        query.multiselect(bucket, cb.countDistinct(root.get(ID))).groupBy(bucket);

        List<Object[]> rows = entityManager.createQuery(query).getResultList();
        return rows.stream().collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1], Long::sum));
    }
}
