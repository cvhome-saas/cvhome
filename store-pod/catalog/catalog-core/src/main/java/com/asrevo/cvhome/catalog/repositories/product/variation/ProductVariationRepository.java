package com.asrevo.cvhome.catalog.repositories.product.variation;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.asrevo.cvhome.catalog.entity.product.variation.ProductVariation;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

public interface ProductVariationRepository extends JpaRepository<ProductVariation, Long> {

    @Query("""
            select distinct p from ProductVariation p
            left join fetch p.productOption po
            left join fetch po.descriptions pod
            left join fetch p.productOptionValue pv
            left join fetch pv.descriptions pvd
            where p.storeMerchantId = ?1
            and p.id = ?2
            and pod.languageCode = ?3""")
    Optional<ProductVariation> findOne(StoreMerchantId storeMerchantId, Long id, LanguageCode language);

    @Query("""
            select distinct p from ProductVariation p
            left join fetch p.productOption po
            left join fetch po.descriptions pod
            left join fetch p.productOptionValue pv
            left join fetch pv.descriptions pvd
            where p.storeMerchantId = ?1
            and p.id = ?2""")
    Optional<ProductVariation> findOne(StoreMerchantId storeMerchantId, Long id);

    @Query("""
            select distinct p from ProductVariation p
            left join fetch p.productOption po
            left join fetch po.descriptions pod
            left join fetch p.productOptionValue pv
            left join fetch pv.descriptions pvd
            where p.code = ?1
            and p.storeMerchantId = ?2""")
    Optional<ProductVariation> findByCode(String code, StoreMerchantId storeMerchantId);

    @Query("""
            select distinct p from ProductVariation p
            left join fetch p.productOption po
            left join fetch po.descriptions pod
            left join fetch p.productOptionValue pv
            left join fetch pv.descriptions pvd
            where p.storeMerchantId = ?1 and p.id in (?2)""")
    List<ProductVariation> findByIds(StoreMerchantId storeMerchantId, List<Long> ids);

}
