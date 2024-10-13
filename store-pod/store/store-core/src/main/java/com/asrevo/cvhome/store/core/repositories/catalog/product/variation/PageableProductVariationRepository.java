package com.asrevo.cvhome.store.core.repositories.catalog.product.variation;

import com.asrevo.cvhome.store.core.entity.catalog.product.variation.ProductVariation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PageableProductVariationRepository
        extends PagingAndSortingRepository<ProductVariation, Long> {

    @Query(
            value =
                    """
            select distinct p from ProductVariation p
            join fetch p.merchantStore pm
            left join fetch p.productOption po
            left join fetch po.descriptions
            left join fetch p.productOptionValue pp
            left join fetch pp.descriptions
            where pm.id = ?1 and (?2 is null or p.code like %?2%)""",
            countQuery =
                    """
                    select count(p) from ProductVariation p
                    join p.merchantStore pm
                    where pm.id = ?1 and (?2 is null or p.code like %?2%)""")
    Page<ProductVariation> list(int merchantStoreId, String code, Pageable pageable);
}
