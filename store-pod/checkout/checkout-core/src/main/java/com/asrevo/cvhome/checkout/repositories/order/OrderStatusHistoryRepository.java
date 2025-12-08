package com.asrevo.cvhome.checkout.repositories.order;

import com.asrevo.cvhome.checkout.entity.order.orderstatus.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {

}
