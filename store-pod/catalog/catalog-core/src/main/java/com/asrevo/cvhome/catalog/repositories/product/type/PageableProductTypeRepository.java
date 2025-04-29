package com.asrevo.cvhome.catalog.repositories.product.type;

import com.asrevo.cvhome.catalog.entity.product.type.ProductType;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PageableProductTypeRepository
        extends PagingAndSortingRepository<ProductType, Long> {

    @Query(
            value =
                    """
                            select distinct p from ProductType p
                            left join fetch p.descriptions pd
                            where p.storeMerchantId=?1""",
            countQuery =
                    """
                            select count(p) from ProductType p
                            where p.storeMerchantId =
                             ?1""")
    Page<ProductType> listByStore(StoreMerchantId storeMerchantId, Pageable pageable);
}
