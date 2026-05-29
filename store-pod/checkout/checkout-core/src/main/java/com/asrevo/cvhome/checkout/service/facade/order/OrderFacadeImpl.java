package com.asrevo.cvhome.checkout.service.facade.order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.catalog.model.product.ProductReservationStatus;
import com.asrevo.cvhome.catalog.services.product.ExternalProductReservationService;
import com.asrevo.cvhome.checkout.entity.customer.Customer;
import com.asrevo.cvhome.checkout.entity.order.Order;
import com.asrevo.cvhome.checkout.entity.order.OrderSummary;
import com.asrevo.cvhome.checkout.entity.order.OrderTotal;
import com.asrevo.cvhome.checkout.entity.order.OrderTotalSummary;
import com.asrevo.cvhome.checkout.entity.order.orderproduct.OrderProduct;
import com.asrevo.cvhome.checkout.entity.order.orderstatus.OrderStatusHistory;
import com.asrevo.cvhome.checkout.entity.shoppingcart.ShoppingCart;
import com.asrevo.cvhome.checkout.entity.shoppingcart.ShoppingCartItem;
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
import com.asrevo.cvhome.checkout.model.payments.Payment;
import com.asrevo.cvhome.checkout.service.facade.cart.ShoppingCartFacade;
import com.asrevo.cvhome.checkout.service.facade.customer.CustomerFacade;
import com.asrevo.cvhome.checkout.service.mapper.customer.ReadableCustomerMapper;
import com.asrevo.cvhome.checkout.service.mapper.order.ReadableOrderProductMapper;
import com.asrevo.cvhome.checkout.service.mapper.order.ReadableOrderTotalMapper;
import com.asrevo.cvhome.checkout.service.populator.order.OrderProductPopulator;
import com.asrevo.cvhome.checkout.service.populator.order.PersistableOrderApiPopulator;
import com.asrevo.cvhome.checkout.service.populator.order.ReadableOrderPopulator;
import com.asrevo.cvhome.checkout.service.populator.order.ReadableOrderProductPopulator;
import com.asrevo.cvhome.checkout.service.populator.order.transaction.PersistablePaymentPopulator;
import com.asrevo.cvhome.checkout.services.order.OrderService;
import com.asrevo.cvhome.checkout.services.shoppingcart.ShoppingCartCalculationService;
import com.asrevo.cvhome.checkout.services.shoppingcart.ShoppingCartService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.customer.model.customer.ReadableCustomer;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.entity.common.Billing;
import com.asrevo.cvhome.store.core.entity.common.Delivery;
import com.asrevo.cvhome.store.core.exception.ConversionException;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.catalog.ProductReservationList;
import com.asrevo.cvhome.store.core.model.catalog.ReserveProductEntry;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.utils.PriceUtils;

import lombok.extern.slf4j.Slf4j;

@Service("orderFacade")
@Slf4j
public class OrderFacadeImpl implements OrderFacade {

    private static final String ORDER_NOT_FOUND_BY_ID_MESSAGE = "Order not found with id {}";
    private static final String CUSTOMER_ID_NOT_FOUND_IN_ORDER_LOG_MESSAGE = "Customer id {} not found in order {}";
    private static final String ERROR_WHILE_GETTING_ORDER_MESSAGE = "Error while getting order [{}]";
    private static final String ORDER_NOT_FOUND_FOR_MERCHANT_MESSAGE = "Order id [{}] not found for merchand [{}]";
    private static final String ORDER_DOES_NOT_EXIST_FOR_MERCHANT_MESSAGE = "Order with id [{}] does not exist for merchant [{}]";

    private final ShoppingCartService shoppingCartService;

    private final ShoppingCartFacade shoppingCartFacade;

    private final OrderService orderService;

    private final ExternalProductReservationService externalProductReservationService;

    private final PersistableOrderApiPopulator persistableOrderApiPopulator;

    private final CustomerFacade customerFacade;

    private final ReadableOrderTotalMapper readableOrderTotalMapper;

    private final ReadableCustomerMapper readableCustomerMapper;

    private final ReadableOrderProductMapper readableOrderProductMapper;

    private final ReadableOrderPopulator readableOrderPopulator;

    private final ShoppingCartCalculationService shoppingCartCalculationService;

    private final PersistablePaymentPopulator paymentPopulator;

    private final OrderProductPopulator orderProductPopulator;
    private final ReadableOrderProductPopulator readableOrderProductPopulator;

    public OrderFacadeImpl(ShoppingCartFacade shoppingCartFacade, ShoppingCartService shoppingCartService,
                           OrderService orderService,
                           ExternalProductReservationService externalProductReservationService,
                           PersistableOrderApiPopulator persistableOrderApiPopulator,
                           ReadableOrderProductMapper readableOrderProductMapper, CustomerFacade customerFacade,
                           ReadableCustomerMapper readableCustomerMapper, ReadableOrderTotalMapper readableOrderTotalMapper,
                           ReadableOrderPopulator readableOrderPopulator,
                           ShoppingCartCalculationService shoppingCartCalculationService,
                           PersistablePaymentPopulator paymentPopulator,
                           OrderProductPopulator orderProductPopulator, ReadableOrderProductPopulator readableOrderProductPopulator) {
        this.shoppingCartFacade = shoppingCartFacade;
        this.shoppingCartService = shoppingCartService;
        this.orderService = orderService;
        this.externalProductReservationService = externalProductReservationService;
        this.persistableOrderApiPopulator = persistableOrderApiPopulator;
        this.readableOrderProductMapper = readableOrderProductMapper;
        this.customerFacade = customerFacade;
        this.readableCustomerMapper = readableCustomerMapper;
        this.readableOrderTotalMapper = readableOrderTotalMapper;
        this.readableOrderPopulator = readableOrderPopulator;
        this.shoppingCartCalculationService = shoppingCartCalculationService;
        this.paymentPopulator = paymentPopulator;
        this.orderProductPopulator = orderProductPopulator;
        this.readableOrderProductPopulator = readableOrderProductPopulator;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order processOrder(PersistableOrder order, Customer customer, StoreMerchantId store, LanguageCode language,
                              Locale locale) throws ServiceException {

        Order modelOrder = null;
        try {

            modelOrder = new Order();
            persistableOrderApiPopulator.populate(order, modelOrder, store, language);

            modelOrder.setCustomerEmailAddress(customer.getEmailAddress());

            Delivery delivery = customer.getDelivery();
            modelOrder.setDelivery(delivery);

            Billing billing = customer.getBilling();
            modelOrder.setBilling(billing);

            Long shoppingCartId = order.getShoppingCartId();
            ShoppingCart cart = shoppingCartService.findCart(shoppingCartId, store);

            if (cart == null) {
                throw new ServiceException("Shopping cart with id " + shoppingCartId + " does not exist");
            }
            OrderTotalSummary calculate = shoppingCartCalculationService.calculate(cart, customer, store, language);
            order.getPayment().setAmount(calculate.getTotal().toString());
            Set<ShoppingCartItem> shoppingCartItems = cart.getLineItems();

            List<ShoppingCartItem> items = new ArrayList<>(shoppingCartItems);

            Set<OrderProduct> orderProducts = new LinkedHashSet<>();

            for (ShoppingCartItem item : shoppingCartItems) {
                OrderProduct orderProduct = new OrderProduct();
                orderProduct = orderProductPopulator.populate(item, orderProduct, store, language);
                orderProduct.setOrder(modelOrder);
                orderProducts.add(orderProduct);
            }

            modelOrder.setOrderProducts(orderProducts);

            OrderSummary orderSummary = new OrderSummary();
            List<ShoppingCartItem> itemsSet = new ArrayList<>(cart.getLineItems());
            orderSummary.setProducts(itemsSet);

            OrderTotalSummary orderTotalSummary = orderService.caculateOrderTotal(orderSummary, customer, store,
                    language);

            if (order.getPayment().getAmount() == null) {
                throw new ConversionException("Requires Payment.amount");
            }

            String submitedAmount = order.getPayment().getAmount();

            BigDecimal formattedSubmittedAmount = PriceUtils.getAmount(submitedAmount);

            BigDecimal calculatedAmount = orderTotalSummary.getTotal();
            String strCalculatedTotal = calculatedAmount.toPlainString();

            // compare both prices
            if (calculatedAmount.compareTo(formattedSubmittedAmount) != 0) {

                throw new ConversionException(
                        "Payment.amount does not match what the system has calculated " + strCalculatedTotal
                                + " (received " + submitedAmount + ") please recalculate the order and submit again");
            }

            modelOrder.setTotal(calculatedAmount);
            List<OrderTotal> totals = orderTotalSummary.getTotals();
            Set<OrderTotal> set = new HashSet<>();

            if (!CollectionUtils.isEmpty(totals)) {
                for (OrderTotal total : totals) {
                    total.setOrder(modelOrder);
                    set.add(total);
                }
            }
            modelOrder.setOrderTotal(set);

            Payment paymentModel = new Payment();
            paymentPopulator.populate(order.getPayment(), paymentModel, store, language);

            modelOrder.setShoppingCartCode(cart.getShoppingCartCode());

            // order service
            modelOrder = orderService.process(modelOrder, customer, items, orderTotalSummary, paymentModel, null, store);

            // Reserve inventory
            log.debug("Update inventory");
            ProductReservationList productReservation = modelOrder.getOrderProducts()
                    .stream()
                    .map(it -> new ReserveProductEntry(it.getSku(), it.getProductQuantity()))
                    .collect(Collectors.collectingAndThen(Collectors.toSet(), ProductReservationList::new));

            ProductReservationStatus reservationStatus = externalProductReservationService.reserve(store,
                    modelOrder.getId(), productReservation);
            if (!reservationStatus.status()) {
                throw new ServiceException("error updating inventory with new qty");
            }

            // update cart
            cart.setOrderId(modelOrder.getId());
            shoppingCartFacade.saveOrUpdateShoppingCart(cart);

            log.debug("Commit inventory reservation for order {}", modelOrder.getId());
            externalProductReservationService.commit(store, modelOrder.getId());

            log.debug("Commit inventory reservation for order {}", modelOrder.getId());
            externalProductReservationService.commit(store, modelOrder.getId());

            return modelOrder;

        } catch (Exception e) {
            if (modelOrder != null && modelOrder.getId() != null) {
                unreserve(store, modelOrder.getId());
            }
            throw new ServiceException(e);
        }
    }

    private void unreserve(StoreMerchantId store, Long orderId) {
        try {
            externalProductReservationService.unreserve(store, orderId);
        } catch (Exception e) {
            log.error("Error while unreserving for order {}", orderId, e);
        }
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

        if (order.getPaymentType() != null) {
            orderConfirmation.setPayment(order.getPaymentType().name());
        }

        orderConfirmation.setId(order.getId());

        return orderConfirmation;
    }

    @Override
    public ReadableOrderList getReadableOrderList(OrderCriteria criteria, StoreMerchantId store) {
        try {
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

        } catch (Exception e) {
            throw new ServiceRuntimeException("Error while getting orders", e);
        }
    }

    @Override
    public ReadableOrder getReadableOrder(Long orderId, StoreMerchantId store, LanguageCode language) {
        Order modelOrder = orderService.getOrder(orderId, store);
        if (modelOrder == null) {
            throw new ResourceNotFoundException(ORDER_NOT_FOUND_BY_ID_MESSAGE.replace("{}", orderId.toString()));
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

        try {
            readableOrderPopulator.populate(modelOrder, readableOrder, store, language);

            // order products
            List<ReadableOrderProduct> orderProducts = new ArrayList<>();
            for (OrderProduct p : modelOrder.getOrderProducts()) {

                ReadableOrderProduct orderProduct = new ReadableOrderProduct();
                readableOrderProductPopulator.populate(p, orderProduct, store, language);
                orderProducts.add(orderProduct);
            }

            readableOrder.setProducts(orderProducts);
        } catch (Exception _) {
            throw new ServiceRuntimeException(ERROR_WHILE_GETTING_ORDER_MESSAGE.replace("{}", orderId.toString()));
        }

        return readableOrder;
    }

    @Override
    public ReadableOrder getReadableOrder(Long orderId, Long customerId, StoreMerchantId store, LanguageCode language) {
        Order modelOrder = orderService.getOrder(orderId, store);
        if (modelOrder == null) {
            throw new ResourceNotFoundException(ORDER_NOT_FOUND_BY_ID_MESSAGE.replace("{}", orderId.toString()));
        }
        if (modelOrder.getCustomerId() == null || !modelOrder.getCustomerId().equals(customerId)) {
            throw new ResourceNotFoundException(
                    ORDER_NOT_FOUND_BY_ID_MESSAGE.replace("{}", orderId.toString()) + " for customer " + customerId);
        }

        ReadableOrder readableOrder = new ReadableOrder();

        ReadableCustomer readableCustomer = customerFacade.getCustomerById(customerId, store, language);
        if (readableCustomer == null) {
            log.warn(CUSTOMER_ID_NOT_FOUND_IN_ORDER_LOG_MESSAGE, customerId, orderId);
        } else {
            readableOrder.setCustomer(readableCustomer);
        }

        try {
            readableOrderPopulator.populate(modelOrder, readableOrder, store, language);

            // order products
            List<ReadableOrderProduct> orderProducts = new ArrayList<>();
            for (OrderProduct p : modelOrder.getOrderProducts()) {

                ReadableOrderProduct orderProduct = new ReadableOrderProduct();
                readableOrderProductPopulator.populate(p, orderProduct, store, language);
                orderProducts.add(orderProduct);
            }

            readableOrder.setProducts(orderProducts);
        } catch (Exception _) {
            throw new ServiceRuntimeException(ERROR_WHILE_GETTING_ORDER_MESSAGE.replace("{}", orderId.toString()));
        }

        return readableOrder;
    }

    @Override
    public List<ReadableOrderStatusHistory> getReadableOrderHistory(Long orderId, StoreMerchantId store,
                                                                    LanguageCode language) {

        Order order = orderService.getOrder(orderId, store);
        if (order == null) {
            throw new ResourceNotFoundException(
                    ORDER_NOT_FOUND_FOR_MERCHANT_MESSAGE.replace("{}", orderId.toString()).replace("{}", store.toString()));
        }

        Set<OrderStatusHistory> historyList = order.getOrderHistory();
        return historyList.stream().map(this::mapToReadbleOrderStatusHistory).toList();
    }

    @Override
    public List<ReadableOrderStatusHistory> getReadableOrderHistory(Long orderId, Long customerId,
                                                                    StoreMerchantId store, LanguageCode language) {

        Order order = orderService.getOrder(orderId, store);
        if (order == null) {
            throw new ResourceNotFoundException(
                    ORDER_NOT_FOUND_FOR_MERCHANT_MESSAGE.replace("{}", orderId.toString()).replace("{}", store.toString()));
        }

        if (order.getCustomerId() == null || !order.getCustomerId().equals(customerId)) {
            throw new ResourceNotFoundException(
                    ORDER_NOT_FOUND_BY_ID_MESSAGE.replace("{}", orderId.toString()) + " for customer " + customerId);
        }

        Set<OrderStatusHistory> historyList = order.getOrderHistory();
        return historyList.stream().map(this::mapToReadbleOrderStatusHistory).toList();
    }

    ReadableOrderStatusHistory mapToReadbleOrderStatusHistory(OrderStatusHistory source) {
        ReadableOrderStatusHistory readable = new ReadableOrderStatusHistory();
        readable.setComments(source.getComments());
        readable.setDate(source.getDateAdded());
        readable.setId(source.getId());
        readable.setOrderId(source.getOrder().getId());
        readable.setOrderStatus(source.getStatus());

        return readable;
    }

    @Override
    public void createOrderStatus(PersistableOrderStatusHistory status, Long id, StoreMerchantId store) {
        Order order = orderService.getOrder(id, store);
        if (order == null) {
            throw new ResourceNotFoundException(
                    ORDER_DOES_NOT_EXIST_FOR_MERCHANT_MESSAGE.replace("{}", id.toString()).replace("{}", store.toString()));
        }

        try {
            OrderStatusHistory history = new OrderStatusHistory();
            history.setComments(status.getComments());
            history.setDateAdded(status.getDate());
            history.setOrder(order);
            history.setStatus(status.getOrderStatus());

            orderService.addOrderStatusHistory(order, history);

        } catch (Exception e) {
            throw new ServiceRuntimeException("An error occured while converting orderstatushistory", e);
        }
    }

}
