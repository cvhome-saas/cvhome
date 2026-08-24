package com.asrevo.cvhome.inventory.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.entity.ProductPrice;

public interface ProductPriceRepository extends JpaRepository<ProductPrice, Long> {

    @Query(value = """
            select distinct p from ProductPrice p
            left join fetch p.productAvailability pa
            left join fetch p.descriptions pd
            where pa.sku=?1 and pa.storeMerchantId=?2""")
    List<ProductPrice> findByProduct(String sku, StoreMerchantId storeMerchantId);

    @Query(value = """
            select distinct p from ProductPrice p
            left join fetch p.productAvailability pa
            left join fetch p.descriptions pd
            where pa.sku=?1
            and p.id=?2 and pa.storeMerchantId=?3""")
    ProductPrice findByProduct(String sku, Long priceId, StoreMerchantId storeMerchantId);

    @Query(value = """
            select distinct p from ProductPrice p
            left join fetch p.productAvailability pa
            left join fetch p.descriptions pd
            where pa.sku=?1
            and pa.id=?2 and pa.storeMerchantId=?3""")
    List<ProductPrice> findByProductInventoty(String sku, Long productInventory, StoreMerchantId storeMerchantId);

}
