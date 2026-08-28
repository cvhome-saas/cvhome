package com.asrevo.cvhome.catalog.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.asrevo.cvhome.catalog.entity.ProductGroup;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

public interface ProductGroupRepository extends JpaRepository<ProductGroup, Long> {

    /**
     * A store-level group: the one with that code and no parent product.
     */
    @Query("""
            select g from ProductGroup g left join fetch g.descriptions
            where g.storeMerchantId = ?1 and g.code = ?2 and g.parentProduct is null""")
    Optional<ProductGroup> findByStoreAndCode(StoreMerchantId store, String code);

    /**
     * A product's own group of that code — its related items.
     */
    @Query("""
            select g from ProductGroup g left join fetch g.descriptions
            where g.storeMerchantId = ?1 and g.parentProduct.id = ?2 and g.code = ?3""")
    Optional<ProductGroup> findByStoreAndParentProductAndCode(StoreMerchantId store, Long productId, String code);

    @Query(value = "select g from ProductGroup g where g.storeMerchantId = ?1 and g.parentProduct is null",
            countQuery = "select count(g) from ProductGroup g where g.storeMerchantId = ?1 and g.parentProduct is null")
    Page<ProductGroup> findByStore(StoreMerchantId store, Pageable pageable);
}
