package com.asrevo.cvhome.order.services.order;

import com.asrevo.cvhome.catalog.services.product.ExternalProductService;
import com.asrevo.cvhome.commons.domain.Entry;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.order.entity.customer.Customer;
import com.asrevo.cvhome.order.entity.order.*;
import com.asrevo.cvhome.order.entity.order.orderstatus.OrderStatusHistory;
import com.asrevo.cvhome.order.entity.payments.Transaction;
import com.asrevo.cvhome.order.entity.shoppingcart.ShoppingCart;
import com.asrevo.cvhome.order.entity.shoppingcart.ShoppingCartItem;
import com.asrevo.cvhome.order.model.order.OrderCriteria;
import com.asrevo.cvhome.order.model.order.OrderSummaryType;
import com.asrevo.cvhome.order.model.payments.Payment;
import com.asrevo.cvhome.order.repositories.order.OrderRepository;
import com.asrevo.cvhome.order.services.customer.CustomerService;
import com.asrevo.cvhome.order.services.shoppingcart.ShoppingCartService;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.catalog.ProductQuantityUpdate;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

// @TODO ASHRAF
@Service
@Slf4j
public class OrderServiceImpl extends SalesManagerEntityServiceImpl<Long, Order>
        implements OrderService {

    private final ShoppingCartService shoppingCartService;
    private final ExternalProductService productService;
    private final CustomerService customerService;
    private final OrderRepository orderRepository;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            ShoppingCartService shoppingCartService,
            ExternalProductService productService,
            CustomerService customerService) {
        super(orderRepository);
        this.shoppingCartService = shoppingCartService;
        this.productService = productService;
        this.customerService = customerService;
        this.orderRepository = orderRepository;
    }

    @Override
    public void addOrderStatusHistory(Order order, OrderStatusHistory history)
            throws ServiceException {
        order.setStatus(history.getStatus());
        order.getOrderHistory().add(history);
        history.setOrder(order);
        update(order);
    }

    @Override
    public OrderTotalSummary caculateOrderTotal(
            OrderSummary orderSummary,
            Customer customer,
            StoreMerchantId store,
            LanguageCode language)
            throws ServiceException {
        Assert.notNull(orderSummary, "Order summary cannot be null");
        Assert.notNull(orderSummary.getProducts(), "Order summary.products cannot be null");
        Assert.notNull(store, "Store cannot be null");
        Assert.notNull(customer, "Customer cannot be null");

        try {
            return calculateOrder(orderSummary, customer, store, language);
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public OrderTotalSummary calculateShoppingCartTotal(
            final ShoppingCart shoppingCart,
            final Customer customer,
            final StoreMerchantId store,
            final LanguageCode language)
            throws ServiceException {
        Assert.notNull(shoppingCart, "Order summary cannot be null");
        Assert.notNull(customer, "Customery cannot be null");
        Assert.notNull(store, "Store cannot be null.");
        try {
            return caculateshoppingcart(shoppingCart, customer, store, language);
        } catch (Exception e) {
            log.error("Error while calculating shopping cart total", e);
            throw new ServiceException(e);
        }
    }

    private OrderTotalSummary caculateshoppingcart(
            ShoppingCart shoppingCart,
            final Customer customer,
            final StoreMerchantId store,
            final LanguageCode language)
            throws Exception {

        OrderSummary orderSummary = new OrderSummary();
        orderSummary.setOrderSummaryType(OrderSummaryType.SHOPPINGCART);

        if (!StringUtils.isBlank(shoppingCart.getPromoCode())) {
            Date promoDateAdded = shoppingCart.getPromoAdded(); // promo valid 1 day
            if (promoDateAdded == null) {
                promoDateAdded = new Date();
            }
            Instant instant = promoDateAdded.toInstant();
            ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());
            LocalDate date = zdt.toLocalDate();
            // date added < date + 1 day
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            if (date.isBefore(tomorrow)) {
                orderSummary.setPromoCode(shoppingCart.getPromoCode());
            } else {
                // clear promo
                shoppingCart.setPromoCode(null);
                shoppingCartService.saveOrUpdate(shoppingCart);
            }
        }

        List<ShoppingCartItem> itemList = new ArrayList<>(shoppingCart.getLineItems());
        orderSummary.setProducts(itemList);
        // filter out unavailable
        //        List<String> skus = itemList.stream().map(ShoppingCartItem::getSku).toList();
        //        Map<String, ReadableProductAvailability> availabilityMap =
        //                productService.getProductsAvailability(store, skus).stream()
        //                        .collect(
        //                                Collectors.toMap(
        //                                        ReadableProductAvailability::getSku,
        // Function.identity()));
        //        itemList =
        //                itemList.stream()
        //                        .filter(
        //                                p -> {
        //                                    var availability = availabilityMap.get(p.getSku());
        //                                    return Optional.ofNullable(availability)
        //
        // .map(ReadableProductAvailability::isCanBePurchased)
        //                                            .orElse(Boolean.FALSE);
        //                                })
        //                        .toList();
        //        orderSummary.setProducts(itemList);

        return calculateOrder(orderSummary, customer, store, language);
    }

    /**
     * <p>Method will be used to calculate Shopping cart total as well will update price for each
     * line items.
     * </p>
     *
     * @return {@link OrderTotalSummary}
     */
    @Override
    public OrderTotalSummary calculateShoppingCartTotal(
            final ShoppingCart shoppingCart,
            final StoreMerchantId store,
            final LanguageCode language)
            throws ServiceException {
        Assert.notNull(shoppingCart, "Order summary cannot be null");
        Assert.notNull(store, "Store cannot be null");

        try {
            return caculateshoppingcart(shoppingCart, null, store, language);
        } catch (Exception e) {
            log.error("Error while calculating shopping cart total", e);
            throw new ServiceException(e);
        }
    }

    @Override
    public Order processOrder(
            Order order,
            Customer customer,
            List<ShoppingCartItem> items,
            OrderTotalSummary summary,
            Payment payment,
            StoreMerchantId store)
            throws ServiceException {

        return process(order, customer, items, summary, payment, null, store);
    }

    @Override
    public void delete(final Order order) throws ServiceException {

        super.delete(order);
    }

    @Override
    public Order getOrder(final Long orderId, StoreMerchantId store) {
        Validate.notNull(orderId, "Order id cannot be null");
        Validate.notNull(store, "Store cannot be null");
        return orderRepository.findOne(orderId, store);
    }

    @Override
    public OrderList getOrders(final OrderCriteria criteria, StoreMerchantId store) {
        return orderRepository.listOrders(store, criteria);
    }

    private Order process(
            Order order,
            Customer customer,
            List<ShoppingCartItem> items,
            OrderTotalSummary summary,
            Payment payment,
            Transaction transaction,
            StoreMerchantId store)
            throws ServiceException {

        Assert.notNull(order, "Order cannot be null");
        Assert.notNull(customer, "Customer cannot be null (even if anonymous order)");
        Assert.notEmpty(items, "ShoppingCart items cannot be null");
        Assert.notNull(payment, "Payment cannot be null");
        Assert.notNull(store, "Store cannot be null");
        Assert.notNull(summary, "Order total Summary cannot be null");

        // @TODO check if we can store ip address
        // order.setIpAddress(ipAddress);

        // first process payment
        // @TODO ASHRAF
        //        Transaction processTransaction = paymentService.processPayment(customer, store,
        // payment, items, order);

        if (order.getOrderHistory() == null
                || order.getOrderHistory().isEmpty()
                || order.getStatus() == null) {
            OrderStatus status = order.getStatus();
            if (status == null) {
                status = OrderStatus.ORDERED;
                order.setStatus(status);
            }
            Set<OrderStatusHistory> statusHistorySet = new HashSet<>();
            OrderStatusHistory statusHistory = new OrderStatusHistory();
            statusHistory.setStatus(status);
            statusHistory.setDateAdded(new Date());
            statusHistory.setOrder(order);
            statusHistorySet.add(statusHistory);
            order.setOrderHistory(statusHistorySet);
        }

        if (customer.getId() == null || customer.getId() == 0) {
            customerService.create(customer);
        }

        order.setCustomerId(customer.getId());
        this.create(order);

        /*        @TODO ASHRAF

                            if(transaction!=null) {
                    transaction.setOrder(order);
                    if(transaction.getId()==null || transaction.getId()==0) {
                        transactionService.create(transaction);
                    } else {
                        transactionService.update(transaction);
                    }
                }



        if(processTransaction!=null) {
                    processTransaction.setOrder(order);
                    if(processTransaction.getId()==null || processTransaction.getId()==0) {
                        transactionService.create(processTransaction);
                    } else {
                        transactionService.update(processTransaction);
                    }
                }*/

        log.debug("Update inventory");
        List<Entry<String, Integer>> newAvailabilities =
                order.getOrderProducts().stream()
                        .map(it -> new Entry<>(it.getSku(), it.getProductQuantity()))
                        .toList();
        productService.productQuantityUpdate(store, new ProductQuantityUpdate(newAvailabilities));
        return order;
    }

    @SneakyThrows
    private OrderTotalSummary calculateOrder(
            OrderSummary summary,
            Customer customer,
            final StoreMerchantId store,
            final LanguageCode language) {

        OrderTotalSummary totalSummary = new OrderTotalSummary();
        List<OrderTotal> orderTotals = new ArrayList<>();
        Map<String, OrderTotal> otherPricesTotals = new HashMap<>();

        //  @TODO ASHRAF
        //        ShippingConfiguration shippingConfiguration = null;

        BigDecimal grandTotal = new BigDecimal(0);
        grandTotal.setScale(2, RoundingMode.HALF_UP);

        // price by item
        BigDecimal subTotal = new BigDecimal(0);
        subTotal.setScale(2, RoundingMode.HALF_UP);
        for (ShoppingCartItem item : summary.getProducts()) {

            BigDecimal st = item.getItemPrice().multiply(new BigDecimal(item.getQuantity()));
            item.setSubTotal(st);
            subTotal = subTotal.add(st);
            // Other prices

            /* @TODO comment this for now as unnecessary
                        FinalPrice finalPrice = productService.getProductPrice(store, item.getSku());
                        if (finalPrice != null) {
                            List<FinalPrice> otherPrices = finalPrice.getAdditionalPrices();
                            if (otherPrices != null) {
                                for (FinalPrice price : otherPrices) {
                                    if (!price.isDefaultPrice()) {
                                        OrderTotal itemSubTotal =
                                                otherPricesTotals.get(price.getProductPrice().getCode());

                                        if (itemSubTotal == null) {
                                            itemSubTotal = new OrderTotal();
                                            itemSubTotal.setModule(Constants.OT_ITEM_PRICE_MODULE_CODE);
                                            itemSubTotal.setTitle(Constants.OT_ITEM_PRICE_MODULE_CODE);
                                            itemSubTotal.setOrderTotalCode(price.getProductPrice().getCode());
                                            itemSubTotal.setOrderTotalType(OrderTotalType.PRODUCT);
                                            itemSubTotal.setSortOrder(0);
                                            otherPricesTotals.put(
                                                    price.getProductPrice().getCode(), itemSubTotal);
                                        }

                                        BigDecimal orderTotalValue = itemSubTotal.getValue();
                                        if (orderTotalValue == null) {
                                            orderTotalValue = new BigDecimal(0);
                                            orderTotalValue.setScale(2, RoundingMode.HALF_UP);
                                        }

                                        orderTotalValue = orderTotalValue.add(price.getFinalPrice());
                                        itemSubTotal.setValue(orderTotalValue);
                                        if (OrderValueType.ONE_TIME
                                                .name()
                                                .equals(price.getProductPrice().getProductPriceType().name())) {
                                            subTotal = subTotal.add(price.getFinalPrice());
                                        }
                                    }
                                }
                            }
                        }
            */
        }

        //  @TODO ASHRAF
        // only in order page, otherwise invokes too many processing
        //        if(
        //
        // OrderSummaryType.ORDERTOTAL.name().equals(summary.getOrderSummaryType().name()) ||
        //
        // OrderSummaryType.SHOPPINGCART.name().equals(summary.getOrderSummaryType().name())
        //
        //        ) {
        //
        //            //Post processing order total variation modules for sub total calculation -
        // drools, custom modules
        //            //may affect the sub total
        //            OrderTotalVariation orderTotalVariation =
        // orderTotalService.findOrderTotalVariation(summary, customer, store, language);
        //
        //            int currentCount = 10;
        //
        //            if(CollectionUtils.isNotEmpty(orderTotalVariation.getVariations())) {
        //                for(OrderTotal variation : orderTotalVariation.getVariations()) {
        //                    variation.setSortOrder(currentCount++);
        //                    orderTotals.add(variation);
        //                    subTotal = subTotal.subtract(variation.getValue());
        //                }
        //            }
        //
        //        }

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

        // shipping
        //        if (summary.getShippingSummary() != null) {
        //
        //
        //            OrderTotal shippingSubTotal = new OrderTotal();
        //            shippingSubTotal.setModule(Constants.OT_SHIPPING_MODULE_CODE);
        //            shippingSubTotal.setOrderTotalType(OrderTotalType.SHIPPING);
        //            shippingSubTotal.setOrderTotalCode("order.total.shipping");
        //            shippingSubTotal.setTitle(Constants.OT_SHIPPING_MODULE_CODE);
        //            shippingSubTotal.setSortOrder(100);
        //
        //            orderTotals.add(shippingSubTotal);
        //
        //            if (!summary.getShippingSummary().isFreeShipping()) {
        //                shippingSubTotal.setValue(summary.getShippingSummary().getShipping());
        //                grandTotal = grandTotal.add(summary.getShippingSummary().getShipping());
        //            } else {
        //                shippingSubTotal.setValue(new BigDecimal(0));
        //                grandTotal = grandTotal.add(new BigDecimal(0));
        //            }
        //
        ////  @TODO ASHRAF
        //            //check handling fees
        /// *
        //            shippingConfiguration = shippingService.getShippingConfiguration(store);
        //            if(summary.getShippingSummary().getHandling()!=null &&
        // summary.getShippingSummary().getHandling().doubleValue()>0) {
        //                if(shippingConfiguration.getHandlingFees()!=null &&
        // shippingConfiguration.getHandlingFees().doubleValue()>0) {
        //                    OrderTotal handlingubTotal = new OrderTotal();
        //                    handlingubTotal.setModule(Constants.OT_HANDLING_MODULE_CODE);
        //                    handlingubTotal.setOrderTotalType(OrderTotalType.HANDLING);
        //                    handlingubTotal.setOrderTotalCode("order.total.handling");
        //                    handlingubTotal.setTitle(Constants.OT_HANDLING_MODULE_CODE);
        //                    //handlingubTotal.setText("order.total.handling");
        //                    handlingubTotal.setSortOrder(120);
        //                    handlingubTotal.setValue(summary.getShippingSummary().getHandling());
        //                    orderTotals.add(handlingubTotal);
        //                    grandTotal=grandTotal.add(summary.getShippingSummary().getHandling());
        //                }
        //            }
        // */
        //        }
        //  @TODO ASHRAF
        // tax
        /*
                List<TaxItem> taxes = taxService.calculateTax(summary, customer, store, language);
                if(taxes!=null && taxes.size()>0) {
                    BigDecimal totalTaxes = new BigDecimal(0);
                    totalTaxes.setScale(2, RoundingMode.HALF_UP);
                    int taxCount = 200;
                    for(TaxItem tax : taxes) {

                        OrderTotal taxLine = new OrderTotal();
                        taxLine.setModule(Constants.OT_TAX_MODULE_CODE);
                        taxLine.setOrderTotalType(OrderTotalType.TAX);
                        taxLine.setOrderTotalCode(tax.getLabel());
                        taxLine.setSortOrder(taxCount);
                        taxLine.setTitle(Constants.OT_TAX_MODULE_CODE);
                        taxLine.setText(tax.getLabel());
                        taxLine.setValue(tax.getItemPrice());

                        totalTaxes = totalTaxes.add(tax.getItemPrice());
                        orderTotals.add(taxLine);
                        //grandTotal=grandTotal.add(tax.getItemPrice());

                        taxCount ++;

                    }
                    grandTotal = grandTotal.add(totalTaxes);
                    totalSummary.setTaxTotal(totalTaxes);

                }
        */
        // grand total
        OrderTotal orderTotal = new OrderTotal();
        orderTotal.setModule(Constants.OT_TOTAL_MODULE_CODE);
        orderTotal.setOrderTotalType(OrderTotalType.TOTAL);
        orderTotal.setOrderTotalCode("order.total.total");
        orderTotal.setTitle(Constants.OT_TOTAL_MODULE_CODE);
        // orderTotal.setText("order.total.total");
        orderTotal.setSortOrder(500);
        orderTotal.setValue(grandTotal);
        orderTotals.add(orderTotal);

        totalSummary.setTotal(grandTotal);
        totalSummary.setTotals(orderTotals);
        return totalSummary;
    }
}
