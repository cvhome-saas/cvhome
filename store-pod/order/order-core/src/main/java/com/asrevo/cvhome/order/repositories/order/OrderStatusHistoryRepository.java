package com.asrevo.cvhome.order.repositories.order;

import com.asrevo.cvhome.order.entity.order.orderstatus.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {}
