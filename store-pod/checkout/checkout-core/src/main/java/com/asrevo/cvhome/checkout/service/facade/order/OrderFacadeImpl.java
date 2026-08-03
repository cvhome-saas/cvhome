package com.asrevo.cvhome.checkout.service.facade.order;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.catalog.api.errors.CatalogApiUnavailableException;
import com.asrevo.cvhome.checkout.entity.customer.Customer;
import com.asrevo.cvhome.checkout.entity.order.Order;
import com.asrevo.cvhome.checkout.entity.order.OrderSummary;
import com.asrevo.cvhome.checkout.entity.order.OrderTotal;
import com.asrevo.cvhome.checkout.entity.order.OrderTotalSummary;
import com.asrevo.cvhome.checkout.entity.order.orderproduct.OrderProduct;
import com.asrevo.cvhome.checkout.entity.order.orderstatus.OrderStatusHistory;
import com.asrevo.cvhome.checkout.entity.shoppingcart.ShoppingCart;
import com.asrevo.cvhome.checkout.entity.shoppingcart.ShoppingCartItem;
import com.asrevo.cvhome.checkout.errors.OrderNotConvertibleException;
import com.asrevo.cvhome.checkout.errors.OrderNotFoundException;
import com.asrevo.cvhome.checkout.errors.OrderProductNotConvertibleException;
import com.asrevo.cvhome.checkout.errors.OrderProductPriceMissingException;
import com.asrevo.cvhome.checkout.errors.PriceNotFormattableException;
import com.asrevo.cvhome.checkout.errors.ShoppingCartNotFoundException;
import com.asrevo.cvhome.checkout.model.order.OrderCriteria;
import com.asrevo.cvhome.checkout.model.order.ReadableOrderProduct;
import com.asrevo.cvhome.checkout.model.order.history.PersistableOrderStatusHistory;
import com.asrevo.cvhome.checkout.model.order.history.ReadableOrderStatusHistory;
import com.asrevo.cvhome.checkout.model.order.total.ReadableOrderTotal;
import com.asrevo.cvhome.checkout.model.order.total.ReadableTotal;
import com.asrevo.cvhome.checkout.model.order.v0.ReadableOrder;
import com.asrevo.cvhome.checkout.model.order.v0.ReadableOrderList;
import com.asrevo.cvhome.checkout.model.order.v1.PersistableOrder;
import com.asrevo.cvhome.checkout.model.order.v1.ReadableOrderConfirmation;
import com.asrevo.cvhome.checkout.model.order.v1.ReadableOrderStatus;
import com.asrevo.cvhome.checkout.service.facade.cart.ShoppingCartFacade;
import com.asrevo.cvhome.checkout.service.facade.customer.CustomerFacade;
import com.asrevo.cvhome.checkout.service.mapper.customer.ReadableCustomerMapper;
import com.asrevo.cvhome.checkout.service.mapper.order.ReadableOrderProductMapper;
import com.asrevo.cvhome.checkout.service.mapper.order.ReadableOrderTotalMapper;
import com.asrevo.cvhome.checkout.service.populator.order.OrderProductPopulator;
import com.asrevo.cvhome.checkout.service.populator.order.PersistableOrderApiPopulator;
import com.asrevo.cvhome.checkout.service.populator.order.ReadableOrderPopulator;
import com.asrevo.cvhome.checkout.service.populator.order.ReadableOrderProductPopulator;
import com.asrevo.cvhome.checkout.services.order.OrderService;
import com.asrevo.cvhome.checkout.services.shoppingcart.ShoppingCartService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.customer.model.customer.ReadableCustomer;
import com.asrevo.cvhome.store.core.entity.common.Billing;
import com.asrevo.cvhome.store.core.entity.common.Delivery;
import com.asrevo.cvhome.store.core.entity.common.InventoryStatus;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;
import com.asrevo.cvhome.store.core.exception.ServiceException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service("orderFacade")
@Slf4j
@RequiredArgsConstructor
public class OrderFacadeImpl implements OrderFacade {

    private static final String CUSTOMER_ID_NOT_FOUND_IN_ORDER_LOG_MESSAGE = "Customer id {} not found in order {}";

    private final ShoppingCartService shoppingCartService;

    private final ShoppingCartFacade shoppingCartFacade;

    private final OrderService orderService;

    private final PersistableOrderApiPopulator persistableOrderApiPopulator;

    private final CustomerFacade customerFacade;

    private final ReadableOrderTotalMapper readableOrderTotalMapper;

    private final ReadableCustomerMapper readableCustomerMapper;

    private final ReadableOrderProductMapper readableOrderProductMapper;

    private final ReadableOrderPopulator readableOrderPopulator;

    private final OrderProductPopulator orderProductPopulator;

    private final ReadableOrderProductPopulator readableOrderProductPopulator;
    private final OrderInventoryOrchestrator orderInventoryOrchestrator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order saveOrder(PersistableOrder order, Customer customer, StoreMerchantId store, LanguageCode language)
            throws ServiceException, ShoppingCartNotFoundException, OrderNotConvertibleException,
            OrderProductNotConvertibleException, OrderProductPriceMissingException {

        Long shoppingCartId = order.getShoppingCartId();

        ShoppingCart cart = shoppingCartService.findCart(shoppingCartId, store);

        if (cart == null) {
            throw ShoppingCartNotFoundException.byId(shoppingCartId);
        }

        Optional<Order> previousOrder =
                orderService.findOrderByShoppingCartCodeAndStoreMerchantId(cart.getShoppingCartCode(), store);

        if (previousOrder.isPresent()) {
            log.info("Returning existing order {} for shopping cart code {}", previousOrder.get().getId(), cart.getShoppingCartCode());
            return previousOrder.get();
        }

        Order modelOrder = new Order();

        persistableOrderApiPopulator.populate(order, modelOrder, store, language);

        modelOrder.setCustomerEmailAddress(customer.getEmailAddress());

        Delivery delivery = customer.getDelivery();
        modelOrder.setDelivery(delivery);

        Billing billing = customer.getBilling();
        modelOrder.setBilling(billing);

        List<ShoppingCartItem> shoppingCartItems = new ArrayList<>(cart.getLineItems());

        Set<OrderProduct> orderProducts = new LinkedHashSet<>();

        for (ShoppingCartItem item : shoppingCartItems) {
            OrderProduct orderProduct = new OrderProduct();
            orderProduct = orderProductPopulator.populate(item, orderProduct, store, language);
            orderProduct.setOrder(modelOrder);
            orderProducts.add(orderProduct);
        }

        modelOrder.setOrderProducts(orderProducts);

        OrderSummary orderSummary = new OrderSummary();

        orderSummary.setProducts(shoppingCartItems);

        OrderTotalSummary orderTotalSummary = orderService.calculateOrderTotal(orderSummary, store);

        modelOrder.setTotal(orderTotalSummary.getTotal());
        List<OrderTotal> totals = orderTotalSummary.getTotals();
        Set<OrderTotal> set = new HashSet<>();

        if (!CollectionUtils.isEmpty(totals)) {
            for (OrderTotal total : totals) {
                total.setOrder(modelOrder);
                set.add(total);
            }
        }
        modelOrder.setOrderTotal(set);

        modelOrder.setShoppingCartCode(cart.getShoppingCartCode());
        modelOrder = orderService.process(modelOrder, customer, shoppingCartItems, orderTotalSummary, store);

        modelOrder.setStatus(OrderStatus.CREATED);
        modelOrder.setInventoryStatus(InventoryStatus.NOT_REQUESTED);
        modelOrder.setPaymentStatus(PaymentStatus.PENDING);
        orderService.save(modelOrder);
        cart.setOrderId(modelOrder.getId());
        shoppingCartFacade.saveOrUpdateShoppingCart(cart);

        return modelOrder;
    }

    @Override
    public ReadableOrderConfirmation orderConfirmation(Order order, Customer customer, StoreMerchantId store,
                                                       LanguageCode language) {

        ReadableOrderConfirmation orderConfirmation = new ReadableOrderConfirmation();

        ReadableCustomer readableCustomer = readableCustomerMapper.convert(customer, store, language);
        orderConfirmation.setBilling(readableCustomer.getBilling());
        orderConfirmation.setDelivery(readableCustomer.getDelivery());

        ReadableTotal readableTotal = new ReadableTotal();

        Set<OrderTotal> totals = order.getOrderTotal();
        List<ReadableOrderTotal> readableTotals = totals.stream()
                .sorted(Comparator.comparingInt(OrderTotal::getSortOrder))
                .map(tot -> readableOrderTotalMapper.convert(tot, store, language))
                .toList();

        readableTotal.setTotals(readableTotals);

        Optional<ReadableOrderTotal> grandTotal = readableTotals.stream()
                .filter(tot -> tot.getCode().equals("order.total.total"))
                .findFirst();

        grandTotal.ifPresent(readableOrderTotal -> readableTotal.setGrandTotal(readableOrderTotal.getText()));
        orderConfirmation.setTotal(readableTotal);

        List<ReadableOrderProduct> products = order.getOrderProducts()
                .stream()
                .map(pr -> readableOrderProductMapper.convert(pr, store, language))
                .toList();
        orderConfirmation.setProducts(products);

        orderConfirmation.setId(order.getId());
        orderConfirmation.setOrderStatus(order.getStatus());
        orderConfirmation.setPaymentStatus(order.getPaymentStatus());

        return orderConfirmation;
    }

    @Override
    public ReadableOrderList getReadableOrderList(OrderCriteria criteria, StoreMerchantId store) {
        Page<Order> ordersList = orderService.getOrders(criteria, store);

        List<Order> orders = ordersList.getContent();
        ReadableOrderList returnList = new ReadableOrderList();

        List<ReadableOrder> readableOrders = new ArrayList<>();
        for (Order order : orders) {
            ReadableOrder readableOrder = new ReadableOrder();
            readableOrderPopulator.populate(order, readableOrder, null, null);
            readableOrders.add(readableOrder);
        }
        returnList.setContent(readableOrders);

        returnList.setTotalElements(ordersList.getTotalElements());
        returnList.setTotalPages(ordersList.getTotalPages());
        returnList.setSize(ordersList.getSize());
        returnList.setRecordsFiltered(ordersList.getSize());
        returnList.setPageNumber(ordersList.getNumber());

        return returnList;
    }

    @Override
    public ReadableOrderStatus getOrderStatus(Long orderId, StoreMerchantId store) throws OrderNotFoundException {
        Order modelOrder = orderService.getOrder(orderId, store);
        if (modelOrder == null) {
            throw OrderNotFoundException.of(orderId, store);
        }

        ReadableOrderStatus readableOrderStatus = new ReadableOrderStatus();
        readableOrderStatus.setOrderId(modelOrder.getId());
        readableOrderStatus.setOrderStatus(modelOrder.getStatus());
        readableOrderStatus.setPaymentStatus(modelOrder.getPaymentStatus());
        readableOrderStatus.setRedirectUrl(modelOrder.getRedirectUri());

        return readableOrderStatus;
    }

    @Override
    public ReadableOrder getReadableOrder(Long orderId, StoreMerchantId store, LanguageCode language)
            throws OrderNotFoundException, PriceNotFormattableException {
        Order modelOrder = orderService.getOrder(orderId, store);
        if (modelOrder == null) {
            throw OrderNotFoundException.of(orderId, store);
        }

        ReadableOrder readableOrder = new ReadableOrder();

        Long customerId = modelOrder.getCustomerId();
        if (customerId != null) {
            ReadableCustomer readableCustomer = customerFacade.getCustomerById(customerId, store, language);
            if (readableCustomer == null) {
                log.warn(CUSTOMER_ID_NOT_FOUND_IN_ORDER_LOG_MESSAGE, customerId, orderId);
            } else {
                readableOrder.setCustomer(readableCustomer);
            }
        }

        readableOrderPopulator.populate(modelOrder, readableOrder, store, language);

        // order products
        List<ReadableOrderProduct> orderProducts = new ArrayList<>();
        for (OrderProduct p : modelOrder.getOrderProducts()) {

            ReadableOrderProduct orderProduct = new ReadableOrderProduct();
            readableOrderProductPopulator.populate(p, orderProduct, store, language);
            orderProducts.add(orderProduct);
        }

        readableOrder.setProducts(orderProducts);

        return readableOrder;
    }

    @Override
    public ReadableOrder getReadableOrder(Long orderId, Long customerId, StoreMerchantId store, LanguageCode language)
            throws OrderNotFoundException, PriceNotFormattableException {
        Order modelOrder = orderService.getOrder(orderId, store);
        if (modelOrder == null) {
            throw OrderNotFoundException.of(orderId, store);
        }
        if (modelOrder.getCustomerId() == null || !modelOrder.getCustomerId().equals(customerId)) {
            throw OrderNotFoundException.forCustomer(orderId, customerId);
        }

        ReadableOrder readableOrder = new ReadableOrder();

        ReadableCustomer readableCustomer = customerFacade.getCustomerById(customerId, store, language);
        if (readableCustomer == null) {
            log.warn(CUSTOMER_ID_NOT_FOUND_IN_ORDER_LOG_MESSAGE, customerId, orderId);
        } else {
            readableOrder.setCustomer(readableCustomer);
        }

        readableOrderPopulator.populate(modelOrder, readableOrder, store, language);

        // order products
        List<ReadableOrderProduct> orderProducts = new ArrayList<>();
        for (OrderProduct p : modelOrder.getOrderProducts()) {

            ReadableOrderProduct orderProduct = new ReadableOrderProduct();
            readableOrderProductPopulator.populate(p, orderProduct, store, language);
            orderProducts.add(orderProduct);
        }

        readableOrder.setProducts(orderProducts);

        return readableOrder;
    }

    @Override
    public List<ReadableOrderStatusHistory> getReadableOrderHistory(Long orderId, StoreMerchantId store,
                                                                    LanguageCode language) throws OrderNotFoundException {

        Order order = orderService.getOrder(orderId, store);
        if (order == null) {
            throw OrderNotFoundException.of(orderId, store);
        }

        Set<OrderStatusHistory> historyList = order.getOrderHistory();
        return historyList.stream().map(this::mapToReadableOrderStatusHistory).toList();
    }

    @Override
    public List<ReadableOrderStatusHistory> getReadableOrderHistory(Long orderId, Long customerId,
                                                                    StoreMerchantId store, LanguageCode language)
            throws OrderNotFoundException {

        Order order = orderService.getOrder(orderId, store);
        if (order == null) {
            throw OrderNotFoundException.of(orderId, store);
        }

        if (order.getCustomerId() == null || !order.getCustomerId().equals(customerId)) {
            throw OrderNotFoundException.forCustomer(orderId, customerId);
        }

        Set<OrderStatusHistory> historyList = order.getOrderHistory();
        return historyList.stream().map(this::mapToReadableOrderStatusHistory).toList();
    }

    ReadableOrderStatusHistory mapToReadableOrderStatusHistory(OrderStatusHistory source) {
        ReadableOrderStatusHistory readable = new ReadableOrderStatusHistory();
        readable.setComments(source.getComments());
        readable.setDate(source.getDateAdded());
        readable.setId(source.getId());
        readable.setOrderId(source.getOrder().getId());
        readable.setOrderStatus(source.getStatus());

        return readable;
    }

    @Override
    public void createOrderStatus(PersistableOrderStatusHistory status, Long id, StoreMerchantId store)
            throws OrderNotFoundException, ServiceException, CatalogApiUnavailableException {
        Order order = orderService.getOrder(id, store);
        if (order == null) {
            throw OrderNotFoundException.of(id, store);
        }

        OrderStatusHistory history = new OrderStatusHistory();
        history.setComments(status.getComments());
        history.setDateAdded(status.getDate());
        history.setOrder(order);
        history.setStatus(status.getOrderStatus());

        orderService.addOrderStatusHistory(order, history);

        if ((status.getOrderStatus() == OrderStatus.DELIVERED || status.getOrderStatus() == OrderStatus.COMPLETED)
                && order.getInventoryStatus() == InventoryStatus.RESERVED) {
            log.info("Order {} reached {} status. Committing inventory for store {}", id, status.getOrderStatus(), store);
            orderInventoryOrchestrator.updateOrderStatusWithReservationCommit(id, store, status.getOrderStatus(), PaymentStatus.PAID);
        } else if (status.getOrderStatus() == OrderStatus.CANCELLED && order.getInventoryStatus() == InventoryStatus.RESERVED) {
            log.info("Order {} cancelled. Releasing inventory for store {}", id, store);
            orderInventoryOrchestrator.updateOrderStatusWithReservationRelease(id, store, status.getOrderStatus(),
                    PaymentStatus.CANCELLED);
        }
    }

    @Override
    public void updateOrderStatus(Long orderId, OrderStatus orderStatus, InventoryStatus inventoryStatus, PaymentStatus paymentStatus) {
        orderService.updateOrderStatus(orderId, orderStatus, inventoryStatus, paymentStatus);
    }

    @Override
    public void updateOrderStatus(Long orderId, OrderStatus orderStatus, InventoryStatus inventoryStatus, PaymentStatus paymentStatus,
                                  String redirectUri) {
        orderService.updateOrderStatus(orderId, orderStatus, inventoryStatus, paymentStatus, redirectUri);
    }
}