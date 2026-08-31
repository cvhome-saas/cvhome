package com.asrevo.cvhome.catalog.repositories;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
     * Loads a page's worth of products with everything the mapper reads already attached.
     *
     * <p>
     * Deliberately unpaged and driven by a list of ids the caller has already paged: fetch-joining collections
     * and paginating in the same query makes Hibernate fall back to paging in memory, which on a large
     * catalogue means reading all of it to return twenty-four rows. Two cheap queries beat that.
     * </p>
     */
    @Query("""
            select distinct p from Product p
            left join fetch p.descriptions
            left join fetch p.images
            left join fetch p.manufacturer m left join fetch m.descriptions
            left join fetch p.type t left join fetch t.descriptions
            where p.id in ?1""")
    List<Product> findAllHydrated(Collection<Long> ids);

    /**
     * The listing behind the console's product table and the storefront's category page. Sorting comes from the
     * caller's {@code Pageable} and must name direct columns of {@code Product}; {@code valuesByOption} is the
     * filter's {@code optionValueIds} already grouped by owning option (the service resolves that once).
     */
    default Page<Product> search(StoreMerchantId store, ProductFilter filter,
                                 Map<Long, List<Long>> valuesByOption, Pageable pageable) {
        return findAll(Specification.allOf(
                ProductSpecifications.inStore(store),
                ProductSpecifications.available(filter.getAvailable()),
                ProductSpecifications.skuLike(filter.getSku()),
                ProductSpecifications.byManufacturers(
                        filter.getManufacturerId() == null ? null : List.of(filter.getManufacturerId())),
                ProductSpecifications.inCategories(filter.getCategoryIds()),
                ProductSpecifications.hasOptionValues(valuesByOption)), pageable);
    }
}
