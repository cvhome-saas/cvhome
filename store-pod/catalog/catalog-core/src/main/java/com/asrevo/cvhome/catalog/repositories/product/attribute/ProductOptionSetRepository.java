package com.asrevo.cvhome.catalog.repositories.product.attribute;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOptionSet;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.domain.LanguageCode;

public interface ProductOptionSetRepository extends JpaRepository<ProductOptionSet, Long> {

    @Query("""
            select distinct p from ProductOptionSet p
            left join fetch p.option po
            left join fetch po.descriptions pod
            left join fetch p.values pv
            left join fetch pv.descriptions pvd
            where p.storeMerchantId = ?1 and p.id = ?2 and pod.languageCode = ?3""")
    ProductOptionSet findOne(StoreMerchantId storeMerchantId, Long id, LanguageCode language);

    @Query("""
            select distinct p from ProductOptionSet p
            left join fetch p.option po
            left join fetch po.descriptions pod
            left join fetch p.values pv
            left join fetch pv.descriptions pvd
            where p.storeMerchantId = ?1 and pod.languageCode = ?2""")
    List<ProductOptionSet> findByStore(StoreMerchantId storeMerchantId, LanguageCode language);

    @Query("""
            select distinct p from ProductOptionSet p
            left join fetch p.productTypes pt
            left join fetch p.option po
            left join fetch po.descriptions pod
            left join fetch p.values pv
            left join fetch pv.descriptions
            pvd where pt.id= ?1 and p.storeMerchantId = ?2 and pod.languageCode = ?3""")
    List<ProductOptionSet> findByProductType(Long typeId, StoreMerchantId storeMerchantId, LanguageCode language);

    @Query("""
            select p from ProductOptionSet p
            left join fetch p.option po
            left join fetch po.descriptions pod
            left join fetch p.values pv
            left join fetch pv.descriptions pvd
            where p.storeMerchantId = ?1 and p.code = ?2""")
    ProductOptionSet findByCode(StoreMerchantId storeMerchantId, String code);

}
