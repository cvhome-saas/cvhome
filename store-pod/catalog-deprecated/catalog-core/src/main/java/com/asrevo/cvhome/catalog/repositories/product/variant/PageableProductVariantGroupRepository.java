package com.asrevo.cvhome.catalog.repositories.product.variant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.asrevo.cvhome.catalog.entity.product.variant.ProductVariantGroup;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

public interface PageableProductVariantGroupRepository extends PagingAndSortingRepository<ProductVariantGroup, Long> {

    @Query(value = """
            select p from ProductVariantGroup p
            join fetch p.productVariants pi
            join fetch pi.product pip
            left join fetch p.images pim
            left join fetch pim.descriptions pimd
            where pip.id = ?2 and p.storeMerchantId = ?1""", countQuery = """
            select p from ProductVariantGroup p
            join fetch p.productVariants pi
            join fetch pi.product pip
            where pip.id = ?2 and p.storeMerchantId = ?1""")
    Page<ProductVariantGroup> findByProductId(StoreMerchantId storeMerchantId, Long productId, Pageable pageable);

}
