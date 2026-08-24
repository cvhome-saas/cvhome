package com.asrevo.cvhome.inventory.repositories;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.entity.ProductAvailability;

public interface ProductAvailabilityRepository extends JpaRepository<ProductAvailability, Long> {

    @Query(value = """
            select distinct p from ProductAvailability p
            left join fetch p.prices pp
            left join fetch pp.descriptions ppd
            where p.id=?1""")
    Optional<ProductAvailability> findProductAvailabilityById(Long availabilityId);

    /**
     * Locked read used by the reservation path. The sku on the availability row is authoritative here — the catalog's
     * product table is no longer reachable, which is why the migration backfills the column.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = """
            select distinct p from ProductAvailability p
            left join fetch p.prices pp
            left join fetch pp.descriptions ppd
            where p.sku=?1
            and p.storeMerchantId=?2""")
    List<ProductAvailability> getBySku(String sku, StoreMerchantId storeMerchantId);

    /**
     * Unlocked bulk read backing the storefront/console price merge.
     */
    @Query(value = """
            select distinct p from ProductAvailability p
            left join fetch p.prices pp
            left join fetch pp.descriptions ppd
            where p.sku in ?1
            and p.storeMerchantId=?2""")
    List<ProductAvailability> findBySkus(Collection<String> skus, StoreMerchantId storeMerchantId);

    @Query(value = """
            select p from ProductAvailability p
            where p.productId=?1 and p.storeMerchantId=?2""")
    List<ProductAvailability> findByProductId(Long productId, StoreMerchantId storeMerchantId);

}
