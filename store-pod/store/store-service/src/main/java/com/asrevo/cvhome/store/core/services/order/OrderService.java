package com.asrevo.cvhome.store.core.services.order;

import com.asrevo.cvhome.store.core.entity.customer.Customer;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.order.Order;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatusHistory;
import com.asrevo.cvhome.store.core.entity.payments.Transaction;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.entity.shoppingcart.ShoppingCart;
import com.asrevo.cvhome.store.core.entity.shoppingcart.ShoppingCartItem;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.order.OrderCriteria;
import com.asrevo.cvhome.store.core.model.order.OrderList;
import com.asrevo.cvhome.store.core.model.order.OrderSummary;
import com.asrevo.cvhome.store.core.model.order.OrderTotalSummary;
import com.asrevo.cvhome.store.core.model.payments.Payment;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;
import org.apache.commons.lang3.Validate;

import java.util.List;

// @TODO ASHRAF

public interface OrderService extends SalesManagerEntityService<Long, Order> {
    void addOrderStatusHistory(Order order, OrderStatusHistory history)
            throws ServiceException;

    OrderTotalSummary caculateOrderTotal(OrderSummary orderSummary,
                                         Customer customer, MerchantStore store, Language language)
            throws ServiceException;

    OrderTotalSummary caculateOrderTotal(OrderSummary orderSummary,
                                         MerchantStore store, Language language) throws ServiceException;

    OrderTotalSummary calculateShoppingCartTotal(ShoppingCart cartModel, Customer customer, MerchantStore store, Language language) throws ServiceException;

    OrderTotalSummary calculateShoppingCartTotal(ShoppingCart cartModel, MerchantStore store, Language language) throws ServiceException;

    Order processOrder(Order order, Customer customer,
                       List<ShoppingCartItem> items, OrderTotalSummary summary,
                       Payment payment, MerchantStore store) throws ServiceException;

    Order processOrder(Order order, Customer customer,
                       List<ShoppingCartItem> items, OrderTotalSummary summary,
                       Payment payment, Transaction transaction, MerchantStore store)
            throws ServiceException;

    Order getOrder(final Long orderId, MerchantStore store);

    OrderList listByStore(final MerchantStore store, final OrderCriteria criteria);

    OrderList getOrders(final OrderCriteria criteria, MerchantStore store);

    void saveOrUpdate(final Order order) throws ServiceException;

    boolean hasDownloadFiles(Order order) throws ServiceException;
}
