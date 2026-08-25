package com.asrevo.cvhome.catalog.repositories.product.attribute;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOption;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

public interface PageableProductOptionRepository extends PagingAndSortingRepository<ProductOption, Long> {

    @Query(value = """
            select distinct p from ProductOption p left join
             fetch p.descriptions pd where p.storeMerchantId = ?1 and (?2 is null or pd.name
             like %?2%)""", countQuery = """
            select count(p) from ProductOption p left join
             p.descriptions pd where p.storeMerchantId = ?1 and (?2 is null or pd.name like
             %?2%)""")
    Page<ProductOption> listOptions(StoreMerchantId storeMerchantId, String name, Pageable pageable);

}
