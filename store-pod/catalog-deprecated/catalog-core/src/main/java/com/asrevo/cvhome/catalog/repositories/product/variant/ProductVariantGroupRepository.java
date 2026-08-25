package com.asrevo.cvhome.catalog.repositories.product.variant;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.asrevo.cvhome.catalog.entity.product.variant.ProductVariantGroup;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

public interface ProductVariantGroupRepository extends JpaRepository<ProductVariantGroup, Long> {

    @Query("""
            select distinct p from ProductVariantGroup p
             left join fetch p.productVariants pp
             left join fetch p.images ppi
             left join fetch ppi.descriptions ppid
             where p.id = ?1 and p.storeMerchantId = ?2""")
    Optional<ProductVariantGroup> findOne(Long id, StoreMerchantId storeMerchantId);

}
