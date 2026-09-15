package com.asrevo.cvhome.catalog.repositories;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.asrevo.cvhome.catalog.entity.ProductVariant;
import com.asrevo.cvhome.commons.domain.Sku;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    /**
     * The sku-addressed resolution every s2s read uses: the variant with its combination fully loaded — values,
     * their owning options, and both sides' translations — so the label block needs no further queries.
     */
    @Query("""
            select distinct v from ProductVariant v
            left join fetch v.optionValues ov
            left join fetch ov.optionValue val
            left join fetch val.descriptions
            left join fetch val.option o
            left join fetch o.descriptions
            where v.storeMerchantId = ?1 and v.sku = ?2""")
    Optional<ProductVariant> findByStoreAndSku(StoreMerchantId store, Sku sku);

    @Query("""
            select distinct v from ProductVariant v
            left join fetch v.optionValues ov
            left join fetch ov.optionValue val
            left join fetch val.descriptions
            left join fetch val.option o
            left join fetch o.descriptions
            where v.storeMerchantId = ?1 and v.sku in ?2""")
    List<ProductVariant> findByStoreAndSkuIn(StoreMerchantId store, Collection<Sku> skus);

    /**
     * The cart-line read: the variants with their products and their label block in one statement; the products'
     * descriptions and images follow in one batch each. Nothing else is touched, so a cart's skus cost three
     * statements however many there are.
     */
    @Query("""
            select distinct v from ProductVariant v
            join fetch v.product p
            left join fetch v.optionValues ov
            left join fetch ov.optionValue val
            left join fetch val.descriptions
            left join fetch val.option o
            left join fetch o.descriptions
            where v.storeMerchantId = ?1 and v.sku in ?2""")
    List<ProductVariant> findCartLinesByStoreAndSkuIn(StoreMerchantId store, Collection<Sku> skus);

    boolean existsByStoreMerchantIdAndSku(StoreMerchantId store, Sku sku);

    /**
     * One product's variants with combinations and labels loaded — the product page and the console matrix.
     */
    @Query("""
            select distinct v from ProductVariant v
            left join fetch v.optionValues ov
            left join fetch ov.optionValue val
            left join fetch val.descriptions
            left join fetch val.option o
            left join fetch o.descriptions
            where v.product.id = ?2 and v.storeMerchantId = ?1""")
    List<ProductVariant> findByProductIdHydrated(StoreMerchantId store, Long productId);
}
