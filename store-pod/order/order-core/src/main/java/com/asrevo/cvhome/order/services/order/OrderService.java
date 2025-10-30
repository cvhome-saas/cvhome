package com.asrevo.cvhome.order.services.order;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.order.entity.customer.Customer;
import com.asrevo.cvhome.order.entity.order.Order;
import com.asrevo.cvhome.order.entity.order.OrderList;
import com.asrevo.cvhome.order.entity.order.OrderSummary;
import com.asrevo.cvhome.order.entity.order.OrderTotalSummary;
import com.asrevo.cvhome.order.entity.order.orderstatus.OrderStatusHistory;
import com.asrevo.cvhome.order.entity.shoppingcart.ShoppingCart;
import com.asrevo.cvhome.order.entity.shoppingcart.ShoppingCartItem;
import com.asrevo.cvhome.order.model.order.OrderCriteria;
import com.asrevo.cvhome.order.model.payments.Payment;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;
import java.util.List;

// @TODO ASHRAF

public interface OrderService extends SalesManagerEntityService<Long, Order> {

	void addOrderStatusHistory(Order order, OrderStatusHistory history) throws ServiceException;

	OrderTotalSummary caculateOrderTotal(OrderSummary orderSummary, Customer customer, StoreMerchantId store,
			LanguageCode language) throws ServiceException;

	OrderTotalSummary calculateShoppingCartTotal(ShoppingCart cartModel, Customer customer, StoreMerchantId store,
			LanguageCode language) throws ServiceException;

	OrderTotalSummary calculateShoppingCartTotal(ShoppingCart cartModel, StoreMerchantId store, LanguageCode language)
			throws ServiceException;

	Order processOrder(Order order, Customer customer, List<ShoppingCartItem> items, OrderTotalSummary summary,
			Payment payment, StoreMerchantId store) throws ServiceException;

	Order getOrder(final Long orderId, StoreMerchantId store);

	OrderList getOrders(final OrderCriteria criteria, StoreMerchantId store);

}
