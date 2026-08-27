package com.asrevo.cvhome.catalog.repositories;

import java.util.Collection;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import org.springframework.data.jpa.domain.Specification;

import com.asrevo.cvhome.catalog.entity.Product;
import com.asrevo.cvhome.catalog.entity.ProductSearchIndex;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * The filter vocabulary of the product tables — the console listing, the storefront category page and search all
 * compose their queries out of these, so there is one definition of what "in this category" or "available" means
 * rather than one per caller.
 */
public final class ProductSpecifications {

    private static final String ID = "id";

    private static final String STORE = "store";

    private static final String PRODUCT_ID = "productId";

    private static final String LANGUAGE_CODE = "languageCode";

    private static final String SEARCH_DOCUMENT = "searchDocument";

    private ProductSpecifications() {
    }

    /**
     * A predicate that narrows nothing.
     *
     * <p>
     * Every factory below returns this rather than {@code null} when its filter is absent, so callers can compose
     * unconditionally — {@code Specification.and(null)} is rejected, and making each call site check first would
     * put the same {@code if} in every caller.
     * </p>
     */
    public static Specification<Product> always() {
        return (root, query, cb) -> null;
    }

    public static Specification<Product> inStore(StoreMerchantId store) {
        return (root, query, cb) -> cb.equal(root.get(STORE), store);
    }

    public static Specification<Product> available(Boolean available) {
        return available == null ? always() : (root, query, cb) -> cb.equal(root.get("available"), available);
    }

    public static Specification<Product> skuLike(String sku) {
        if (sku == null || sku.isBlank()) {
            return always();
        }
        return (root, query, cb) -> cb.like(cb.lower(root.get("sku")), "%%%s%%".formatted(sku.toLowerCase()));
    }

    public static Specification<Product> inCategories(Collection<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return always();
        }
        return (root, query, cb) -> {
            if (query != null) {
                query.distinct(true);
            }
            return root.join("categories").get(ID).in(categoryIds);
        };
    }

    public static Specification<Product> byManufacturers(Collection<Long> manufacturerIds) {
        if (manufacturerIds == null || manufacturerIds.isEmpty()) {
            return always();
        }
        return (root, query, cb) -> root.get("manufacturer").get(ID).in(manufacturerIds);
    }

    public static Specification<Product> byTypes(Collection<Long> typeIds) {
        if (typeIds == null || typeIds.isEmpty()) {
            return always();
        }
        return (root, query, cb) -> root.get("type").get(ID).in(typeIds);
    }

    /**
     * The full-text match.
     *
     * <p>
     * Written as a correlated {@code exists} that selects a literal, for two reasons. The root stays
     * {@code Product}, so every other predicate here, the {@code Page<Product>} and the existing mapper all keep
     * working untouched. And no {@code tsvector} ever reaches a select list — putting one there would mean
     * fetching the whole document for every row of every page.
     * </p>
     */
    public static Specification<Product> matchesText(String queryText, StoreMerchantId store, LanguageCode language) {
        if (queryText == null || queryText.isBlank()) {
            return always();
        }
        return (root, query, cb) -> {
            Subquery<Integer> sub = query.subquery(Integer.class);
            Root<ProductSearchIndex> index = sub.from(ProductSearchIndex.class);
            sub.select(cb.literal(1));
            sub.where(cb.and(
                    cb.equal(index.get(PRODUCT_ID), root.get(ID)),
                    cb.equal(index.get(STORE), store),
                    cb.equal(index.get(LANGUAGE_CODE), language.code()),
                    cb.isTrue(cb.function("fts_match", Boolean.class, index.get(SEARCH_DOCUMENT),
                            cb.literal(language.code()), cb.literal(queryText)))));
            return cb.exists(sub);
        };
    }

    /**
     * The rank of this product's match, as a correlated scalar subquery.
     *
     * <p>
     * Only ever used in {@code order by}, and only for the rows a page actually returns, so the extra index
     * lookup it costs is paid per result rather than per candidate.
     * </p>
     */
    public static Expression<Float> relevance(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder cb,
                                              String queryText, StoreMerchantId store, LanguageCode language) {
        Subquery<Float> sub = query.subquery(Float.class);
        Root<ProductSearchIndex> index = sub.from(ProductSearchIndex.class);
        sub.select(cb.function("fts_rank", Float.class, index.get(SEARCH_DOCUMENT),
                cb.literal(language.code()), cb.literal(queryText)));
        sub.where(cb.and(
                cb.equal(index.get(PRODUCT_ID), root.get(ID)),
                cb.equal(index.get(STORE), store),
                cb.equal(index.get(LANGUAGE_CODE), language.code())));
        return sub;
    }
}
