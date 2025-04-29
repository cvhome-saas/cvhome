package com.asrevo.cvhome.catalog.repositories.product.attribute;

import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOption;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {
    @Query(
            """
                    select p from ProductOption p left join fetch
                     p.descriptions pd where p.id = ?2 and p.storeMerchantId = ?1""")
    ProductOption findOne(StoreMerchantId storeMerchantId, Long id);

    @Query(
            """
                    select p from ProductOption p left join fetch
                     p.descriptions pd where p.storeMerchantId = ?1 and p.code = ?2""")
    ProductOption findByCode(StoreMerchantId storeMerchantId, String optionCode);
}
