package com.asrevo.cvhome.checkout.repositories.order;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asrevo.cvhome.checkout.entity.order.orderstatus.OrderStatusHistory;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {

}
