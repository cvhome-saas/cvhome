package com.asrevo.cvhome.catalog.repositories.product.availability;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.asrevo.cvhome.catalog.entity.product.availability.ProductAvailability;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

public interface ProductAvailabilityRepository extends JpaRepository<ProductAvailability, Long> {

    @Query(value = """
            select distinct p from ProductAvailability p
            left join fetch p.prices pp
            left join fetch pp.descriptions ppd
            join fetch p.product ppr
            where p.id=?1""")
    Optional<ProductAvailability> findProductAvailabilityById(Long availabilityId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = """
            select distinct p from ProductAvailability p
            left join fetch p.prices pp
            left join fetch pp.descriptions ppd
            join fetch p.product ppr
            left join fetch ppr.descriptions pprd
            left join fetch p.productVariant ppi
            where (ppr.sku=?1 or ppi.sku=?1)
            and p.storeMerchantId=?2""")
    List<ProductAvailability> getBySku(String productCode, StoreMerchantId storeMerchantId);

}
