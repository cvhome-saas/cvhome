package com.asrevo.cvhome.catalog.repositories;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.asrevo.cvhome.catalog.entity.Product;
import com.asrevo.cvhome.catalog.model.product.ProductFilter;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    String ID = "id";

    /**
     * The product with what the readers need already loaded: copy, images, brand and type. Categories stay lazy.
     */
    @Query("""
            select distinct p from Product p
            left join fetch p.descriptions
            left join fetch p.images
            left join fetch p.manufacturer m left join fetch m.descriptions
            left join fetch p.type t left join fetch t.descriptions
            where p.store = ?1 and p.id = ?2""")
    Optional<Product> findByStoreAndId(StoreMerchantId store, Long id);

    @Query("""
            select distinct p from Product p
            left join fetch p.descriptions
            left join fetch p.images
            where p.store = ?1 and p.sku = ?2""")
    Optional<Product> findByStoreAndSku(StoreMerchantId store, String sku);

    boolean existsByStoreAndSku(StoreMerchantId store, String sku);

    int countByStore(StoreMerchantId store);

    /**
     * The storefront's product page: by slug in the shopper's language, visible products only.
     */
    @Query("""
            select distinct p from Product p
            join p.descriptions slug
            left join fetch p.descriptions
            left join fetch p.images
            left join fetch p.manufacturer m left join fetch m.descriptions
            left join fetch p.type t left join fetch t.descriptions
            where p.store = ?1 and slug.seUrl = ?2 and slug.languageCode = ?3 and p.available = true""")
    Optional<Product> findByStoreAndFriendlyUrl(StoreMerchantId store, String friendlyUrl, LanguageCode language);

    @Query("select distinct p from Product p join p.categories c where p.store = ?1 and c.id in ?2")
    List<Product> findByStoreAndCategories(StoreMerchantId store, Collection<Long> categoryIds);

    /**
     * The listing behind the console's product table and the storefront's category page. Sorting comes from the
     * caller's {@code Pageable} and must name direct columns of {@code Product}.
     */
    default Page<Product> search(StoreMerchantId store, ProductFilter filter, Pageable pageable) {
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("store"), store));
            if (filter.getAvailable() != null) {
                predicates.add(cb.equal(root.get("available"), filter.getAvailable()));
            }
            if (filter.getSku() != null && !filter.getSku().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("sku")), "%%%s%%".formatted(filter.getSku().toLowerCase())));
            }
            if (filter.getManufacturerId() != null) {
                predicates.add(cb.equal(root.get("manufacturer").get(ID), filter.getManufacturerId()));
            }
            if (filter.getCategoryIds() != null && !filter.getCategoryIds().isEmpty()) {
                predicates.add(root.join("categories").get(ID).in(filter.getCategoryIds()));
                query.distinct(true);
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        return findAll(spec, pageable);
    }
}
