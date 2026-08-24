package com.asrevo.cvhome.catalog.repositories.product.variation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.asrevo.cvhome.catalog.entity.product.variation.ProductVariation;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

public interface PageableProductVariationRepository extends PagingAndSortingRepository<ProductVariation, Long> {

    @Query(value = """
            select distinct p from ProductVariation p
            left join fetch p.productOption po
            left join fetch po.descriptions
            left join fetch p.productOptionValue pp
            left join fetch pp.descriptions
            where p.storeMerchantId = ?1 and (?2 is null or p.code like %?2%)""", countQuery = """
            select count(p) from ProductVariation p
            where p.storeMerchantId = ?1 and (?2 is null or p.code like %?2%)""")
    Page<ProductVariation> list(StoreMerchantId storeMerchantId, String code, Pageable pageable);

}
