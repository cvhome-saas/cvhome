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
import org.apache.commons.lang3.Validate;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.asrevo.cvhome.catalog.model.product.ProductReservationStatus;
import com.asrevo.cvhome.catalog.services.product.ExternalProductReservationService;
import com.asrevo.cvhome.catalog.services.product.ExternalProductService;
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
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.entity.common.Billing;
import com.asrevo.cvhome.store.core.entity.common.Delivery;
import com.asrevo.cvhome.store.core.exception.ConversionException;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.catalog.ProductReservationList;
import com.asrevo.cvhome.store.core.model.catalog.ReserveProductEntry;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.utils.DateUtil;
import com.asrevo.cvhome.store.utils.ImageFilePath;
import com.asrevo.cvhome.store.utils.PriceUtils;

import lombok.extern.slf4j.Slf4j;

@Service("orderFacade")
@Slf4j
public class OrderFacadeImpl implements OrderFacade {

    private final ShoppingCartService shoppingCartService;

    private final ShoppingCartFacade shoppingCartFacade;

    private final OrderService orderService;

    private final ExternalProductService externalProductService;

    private final ExternalProductReservationService externalProductReservationService;

    private final PersistableOrderApiPopulator persistableOrderApiPopulator;

    private final CustomerFacade customerFacade;

    private final ReadableOrderTotalMapper readableOrderTotalMapper;

    private final ReadableCustomerMapper readableCustomerMapper;

    private final ReadableOrderProductMapper readableOrderProductMapper;

    private final ReadableOrderPopulator readableOrderPopulator;

    private final ImageFilePath imageUtils;

    private final ShoppingCartCalculationService shoppingCartCalculationService;

    private final ExternalMerchantStoreService externalMerchantStoreService;

    private final PersistablePaymentPopulator paymentPopulator;

    private final OrderProductPopulator orderProductPopulator;

    public OrderFacadeImpl(ShoppingCartFacade shoppingCartFacade, ShoppingCartService shoppingCartService,
                           OrderService orderService, ExternalProductService externalProductService,
                           ExternalProductReservationService externalProductReservationService,
                           PersistableOrderApiPopulator persistableOrderApiPopulator,
                           ReadableOrderProductMapper readableOrderProductMapper, CustomerFacade customerFacade,
                           ReadableCustomerMapper readableCustomerMapper, ReadableOrderTotalMapper readableOrderTotalMapper,
                           ReadableOrderPopulator readableOrderPopulator, ImageFilePath imageUtils,
                           ShoppingCartCalculationService shoppingCartCalculationService,
                           ExternalMerchantStoreService externalMerchantStoreService, PersistablePaymentPopulator paymentPopulator,
                           OrderProductPopulator orderProductPopulator) {
        this.shoppingCartFacade = shoppingCartFacade;
        this.shoppingCartService = shoppingCartService;
        this.orderService = orderService;
        this.externalProductService = externalProductService;
        this.externalProductReservationService = externalProductReservationService;
        this.persistableOrderApiPopulator = persistableOrderApiPopulator;
        this.readableOrderProductMapper = readableOrderProductMapper;
        this.customerFacade = customerFacade;
        this.readableCustomerMapper = readableCustomerMapper;
        this.readableOrderTotalMapper = readableOrderTotalMapper;
        this.readableOrderPopulator = readableOrderPopulator;
        this.imageUtils = imageUtils;
        this.shoppingCartCalculationService = shoppingCartCalculationService;
        this.externalMerchantStoreService = externalMerchantStoreService;
        this.paymentPopulator = paymentPopulator;
        this.orderProductPopulator = orderProductPopulator;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order processOrder(PersistableOrder order, Customer customer, StoreMerchantId store, LanguageCode language,
                              Locale locale) throws ServiceException {

        Assert.notNull(order, "Order cannot be null");
        Assert.notNull(customer, "Customer cannot be null");
        Assert.notNull(store, "store cannot be null");
        Assert.notNull(language, "Language cannot be null");
        Assert.notNull(locale, "Locale cannot be null");

        try {

            Order modelOrder = new Order();
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
            modelOrder = orderService.processOrder(modelOrder, customer, items, orderTotalSummary, paymentModel, store);

            // Reserve inventory
            log.debug("Update inventory");
            ProductReservationList productReservation = modelOrder.getOrderProducts()
                    .stream()
                    .map(it -> new ReserveProductEntry(it.getSku(), it.getProductQuantity()))
                    .collect(Collectors.collectingAndThen(Collectors.toSet(), ProductReservationList::new));

            ProductReservationStatus reservationStatus = externalProductReservationService.reserve(store,
                    productReservation);
            if (!reservationStatus.status()) {
                throw new ServiceException("error updating inventory with new qty");
            }

            // update cart
            cart.setOrderId(modelOrder.getId());
            shoppingCartFacade.saveOrUpdateShoppingCart(cart);

            return modelOrder;

        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public ReadableOrderConfirmation orderConfirmation(Order order, Customer customer, StoreMerchantId store,
                                                       LanguageCode language) {
        Validate.notNull(order, "Order cannot be null");
        Validate.notNull(customer, "Customer cannot be null");
        Validate.notNull(store, "store cannot be null");

        ReadableOrderConfirmation orderConfirmation = new ReadableOrderConfirmation();

        ReadableCustomer readableCustomer = readableCustomerMapper.convert(customer, store, language);
        orderConfirmation.setBilling(readableCustomer.getBilling());
        orderConfirmation.setDelivery(readableCustomer.getDelivery());

        ReadableTotal readableTotal = new ReadableTotal();

        Set<OrderTotal> totals = order.getOrderTotal();
        List<ReadableOrderTotal> readableTotals = totals.stream()
                .sorted(Comparator.comparingInt(OrderTotal::getSortOrder))
                .map(tot -> readableOrderTotalMapper.convert(tot, store, language))
                .collect(Collectors.toList());

        readableTotal.setTotals(readableTotals);

        Optional<ReadableOrderTotal> grandTotal = readableTotals.stream()
                .filter(tot -> tot.getCode().equals("order.total.total"))
                .findFirst();

        grandTotal.ifPresent(readableOrderTotal -> readableTotal.setGrandTotal(readableOrderTotal.getText()));
        orderConfirmation.setTotal(readableTotal);

        List<ReadableOrderProduct> products = order.getOrderProducts()
                .stream()
                .map(pr -> readableOrderProductMapper.convert(pr, store, language))
                .collect(Collectors.toList());
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
        Validate.notNull(store, "store cannot be null");
        Order modelOrder = orderService.getOrder(orderId, store);
        if (modelOrder == null) {
            throw new ResourceNotFoundException("Order not found with id " + orderId);
        }

        ReadableOrder readableOrder = new ReadableOrder();

        Long customerId = modelOrder.getCustomerId();
        if (customerId != null) {
            ReadableCustomer readableCustomer = customerFacade.getCustomerById(customerId, store, language);
            if (readableCustomer == null) {
                log.warn("Customer id {} not found in order {}", customerId, orderId);
            } else {
                readableOrder.setCustomer(readableCustomer);
            }
        }

        try {
            readableOrderPopulator.populate(modelOrder, readableOrder, store, language);

            // order products
            List<ReadableOrderProduct> orderProducts = new ArrayList<>();
            for (OrderProduct p : modelOrder.getOrderProducts()) {
                ReadableOrderProductPopulator orderProductPopulator = new ReadableOrderProductPopulator(
                        externalProductService, imageUtils, externalMerchantStoreService);

                ReadableOrderProduct orderProduct = new ReadableOrderProduct();
                orderProductPopulator.populate(p, orderProduct, store, language);
                orderProducts.add(orderProduct);
            }

            readableOrder.setProducts(orderProducts);
        } catch (Exception e) {
            throw new ServiceRuntimeException("Error while getting order [" + orderId + "]");
        }

        return readableOrder;
    }

    @Override
    public ReadableOrder getReadableOrder(Long orderId, Long customerId, StoreMerchantId store, LanguageCode language) {
        Validate.notNull(store, "store cannot be null");
        Order modelOrder = orderService.getOrder(orderId, store);
        if (modelOrder == null) {
            throw new ResourceNotFoundException("Order not found with id " + orderId);
        }
        if (modelOrder.getCustomerId() == null || !modelOrder.getCustomerId().equals(customerId)) {
            throw new ResourceNotFoundException("Order not found with id " + orderId + " for customer " + customerId);
        }

        ReadableOrder readableOrder = new ReadableOrder();

        ReadableCustomer readableCustomer = customerFacade.getCustomerById(customerId, store, language);
        if (readableCustomer == null) {
            log.warn("Customer id {} not found in order {}", customerId, orderId);
        } else {
            readableOrder.setCustomer(readableCustomer);
        }

        try {
            readableOrderPopulator.populate(modelOrder, readableOrder, store, language);

            // order products
            List<ReadableOrderProduct> orderProducts = new ArrayList<>();
            for (OrderProduct p : modelOrder.getOrderProducts()) {
                ReadableOrderProductPopulator orderProductPopulator = new ReadableOrderProductPopulator(
                        externalProductService, imageUtils, externalMerchantStoreService);

                ReadableOrderProduct orderProduct = new ReadableOrderProduct();
                orderProductPopulator.populate(p, orderProduct, store, language);
                orderProducts.add(orderProduct);
            }

            readableOrder.setProducts(orderProducts);
        } catch (Exception e) {
            throw new ServiceRuntimeException("Error while getting order [" + orderId + "]");
        }

        return readableOrder;
    }

    @Override
    public List<ReadableOrderStatusHistory> getReadableOrderHistory(Long orderId, StoreMerchantId store,
                                                                    LanguageCode language) {

        Order order = orderService.getOrder(orderId, store);
        if (order == null) {
            throw new ResourceNotFoundException("Order id [" + orderId + "] not found for merchand [" + store + "]");
        }

        Set<OrderStatusHistory> historyList = order.getOrderHistory();
        return historyList.stream().map(this::mapToReadbleOrderStatusHistory).collect(Collectors.toList());
    }

    @Override
    public List<ReadableOrderStatusHistory> getReadableOrderHistory(Long orderId, Long customerId,
                                                                    StoreMerchantId store, LanguageCode language) {

        Order order = orderService.getOrder(orderId, store);
        if (order == null) {
            throw new ResourceNotFoundException("Order id [" + orderId + "] not found for merchand [" + store + "]");
        }

        if (order.getCustomerId() == null || !order.getCustomerId().equals(customerId)) {
            throw new ResourceNotFoundException("Order not found with id " + orderId + " for customer " + customerId);
        }

        Set<OrderStatusHistory> historyList = order.getOrderHistory();
        return historyList.stream().map(this::mapToReadbleOrderStatusHistory).collect(Collectors.toList());
    }

    ReadableOrderStatusHistory mapToReadbleOrderStatusHistory(OrderStatusHistory source) {
        ReadableOrderStatusHistory readable = new ReadableOrderStatusHistory();
        readable.setComments(source.getComments());
        readable.setDate(DateUtil.formatLongDate(source.getDateAdded()));
        readable.setId(source.getId());
        readable.setOrderId(source.getOrder().getId());
        readable.setOrderStatus(source.getStatus());

        return readable;
    }

    @Override
    public void createOrderStatus(PersistableOrderStatusHistory status, Long id, StoreMerchantId store) {
        Validate.notNull(status, "OrderStatusHistory must not be null");
        Validate.notNull(id, "Order id must not be null");
        Validate.notNull(store, "store cannot be null");

        // retrieve original order
        Order order = orderService.getOrder(id, store);
        if (order == null) {
            throw new ResourceNotFoundException(
                    "Order with id [" + id + "] does not exist for merchant [" + store + "]");
        }

        try {
            OrderStatusHistory history = new OrderStatusHistory();
            history.setComments(status.getComments());
            history.setDateAdded(DateUtil.getDate(status.getDate()));
            history.setOrder(order);
            history.setStatus(status.getOrderStatus());

            orderService.addOrderStatusHistory(order, history);

        } catch (Exception e) {
            throw new ServiceRuntimeException("An error occured while converting orderstatushistory", e);
        }
    }

    private void notify(Order modelOrder, Customer customer, StoreMerchantId store, LanguageCode language,
                        Locale locale) {
        // @TODO ASHRAF
    }

}
