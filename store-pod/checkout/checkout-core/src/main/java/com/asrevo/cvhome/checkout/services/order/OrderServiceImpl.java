package com.asrevo.cvhome.checkout.services.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.checkout.entity.customer.Customer;
import com.asrevo.cvhome.checkout.entity.order.Order;
import com.asrevo.cvhome.checkout.entity.order.OrderSummary;
import com.asrevo.cvhome.checkout.entity.order.OrderTotal;
import com.asrevo.cvhome.checkout.entity.order.OrderTotalSummary;
import com.asrevo.cvhome.checkout.entity.order.OrderTotalType;
import com.asrevo.cvhome.checkout.entity.order.orderstatus.OrderStatusHistory;
import com.asrevo.cvhome.checkout.entity.shoppingcart.ShoppingCartItem;
import com.asrevo.cvhome.checkout.model.order.OrderCriteria;
import com.asrevo.cvhome.checkout.repositories.order.OrderRepository;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.common.InventoryStatus;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrderServiceImpl extends SalesManagerEntityServiceImpl<Long, Order> implements OrderService {

    private final OrderRepository orderRepository;

    public OrderServiceImpl(OrderRepository orderRepository) {
        super(orderRepository);
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public void addOrderStatusHistory(Order order, OrderStatusHistory history) throws ServiceException {
        order.setStatus(history.getStatus());
        order.getOrderHistory().add(history);
        history.setOrder(order);
        update(order);
    }

    @Override
    public OrderTotalSummary calculateOrderTotal(OrderSummary orderSummary, StoreMerchantId store) {
        return calculateOrder(orderSummary, store);
    }

    @Override
    @Transactional
    public void delete(Order order) throws ServiceException {

        super.delete(order);
    }

    @Override
    public Order getOrder(Long orderId, StoreMerchantId store) {
        return orderRepository.findOne(orderId, store);
    }

    @Override
    public Page<Order> getOrders(OrderCriteria criteria, StoreMerchantId store) {
        return orderRepository.listOrders(store, criteria, criteria.getPageable());
    }

    @Transactional
    @Override
    public Order process(Order order, Customer customer, List<ShoppingCartItem> items, OrderTotalSummary summary, StoreMerchantId store)
            throws ServiceException {

        if (order.getOrderHistory() == null || order.getOrderHistory().isEmpty() || order.getStatus() == null) {
            OrderStatus status = order.getStatus();
            if (status == null) {
                status = OrderStatus.CREATED;
                order.setStatus(status);
                order.setInventoryStatus(InventoryStatus.NOT_REQUESTED);
                order.setPaymentStatus(PaymentStatus.PENDING);
            }
            Set<OrderStatusHistory> statusHistorySet = new HashSet<>();
            OrderStatusHistory statusHistory = new OrderStatusHistory();
            statusHistory.setStatus(status);
            statusHistory.setDateAdded(Instant.now());
            statusHistory.setOrder(order);
            statusHistorySet.add(statusHistory);
            order.setOrderHistory(statusHistorySet);
        }

        order.setCustomerId(customer.getId());
        this.create(order);

        return order;
    }

    private OrderTotalSummary calculateOrder(OrderSummary summary, StoreMerchantId store) {

        OrderTotalSummary totalSummary = new OrderTotalSummary();
        List<OrderTotal> orderTotals = new ArrayList<>();

        BigDecimal grandTotal = new BigDecimal(0).setScale(2, RoundingMode.HALF_UP);
        BigDecimal subTotal = new BigDecimal(0).setScale(2, RoundingMode.HALF_UP);

        for (ShoppingCartItem item : summary.getProducts()) {

            BigDecimal st = item.getItemPrice().multiply(new BigDecimal(item.getQuantity()));
            item.setSubTotal(st);
            subTotal = subTotal.add(st);
        }

        totalSummary.setSubTotal(subTotal);
        grandTotal = grandTotal.add(subTotal);

        OrderTotal orderTotalSubTotal = new OrderTotal();
        orderTotalSubTotal.setModule(Constants.OT_SUBTOTAL_MODULE_CODE);
        orderTotalSubTotal.setOrderTotalType(OrderTotalType.SUBTOTAL);
        orderTotalSubTotal.setOrderTotalCode("order.total.subtotal");
        orderTotalSubTotal.setTitle(Constants.OT_SUBTOTAL_MODULE_CODE);
        orderTotalSubTotal.setSortOrder(5);
        orderTotalSubTotal.setValue(subTotal);

        orderTotals.add(orderTotalSubTotal);

        OrderTotal orderTotal = new OrderTotal();
        orderTotal.setModule(Constants.OT_TOTAL_MODULE_CODE);
        orderTotal.setOrderTotalType(OrderTotalType.TOTAL);
        orderTotal.setOrderTotalCode("order.total.total");
        orderTotal.setTitle(Constants.OT_TOTAL_MODULE_CODE);
        orderTotal.setSortOrder(500);
        orderTotal.setValue(grandTotal);
        orderTotals.add(orderTotal);

        totalSummary.setTotal(grandTotal);
        totalSummary.setTotals(orderTotals);
        return totalSummary;
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus orderStatus, InventoryStatus inventoryStatus, PaymentStatus paymentStatus) {
        updateOrderStatus(orderId, orderStatus, inventoryStatus, paymentStatus, null);
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus orderStatus, InventoryStatus inventoryStatus, PaymentStatus paymentStatus,
                                  String redirectUri) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order != null) {
            if (orderStatus != null && !orderStatus.equals(order.getStatus())) {
                order.setStatus(orderStatus);
                // Add to history
                OrderStatusHistory statusHistory = new OrderStatusHistory();
                statusHistory.setStatus(orderStatus);
                statusHistory.setDateAdded(Instant.now());
                statusHistory.setOrder(order);
                order.getOrderHistory().add(statusHistory);
            }
            if (inventoryStatus != null) {
                order.setInventoryStatus(inventoryStatus);
            }
            if (paymentStatus != null) {
                order.setPaymentStatus(paymentStatus);
            }
            if (redirectUri != null) {
                order.setRedirectUri(redirectUri);
            }
            orderRepository.save(order);
            log.info("Order {} status updated: status={}, inventory={}, payment={}", orderId, orderStatus, inventoryStatus, paymentStatus);
        } else {
            log.warn("Attempted to update status for non-existent order {}", orderId);
        }
    }

    @Override
    public Optional<Order> findOrderByShoppingCartCodeAndStoreMerchantId(String shoppingCartCode, StoreMerchantId storeMerchantId) {
        return orderRepository.findOrderByShoppingCartCodeAndStoreMerchantId(shoppingCartCode, storeMerchantId);
    }
}