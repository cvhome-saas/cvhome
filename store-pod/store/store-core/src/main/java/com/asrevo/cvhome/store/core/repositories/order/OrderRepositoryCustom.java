package com.asrevo.cvhome.store.core.repositories.order;


import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.model.order.OrderCriteria;
import com.asrevo.cvhome.store.core.model.order.OrderList;

public interface OrderRepositoryCustom {

    OrderList listByStore(MerchantStore store, OrderCriteria criteria);

    OrderList listOrders(MerchantStore store, OrderCriteria criteria);
}
