package com.asrevo.cvhome.catalog.repositories.product.availability;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.asrevo.cvhome.catalog.entity.product.availability.ProductAvailability;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

public interface PageableProductAvailabilityRepository extends PagingAndSortingRepository<ProductAvailability, Long> {

    @Query(value = """
            select distinct p from ProductAvailability p
            left join fetch p.prices pp
            left join fetch pp.descriptions ppd
            join fetch p.product ppr
            left join fetch ppr.variants ppri
            left join fetch ppri.availabilities ppria
            left join fetch ppria.prices ppriap
            left join fetch ppriap.descriptions ppriapd
            where ppr.id=?1 and p.storeMerchantId=?2""", countQuery = """
            select  count(p) from ProductAvailability p
            join p.product ppr left join ppr.variants ppri left join ppri.availabilities ppria where ppr.id=?1
            and p.storeMerchantId=?2""")
    Page<ProductAvailability> getByProductId(Long productId, StoreMerchantId storeMerchantId, Pageable pageable);

    @Query(value = """
            select distinct p from ProductAvailability p
            
            left join fetch p.prices pp
            left join fetch pp.descriptions ppd join fetch p.product ppr
            left join fetch p.productVariant ppi
            where ppr.sku=?1 or ppi.sku=?1""", countQuery = """
            select  count(p) from ProductAvailability p join p.product ppr left join p.productVariant ppi
            where ppr.sku=?1 or ppi.sku=?1""")
    Page<ProductAvailability> getBySku(String productCode, Pageable pageable);

}
