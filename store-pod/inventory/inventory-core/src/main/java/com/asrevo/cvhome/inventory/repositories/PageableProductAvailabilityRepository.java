package com.asrevo.cvhome.inventory.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.entity.ProductAvailability;

public interface PageableProductAvailabilityRepository
        extends PagingAndSortingRepository<ProductAvailability, Long> {

    @Query(value = """
            select distinct p from ProductAvailability p
            left join fetch p.prices pp
            left join fetch pp.descriptions ppd
            where p.productId=?1 and p.storeMerchantId=?2""", countQuery = """
            select count(p) from ProductAvailability p
            where p.productId=?1 and p.storeMerchantId=?2""")
    Page<ProductAvailability> getByProductId(Long productId, StoreMerchantId storeMerchantId, Pageable pageable);

    @Query(value = """
            select distinct p from ProductAvailability p
            left join fetch p.prices pp
            left join fetch pp.descriptions ppd
            where p.sku=?1""", countQuery = """
            select count(p) from ProductAvailability p
            where p.sku=?1""")
    Page<ProductAvailability> getBySku(String sku, Pageable pageable);

}
