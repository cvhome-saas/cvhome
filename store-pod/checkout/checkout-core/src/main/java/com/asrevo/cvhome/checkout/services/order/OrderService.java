package com.asrevo.cvhome.checkout.services.order;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;

import com.asrevo.cvhome.checkout.entity.customer.Customer;
import com.asrevo.cvhome.checkout.entity.order.Order;
import com.asrevo.cvhome.checkout.entity.order.OrderSummary;
import com.asrevo.cvhome.checkout.entity.order.OrderTotalSummary;
import com.asrevo.cvhome.checkout.entity.order.orderstatus.OrderStatusHistory;
import com.asrevo.cvhome.checkout.entity.shoppingcart.ShoppingCartItem;
import com.asrevo.cvhome.checkout.model.order.OrderCriteria;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus; // Import OrderStatus
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;

public interface OrderService extends SalesManagerEntityService<Long, Order> {

    void addOrderStatusHistory(Order order, OrderStatusHistory history) throws ServiceException;

    OrderTotalSummary calculateOrderTotal(OrderSummary orderSummary, StoreMerchantId store) throws ServiceException;

    Order process(Order order, Customer customer, List<ShoppingCartItem> items, OrderTotalSummary summary, StoreMerchantId store)
            throws ServiceException;

    Order getOrder(Long orderId, StoreMerchantId store);

    Page<Order> getOrders(OrderCriteria criteria, StoreMerchantId store);

    List<Order> findPendingOnlinePaymentOrders();

    void updateOrderStatus(Long orderId, OrderStatus newStatus);

    Optional<Order> findOrderByShoppingCartCodeAndStoreMerchantId(String shoppingCartCode, StoreMerchantId storeMerchantId);
}