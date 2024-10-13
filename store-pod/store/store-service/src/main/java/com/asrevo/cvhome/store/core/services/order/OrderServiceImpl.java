package com.asrevo.cvhome.store.core.services.order;

import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.availability.ProductAvailability;
import com.asrevo.cvhome.store.core.entity.catalog.product.price.FinalPrice;
import com.asrevo.cvhome.store.core.entity.customer.Customer;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.order.Order;
import com.asrevo.cvhome.store.core.entity.order.OrderTotal;
import com.asrevo.cvhome.store.core.entity.order.OrderTotalType;
import com.asrevo.cvhome.store.core.entity.order.OrderValueType;
import com.asrevo.cvhome.store.core.entity.order.orderproduct.OrderProduct;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatusHistory;
import com.asrevo.cvhome.store.core.entity.payments.Transaction;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.entity.shoppingcart.ShoppingCart;
import com.asrevo.cvhome.store.core.entity.shoppingcart.ShoppingCartItem;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.common.UserContext;
import com.asrevo.cvhome.store.core.model.order.*;
import com.asrevo.cvhome.store.core.model.payments.Payment;
import com.asrevo.cvhome.store.core.repositories.order.OrderRepository;
import com.asrevo.cvhome.store.core.services.catalog.product.ProductService;
import com.asrevo.cvhome.store.core.services.customer.CustomerService;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import com.asrevo.cvhome.store.core.services.shoppingcart.ShoppingCartService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

// @TODO ASHRAF
@Service
@Slf4j
public class OrderServiceImpl extends SalesManagerEntityServiceImpl<Long, Order>
        implements OrderService {

    private final ShoppingCartService shoppingCartService;
    private final ProductService productService;
    private final CustomerService customerService;
    private final OrderRepository orderRepository;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            ShoppingCartService shoppingCartService,
            ProductService productService,
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
            final OrderSummary orderSummary,
            final Customer customer,
            final MerchantStore store,
            final Language language)
            throws ServiceException {
        Assert.notNull(orderSummary, "Order summary cannot be null");
        Assert.notNull(orderSummary.getProducts(), "Order summary.products cannot be null");
        Assert.notNull(store, "MerchantStore cannot be null");
        Assert.notNull(customer, "Customer cannot be null");

        try {
            return caculateOrder(orderSummary, customer, store, language);
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public OrderTotalSummary caculateOrderTotal(
            final OrderSummary orderSummary, final MerchantStore store, final Language language)
            throws ServiceException {
        Assert.notNull(orderSummary, "Order summary cannot be null");
        Assert.notNull(orderSummary.getProducts(), "Order summary.products cannot be null");
        Assert.notNull(store, "MerchantStore cannot be null");

        try {
            return caculateOrder(orderSummary, null, store, language);
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public OrderTotalSummary calculateShoppingCartTotal(
            final ShoppingCart shoppingCart,
            final Customer customer,
            final MerchantStore store,
            final Language language)
            throws ServiceException {
        Assert.notNull(shoppingCart, "Order summary cannot be null");
        Assert.notNull(customer, "Customery cannot be null");
        Assert.notNull(store, "MerchantStore cannot be null.");
        try {
            return caculateShoppingCart(shoppingCart, customer, store, language);
        } catch (Exception e) {
            log.error("Error while calculating shopping cart total{}", e);
            throw new ServiceException(e);
        }
    }

    private OrderTotalSummary caculateShoppingCart(
            ShoppingCart shoppingCart,
            final Customer customer,
            final MerchantStore store,
            final Language language)
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
        // filter out unavailable
        itemList =
                itemList.stream()
                        .filter(p -> p.getProduct().isAvailable())
                        .collect(Collectors.toList());
        orderSummary.setProducts(itemList);

        return caculateOrder(orderSummary, customer, store, language);
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
            final ShoppingCart shoppingCart, final MerchantStore store, final Language language)
            throws ServiceException {
        Assert.notNull(shoppingCart, "Order summary cannot be null");
        Assert.notNull(store, "MerchantStore cannot be null");

        try {
            return caculateShoppingCart(shoppingCart, null, store, language);
        } catch (Exception e) {
            log.error("Error while calculating shopping cart total{}", e);
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
            MerchantStore store)
            throws ServiceException {

        return process(order, customer, items, summary, payment, null, store);
    }

    @Override
    public Order processOrder(
            Order order,
            Customer customer,
            List<ShoppingCartItem> items,
            OrderTotalSummary summary,
            Payment payment,
            Transaction transaction,
            MerchantStore store)
            throws ServiceException {
        return process(order, customer, items, summary, payment, transaction, store);
    }

    @Override
    public void delete(final Order order) throws ServiceException {

        super.delete(order);
    }

    @Override
    public Order getOrder(final Long orderId, MerchantStore store) {
        Validate.notNull(orderId, "Order id cannot be null");
        Validate.notNull(store, "Store cannot be null");
        return orderRepository.findOne(orderId, store.getId());
    }

    /**
     * legacy
     **/
    @Override
    public OrderList listByStore(final MerchantStore store, final OrderCriteria criteria) {
        return orderRepository.listByStore(store, criteria);
    }

    @Override
    public OrderList getOrders(final OrderCriteria criteria, MerchantStore store) {
        return orderRepository.listOrders(store, criteria);
    }

    @Override
    public void saveOrUpdate(final Order order) throws ServiceException {

        if (order.getId() != null && order.getId() > 0) {
            log.debug("Updating Order");
            super.update(order);

        } else {
            log.debug("Creating Order");
            super.create(order);
        }
    }

    @Override
    public boolean hasDownloadFiles(Order order) {

        Validate.notNull(order, "Order cannot be null");
        Validate.notNull(order.getOrderProducts(), "Order products cannot be null");
        Validate.notEmpty(order.getOrderProducts(), "Order products cannot be empty");

        boolean hasDownloads = false;
        for (OrderProduct orderProduct : order.getOrderProducts()) {

            if (!CollectionUtils.isEmpty(orderProduct.getDownloads())) {
                hasDownloads = true;
                break;
            }
        }

        return hasDownloads;
    }

    private Order process(
            Order order,
            Customer customer,
            List<ShoppingCartItem> items,
            OrderTotalSummary summary,
            Payment payment,
            Transaction transaction,
            MerchantStore store)
            throws ServiceException {

        Assert.notNull(order, "Order cannot be null");
        Assert.notNull(customer, "Customer cannot be null (even if anonymous order)");
        Assert.notEmpty(items, "ShoppingCart items cannot be null");
        Assert.notNull(payment, "Payment cannot be null");
        Assert.notNull(store, "MerchantStore cannot be null");
        Assert.notNull(summary, "Order total Summary cannot be null");

        UserContext context = UserContext.getCurrentInstance();
        if (context != null) {
            String ipAddress = context.getIpAddress();
            if (!StringUtils.isBlank(ipAddress)) {
                order.setIpAddress(ipAddress);
            }
        }

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
        Set<OrderProduct> products = order.getOrderProducts();
        for (OrderProduct orderProduct : products) {
            //            Product p = productService.getById(orderProduct.getId());
            Product p = productService.getBySku(orderProduct.getSku(), store);
            if (p == null)
                throw new ServiceException(ServiceException.EXCEPTION_INVENTORY_MISMATCH);
            for (ProductAvailability availability : p.getAvailabilities()) {
                int qty = availability.getProductQuantity();
                if (qty < orderProduct.getProductQuantity()) {
                    // throw new ServiceException(ServiceException.EXCEPTION_INVENTORY_MISMATCH);
                    log.error(
                            "APP-BACKEND [" + ServiceException.EXCEPTION_INVENTORY_MISMATCH + "]");
                }
                qty = qty - orderProduct.getProductQuantity();
                availability.setProductQuantity(qty);
            }
            productService.update(p);
        }

        return order;
    }

    private OrderTotalSummary caculateOrder(
            OrderSummary summary,
            Customer customer,
            final MerchantStore store,
            final Language language)
            throws Exception {

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
            FinalPrice finalPrice = item.getFinalPrice();
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
                            if (price.getProductPrice()
                                    .getProductPriceType()
                                    .name()
                                    .equals(OrderValueType.ONE_TIME)) {
                                subTotal = subTotal.add(price.getFinalPrice());
                            }
                        }
                    }
                }
            }
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
        if (summary.getShippingSummary() != null) {

            OrderTotal shippingSubTotal = new OrderTotal();
            shippingSubTotal.setModule(Constants.OT_SHIPPING_MODULE_CODE);
            shippingSubTotal.setOrderTotalType(OrderTotalType.SHIPPING);
            shippingSubTotal.setOrderTotalCode("order.total.shipping");
            shippingSubTotal.setTitle(Constants.OT_SHIPPING_MODULE_CODE);
            shippingSubTotal.setSortOrder(100);

            orderTotals.add(shippingSubTotal);

            if (!summary.getShippingSummary().isFreeShipping()) {
                shippingSubTotal.setValue(summary.getShippingSummary().getShipping());
                grandTotal = grandTotal.add(summary.getShippingSummary().getShipping());
            } else {
                shippingSubTotal.setValue(new BigDecimal(0));
                grandTotal = grandTotal.add(new BigDecimal(0));
            }

            //  @TODO ASHRAF
            // check handling fees
            /*
                        shippingConfiguration = shippingService.getShippingConfiguration(store);
                        if(summary.getShippingSummary().getHandling()!=null && summary.getShippingSummary().getHandling().doubleValue()>0) {
                            if(shippingConfiguration.getHandlingFees()!=null && shippingConfiguration.getHandlingFees().doubleValue()>0) {
                                OrderTotal handlingubTotal = new OrderTotal();
                                handlingubTotal.setModule(Constants.OT_HANDLING_MODULE_CODE);
                                handlingubTotal.setOrderTotalType(OrderTotalType.HANDLING);
                                handlingubTotal.setOrderTotalCode("order.total.handling");
                                handlingubTotal.setTitle(Constants.OT_HANDLING_MODULE_CODE);
                                //handlingubTotal.setText("order.total.handling");
                                handlingubTotal.setSortOrder(120);
                                handlingubTotal.setValue(summary.getShippingSummary().getHandling());
                                orderTotals.add(handlingubTotal);
                                grandTotal=grandTotal.add(summary.getShippingSummary().getHandling());
                            }
                        }
            */
        }
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
