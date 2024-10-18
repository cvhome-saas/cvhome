package com.asrevo.cvhome.store.core.repositories.order;

import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatusHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {

    @Query(
            "select osh from OrderStatusHistory osh"
                    + " join fetch osh.order o"
                    + " where o.id = ?1 order by osh.dateAdded desc")
    List<OrderStatusHistory> findByOrderId(Long id);
}
