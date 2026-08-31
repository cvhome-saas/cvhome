package com.asrevo.cvhome.catalog.repositories;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import org.springframework.data.jpa.domain.Specification;

import com.asrevo.cvhome.catalog.entity.Product;
import com.asrevo.cvhome.catalog.entity.ProductSearchIndex;
import com.asrevo.cvhome.catalog.entity.ProductVariant;
import com.asrevo.cvhome.catalog.entity.ProductVariantOptionValue;
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

    private static final String PRODUCT = "product";

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

    /**
     * Substring match over the product's variant skus — the sku always lives on the variant, so the match is a
     * correlated {@code exists} rather than a column comparison.
     */
    public static Specification<Product> skuLike(String sku) {
        if (sku == null || sku.isBlank()) {
            return always();
        }
        return (root, query, cb) -> {
            Subquery<Integer> sub = query.subquery(Integer.class);
            Root<ProductVariant> variant = sub.from(ProductVariant.class);
            sub.select(cb.literal(1));
            sub.where(cb.and(
                    cb.equal(variant.get(PRODUCT).get(ID), root.get(ID)),
                    cb.like(cb.lower(variant.get("sku")), "%%%s%%".formatted(sku.toLowerCase()))));
            return cb.exists(sub);
        };
    }

    /**
     * The option filter: OR within one option, AND across options, anchored to a single variant — one
     * correlated {@code exists} over the product's variants, with one inner {@code exists} per requested
     * option. "Red AND L" therefore means <em>some one variant</em> is both, not "owns a Red variant and owns
     * an L variant".
     */
    public static Specification<Product> hasOptionValues(Map<Long, List<Long>> valuesByOption) {
        if (valuesByOption == null || valuesByOption.isEmpty()) {
            return always();
        }
        return (root, query, cb) -> {
            Subquery<Integer> variantSub = query.subquery(Integer.class);
            Root<ProductVariant> variant = variantSub.from(ProductVariant.class);
            variantSub.select(cb.literal(1));
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(variant.get(PRODUCT).get(ID), root.get(ID)));
            for (List<Long> valueIds : valuesByOption.values()) {
                Subquery<Integer> valueSub = variantSub.subquery(Integer.class);
                Root<ProductVariantOptionValue> chosen = valueSub.from(ProductVariantOptionValue.class);
                valueSub.select(cb.literal(1));
                valueSub.where(cb.and(
                        cb.equal(chosen.get("variant"), variant),
                        chosen.get("optionValue").get(ID).in(valueIds)));
                predicates.add(cb.exists(valueSub));
            }
            variantSub.where(cb.and(predicates.toArray(new Predicate[0])));
            return cb.exists(variantSub);
        };
    }

    /**
     * Membership of any of the given categories.
     *
     * <p>
     * A correlated {@code exists} rather than a join: a product sitting in several of the selected categories
     * would come back once per category row, and de-duplicating that with {@code distinct} is not free here.
     * Postgres requires every {@code order by} expression of a {@code select distinct} to appear in the select
     * list, which rules out ordering by relevance — a function of the search index, not a column of the row.
     * The {@code exists} keeps one row per product, so neither the {@code distinct} nor that restriction applies.
     * </p>
     */
    public static Specification<Product> inCategories(Collection<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return always();
        }
        return (root, query, cb) -> {
            Subquery<Integer> sub = query.subquery(Integer.class);
            Root<Product> product = sub.correlate(root);
            sub.select(cb.literal(1));
            sub.where(product.join("categories").get(ID).in(categoryIds));
            return cb.exists(sub);
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
