package com.asrevo.cvhome.catalog.repositories.product.type;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.asrevo.cvhome.catalog.entity.product.type.ProductType;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;

public interface ProductTypeRepository extends JpaRepository<ProductType, Long> {

    @Query(value = """
            select p from ProductType p
            left join fetch p.descriptions pd
            where p.code=?1 and p.storeMerchantId=?2""")
    ProductType findByCode(String code, StoreMerchantId storeMerchantId);

    @Query(value = """
            select p from ProductType p
            left join fetch p.descriptions pd
            where p.id=?1 and p.storeMerchantId=?2""")
    ProductType findById(Long id, StoreMerchantId storeMerchantId);

    @Query(value = """
            select p from ProductType p
            left join fetch p.descriptions pd
            where p.id in ?1 and p.storeMerchantId=?2""")
    List<ProductType> findByIds(List<Long> id, StoreMerchantId storeMerchantId, LanguageCode language);

}
