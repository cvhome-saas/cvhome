package com.asrevo.cvhome.order.repositories.order;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.order.entity.order.OrderList;
import com.asrevo.cvhome.order.model.order.OrderCriteria;

public interface OrderRepositoryCustom {

    OrderList listOrders(StoreMerchantId store, OrderCriteria criteria);
}
