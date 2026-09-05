package com.asrevo.cvhome.checkout.repositories.order.orderproduct;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.asrevo.cvhome.checkout.entity.order.orderproduct.OrderProduct;
import com.asrevo.cvhome.commons.domain.StatisticEntry;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {

    @Query("""
            select new com.asrevo.cvhome.commons.domain.StatisticEntry(null ,op.sku ,count(o.id))
            from OrderProduct op
            join op.order o
            where o.storeMerchantId=:storeId
            and o.datePurchased between :from and :to
            group by op.sku
            """)
    List<StatisticEntry> productStatistic(@Param("from") Instant from, @Param("to") Instant to,
                                          @Param("storeId") StoreMerchantId storeMerchantId);

}
