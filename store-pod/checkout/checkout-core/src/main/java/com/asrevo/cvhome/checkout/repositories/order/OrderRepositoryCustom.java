package com.asrevo.cvhome.checkout.repositories.order;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.checkout.entity.order.OrderList;
import com.asrevo.cvhome.checkout.model.order.OrderCriteria;

public interface OrderRepositoryCustom {

	OrderList listOrders(StoreMerchantId store, OrderCriteria criteria);

}
