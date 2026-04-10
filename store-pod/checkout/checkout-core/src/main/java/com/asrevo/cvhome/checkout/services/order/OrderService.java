package com.asrevo.cvhome.checkout.services.order;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.checkout.entity.customer.Customer;
import com.asrevo.cvhome.checkout.entity.order.Order;
import com.asrevo.cvhome.checkout.entity.order.OrderSummary;
import com.asrevo.cvhome.checkout.entity.order.OrderTotalSummary;
import com.asrevo.cvhome.checkout.entity.order.orderstatus.OrderStatusHistory;
import com.asrevo.cvhome.checkout.entity.shoppingcart.ShoppingCart;
import com.asrevo.cvhome.checkout.entity.shoppingcart.ShoppingCartItem;
import com.asrevo.cvhome.checkout.model.order.OrderCriteria;
import com.asrevo.cvhome.checkout.model.payments.Payment;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;
import org.springframework.data.domain.Page;

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

	Page<Order> getOrders(final OrderCriteria criteria, StoreMerchantId store);

}
