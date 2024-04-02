package com.asrevo.cvhome.store.core.services.order.orderstatushistory;

import com.asrevo.cvhome.store.core.entity.order.Order;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatusHistory;
import com.asrevo.cvhome.store.core.repositories.order.OrderStatusHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderStatusHistoryServiceImpl implements OrderStatusHistoryService {
    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Override
    public List<OrderStatusHistory> findByOrder(Order order) {
        return orderStatusHistoryRepository.findByOrderId(order.getId());
    }
}
