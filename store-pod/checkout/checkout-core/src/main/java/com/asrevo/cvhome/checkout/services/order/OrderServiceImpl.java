package com.asrevo.cvhome.checkout.services.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
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
import com.asrevo.cvhome.checkout.entity.shoppingcart.ShoppingCart;
import com.asrevo.cvhome.checkout.entity.shoppingcart.ShoppingCartItem;
import com.asrevo.cvhome.checkout.model.order.OrderCriteria;
import com.asrevo.cvhome.checkout.model.order.OrderSummaryType;
import com.asrevo.cvhome.checkout.repositories.order.OrderRepository;
import com.asrevo.cvhome.checkout.services.shoppingcart.ShoppingCartService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrderServiceImpl extends SalesManagerEntityServiceImpl<Long, Order> implements OrderService {

    private static final String ERROR_CALCULATING_SHOPPING_CART_TOTAL = "Error while calculating shopping cart total";

    private final ShoppingCartService shoppingCartService;

    private final OrderRepository orderRepository;

    public OrderServiceImpl(OrderRepository orderRepository, ShoppingCartService shoppingCartService) {
        super(orderRepository);
        this.shoppingCartService = shoppingCartService;
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
    public OrderTotalSummary caculateOrderTotal(OrderSummary orderSummary, Customer customer, StoreMerchantId store,
                                                LanguageCode language) throws ServiceException {
        try {
            return calculateOrder(orderSummary, customer, store, language);
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public OrderTotalSummary calculateShoppingCartTotal(final ShoppingCart shoppingCart, final Customer customer,
                                                        final StoreMerchantId store, final LanguageCode language) throws ServiceException {
        try {
            return caculateshoppingcart(shoppingCart, customer, store, language);
        } catch (Exception e) {
            log.error(ERROR_CALCULATING_SHOPPING_CART_TOTAL, e);
            throw new ServiceException(e);
        }
    }

    private OrderTotalSummary caculateshoppingcart(ShoppingCart shoppingCart, final Customer customer,
                                                   final StoreMerchantId store, final LanguageCode language) throws Exception {

        OrderSummary orderSummary = new OrderSummary();
        orderSummary.setOrderSummaryType(OrderSummaryType.SHOPPINGCART);

        if (!StringUtils.isBlank(shoppingCart.getPromoCode())) {
            Instant promoDateAdded = shoppingCart.getPromoAdded();
            if (promoDateAdded == null) {
                promoDateAdded = Instant.now();
            }
            ZonedDateTime zdt = promoDateAdded.atZone(ZoneId.systemDefault());
            LocalDate date = zdt.toLocalDate();
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            if (date.isBefore(tomorrow)) {
                orderSummary.setPromoCode(shoppingCart.getPromoCode());
            } else {
                shoppingCart.setPromoCode(null);
                shoppingCartService.saveOrUpdate(shoppingCart);
            }
        }

        List<ShoppingCartItem> itemList = new ArrayList<>(shoppingCart.getLineItems());
        orderSummary.setProducts(itemList);

        return calculateOrder(orderSummary, customer, store, language);
    }

    @Override
    public OrderTotalSummary calculateShoppingCartTotal(final ShoppingCart shoppingCart, final StoreMerchantId store,
                                                        final LanguageCode language) throws ServiceException {
        try {
            return caculateshoppingcart(shoppingCart, null, store, language);
        } catch (Exception e) {
            log.error(ERROR_CALCULATING_SHOPPING_CART_TOTAL, e);
            throw new ServiceException(e);
        }
    }

    @Override
    @Transactional
    public void delete(final Order order) throws ServiceException {

        super.delete(order);
    }

    @Override
    public Order getOrder(final Long orderId, StoreMerchantId store) {
        return orderRepository.findOne(orderId, store);
    }

    @Override
    public Page<Order> getOrders(OrderCriteria criteria, StoreMerchantId store) {
        return orderRepository.listOrders(store, criteria, criteria.getPageable());
    }

    @Transactional
    @Override
    public Order process(Order order, Customer customer, List<ShoppingCartItem> items, OrderTotalSummary summary, StoreMerchantId store) throws ServiceException {

        if (order.getOrderHistory() == null || order.getOrderHistory().isEmpty() || order.getStatus() == null) {
            OrderStatus status = order.getStatus();
            if (status == null) {
                status = OrderStatus.ORDERED;
                order.setStatus(status);
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

    @SneakyThrows
    private OrderTotalSummary calculateOrder(OrderSummary summary, Customer customer, final StoreMerchantId store,
                                             final LanguageCode language) {

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

}
