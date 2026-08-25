package com.asrevo.cvhome.catalog.repositories.product.variant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.asrevo.cvhome.catalog.entity.product.variant.ProductVariant;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

public interface PageableProductVariantRepositoty extends PagingAndSortingRepository<ProductVariant, Long> {

    @Query(value = """
            select p from ProductVariant p
            join fetch p.product pr
            left join fetch p.variation pv
            left join fetch pv.productOption pvpo
            left join fetch pv.productOptionValue pvpov
            left join fetch pvpo.descriptions pvpod
            left join fetch pvpov.descriptions pvpovd
            left join fetch p.variationValue pvv
            left join fetch pvv.productOption pvvpo
            left join fetch pvv.productOptionValue pvvpov
            left join fetch pvvpo.descriptions povvpod
            left join fetch p.productVariantGroup pig
            left join fetch pig.images pigi
            left join fetch pigi.descriptions pigid
            where pr.id = ?2 and pr.store = ?1""", countQuery = """
            select p from ProductVariant p
            join fetch p.product pr
            where pr.id = ?2 and pr.store = ?1""")
    Page<ProductVariant> findByProductId(StoreMerchantId storeMerchantId, Long productId, Pageable pageable);

}
