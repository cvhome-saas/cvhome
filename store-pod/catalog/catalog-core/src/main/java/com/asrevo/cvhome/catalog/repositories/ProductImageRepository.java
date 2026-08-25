package com.asrevo.cvhome.catalog.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.asrevo.cvhome.catalog.entity.ProductImage;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    @Query("""
            select i from ProductImage i join fetch i.product p
            where p.store = ?1 and p.id = ?2 and i.id = ?3""")
    Optional<ProductImage> findByStoreAndProductAndId(StoreMerchantId store, Long productId, Long imageId);
}
