package com.asrevo.cvhome.store.core.services.order.orderstatushistory;

import com.asrevo.cvhome.store.core.entity.order.Order;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatusHistory;
import java.util.List;

public interface OrderStatusHistoryService {
    List<OrderStatusHistory> findByOrder(Order order);
}
