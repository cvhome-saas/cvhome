package com.asrevo.cvhome.store.service.facade.order;

import com.asrevo.cvhome.store.controller.exception.ConversionException;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.entity.customer.Customer;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.order.Order;
import com.asrevo.cvhome.store.core.entity.order.OrderTotal;
import com.asrevo.cvhome.store.core.entity.order.attributes.OrderAttribute;
import com.asrevo.cvhome.store.core.entity.order.orderproduct.OrderProduct;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatusHistory;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.entity.shoppingcart.ShoppingCart;
import com.asrevo.cvhome.store.core.entity.shoppingcart.ShoppingCartItem;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.customer.ReadableCustomer;
import com.asrevo.cvhome.store.core.model.order.*;
import com.asrevo.cvhome.store.core.model.order.history.PersistableOrderStatusHistory;
import com.asrevo.cvhome.store.core.model.order.history.ReadableOrderStatusHistory;
import com.asrevo.cvhome.store.core.model.order.total.ReadableOrderTotal;
import com.asrevo.cvhome.store.core.model.order.total.ReadableTotal;
import com.asrevo.cvhome.store.core.model.order.v1.PersistableOrder;
import com.asrevo.cvhome.store.core.model.order.v1.ReadableOrderConfirmation;
import com.asrevo.cvhome.store.core.model.payments.Payment;
import com.asrevo.cvhome.store.core.model.shipping.ShippingSummary;
import com.asrevo.cvhome.store.core.services.catalog.pricing.PricingService;
import com.asrevo.cvhome.store.core.services.catalog.product.ProductService;
import com.asrevo.cvhome.store.core.services.catalog.product.attribute.ProductAttributeService;
import com.asrevo.cvhome.store.core.services.order.OrderService;
import com.asrevo.cvhome.store.core.services.reference.language.LanguageService;
import com.asrevo.cvhome.store.core.services.shipping.ShippingQuoteService;
import com.asrevo.cvhome.store.core.services.shoppingcart.ShoppingCartService;
import com.asrevo.cvhome.store.service.facade.cart.ShoppingCartFacade;
import com.asrevo.cvhome.store.service.facade.customer.CustomerFacade;
import com.asrevo.cvhome.store.service.mapper.customer.ReadableCustomerMapper;
import com.asrevo.cvhome.store.service.mapper.order.ReadableOrderProductMapper;
import com.asrevo.cvhome.store.service.mapper.order.ReadableOrderTotalMapper;
import com.asrevo.cvhome.store.service.populator.order.OrderProductPopulator;
import com.asrevo.cvhome.store.service.populator.order.PersistableOrderApiPopulator;
import com.asrevo.cvhome.store.service.populator.order.ReadableOrderPopulator;
import com.asrevo.cvhome.store.service.populator.order.ReadableOrderProductPopulator;
import com.asrevo.cvhome.store.service.populator.order.transaction.PersistablePaymentPopulator;
import com.asrevo.cvhome.store.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service("orderFacade")
@Slf4j
public class OrderFacadeImpl implements OrderFacade {


    private final ShoppingCartService shoppingCartService;
    private final ShoppingCartFacade shoppingCartFacade;

    private final OrderService orderService;
    private final ShippingQuoteService shippingQuoteService;
    private final ProductAttributeService productAttributeService;
    private final ProductService productService;
    private final PersistableOrderApiPopulator persistableOrderApiPopulator;
    private final ProductPriceUtils productPriceUtils;
    private final PricingService pricingService;
    private final CustomerFacade customerFacade;
    private final CoreConfiguration coreConfiguration;
    private final LabelUtils messages;
    private final ReadableOrderTotalMapper readableOrderTotalMapper;
    private final LanguageService languageService;
    private final ReadableCustomerMapper readableCustomerMapper;
    private final ReadableOrderProductMapper readableOrderProductMapper;
    private final ReadableOrderPopulator readableOrderPopulator;
    private final ImageFilePathUtils imageUtils;

    public OrderFacadeImpl(ShoppingCartFacade shoppingCartFacade, ShoppingCartService shoppingCartService, OrderService orderService, ShippingQuoteService shippingQuoteService, ProductAttributeService productAttributeService, ProductService productService, PersistableOrderApiPopulator persistableOrderApiPopulator, ProductPriceUtils productPriceUtils, PricingService pricingService, ReadableOrderProductMapper readableOrderProductMapper, LabelUtils messages, CustomerFacade customerFacade, CoreConfiguration coreConfiguration, ReadableCustomerMapper readableCustomerMapper, ReadableOrderTotalMapper readableOrderTotalMapper, LanguageService languageService, ReadableOrderPopulator readableOrderPopulator, ImageFilePathUtils imageUtils) {
        this.shoppingCartFacade = shoppingCartFacade;
        this.shoppingCartService = shoppingCartService;
        this.orderService = orderService;
        this.shippingQuoteService = shippingQuoteService;
        this.productAttributeService = productAttributeService;
        this.productService = productService;
        this.persistableOrderApiPopulator = persistableOrderApiPopulator;
        this.productPriceUtils = productPriceUtils;
        this.pricingService = pricingService;
        this.readableOrderProductMapper = readableOrderProductMapper;
        this.messages = messages;
        this.customerFacade = customerFacade;
        this.coreConfiguration = coreConfiguration;
        this.readableCustomerMapper = readableCustomerMapper;
        this.readableOrderTotalMapper = readableOrderTotalMapper;
        this.languageService = languageService;
        this.readableOrderPopulator = readableOrderPopulator;
        this.imageUtils = imageUtils;
    }

    @Override
    public Order processOrder(PersistableOrder order, Customer customer,
                              MerchantStore store, Language language, Locale locale) throws ServiceException {

        Assert.notNull(order, "Order cannot be null");
        Assert.notNull(customer, "Customer cannot be null");
        Assert.notNull(store, "MerchantStore cannot be null");
        Assert.notNull(language, "Language cannot be null");
        Assert.notNull(locale, "Locale cannot be null");

        try {


            Order modelOrder = new Order();
            persistableOrderApiPopulator.populate(order, modelOrder, store, language);

            Long shoppingCartId = order.getShoppingCartId();
            ShoppingCart cart = shoppingCartService.getById(shoppingCartId, store);

            if (cart == null) {
                throw new ServiceException("Shopping cart with id " + shoppingCartId + " does not exist");
            }

            Set<ShoppingCartItem> shoppingCartItems = cart.getLineItems();

            List<ShoppingCartItem> items = new ArrayList<>(shoppingCartItems);

            Set<OrderProduct> orderProducts = new LinkedHashSet<>();

            OrderProductPopulator orderProductPopulator = new OrderProductPopulator();
//            orderProductPopulator.setDigitalProductService(digitalProductService);
            orderProductPopulator.setProductAttributeService(productAttributeService);
            orderProductPopulator.setProductService(productService);

            for (ShoppingCartItem item : shoppingCartItems) {
                OrderProduct orderProduct = new OrderProduct();
                orderProduct = orderProductPopulator.populate(item, orderProduct, store, language);
                orderProduct.setOrder(modelOrder);
                orderProducts.add(orderProduct);
            }

            modelOrder.setOrderProducts(orderProducts);

            if (order.getAttributes() != null && !order.getAttributes().isEmpty()) {
                Set<OrderAttribute> attrs = new HashSet<>();
                for (OrderAttribute attribute : order.getAttributes()) {
                    OrderAttribute attr = new OrderAttribute();
                    attr.setKey(attribute.getKey());
                    attr.setValue(attribute.getValue());
                    attr.setOrder(modelOrder);
                    attrs.add(attr);
                }
                modelOrder.setOrderAttributes(attrs);
            }

            // requires Shipping information (need a quote id calculated)
            ShippingSummary shippingSummary = null;

            // get shipping quote if asked for
            if (order.getShippingQuote() != null && order.getShippingQuote().longValue() > 0) {
                shippingSummary = shippingQuoteService.getShippingSummary(order.getShippingQuote(), store);
                if (shippingSummary != null) {
                    modelOrder.setShippingModuleCode(shippingSummary.getShippingModule());
                }
            }

            // requires Order Totals, this needs recalculation and then compare
            // total with the amount sent as part
            // of process order request. If totals does not match, an error
            // should be thrown.

            OrderTotalSummary orderTotalSummary = null;

            OrderSummary orderSummary = new OrderSummary();
            orderSummary.setShippingSummary(shippingSummary);
            List<ShoppingCartItem> itemsSet = new ArrayList<>(cart.getLineItems());
            orderSummary.setProducts(itemsSet);

            orderTotalSummary = orderService.caculateOrderTotal(orderSummary, customer, store, language);

            if (order.getPayment().getAmount() == null) {
                throw new ConversionException("Requires Payment.amount");
            }

            String submitedAmount = order.getPayment().getAmount();

            BigDecimal formattedSubmittedAmount = productPriceUtils.getAmount(submitedAmount);

            BigDecimal submitedAmountFormat = productPriceUtils.getAmount(submitedAmount);

            BigDecimal calculatedAmount = orderTotalSummary.getTotal();
            String strCalculatedTotal = calculatedAmount.toPlainString();

            // compare both prices
            if (calculatedAmount.compareTo(formattedSubmittedAmount) != 0) {


                throw new ConversionException("Payment.amount does not match what the system has calculated "
                        + strCalculatedTotal + " (received " + submitedAmount + ") please recalculate the order and submit again");
            }

            modelOrder.setTotal(calculatedAmount);
            List<OrderTotal> totals = orderTotalSummary.getTotals();
            Set<OrderTotal> set = new HashSet<OrderTotal>();

            if (!CollectionUtils.isEmpty(totals)) {
                for (OrderTotal total : totals) {
                    total.setOrder(modelOrder);
                    set.add(total);
                }
            }
            modelOrder.setOrderTotal(set);

            PersistablePaymentPopulator paymentPopulator = new PersistablePaymentPopulator();
            paymentPopulator.setPricingService(pricingService);
            Payment paymentModel = new Payment();
            paymentPopulator.populate(order.getPayment(), paymentModel, store, language);

            modelOrder.setShoppingCartCode(cart.getShoppingCartCode());

            //lookup existing customer
            //if customer exist then do not set authentication for this customer and send an instructions email
            /** **/
            if (!StringUtils.isBlank(customer.getNick()) && !customer.isAnonymous()) {
                if (order.getCustomerId() == null && (customerFacade.checkIfUserExists(customer.getNick(), store))) {
                    customer.setAnonymous(true);
                    customer.setNick(null);
                    //send email instructions
                }
            }


            //order service
            modelOrder = orderService.processOrder(modelOrder, customer, items, orderTotalSummary, paymentModel, store);

            // update cart
            try {
                cart.setOrderId(modelOrder.getId());
                shoppingCartFacade.saveOrUpdateShoppingCart(cart);
            } catch (Exception e) {
                log.error("Cannot delete cart {}", cart.getId(), e);
            }

            //email management
            if ("true".equals(coreConfiguration.getProperty("ORDER_EMAIL_API"))) {
                // send email
                try {

                    notify(modelOrder, customer, store, language, locale);


                } catch (Exception e) {
                    log.error("Cannot send order confirmation email", e);
                }
            }

            return modelOrder;

        } catch (Exception e) {

            throw new ServiceException(e);

        }

    }

    @Override
    public ReadableOrderConfirmation orderConfirmation(Order order, Customer customer, MerchantStore store,
                                                       Language language) {
        Validate.notNull(order, "Order cannot be null");
        Validate.notNull(customer, "Customer cannot be null");
        Validate.notNull(store, "MerchantStore cannot be null");

        ReadableOrderConfirmation orderConfirmation = new ReadableOrderConfirmation();

        ReadableCustomer readableCustomer = readableCustomerMapper.convert(customer, store, language);
        orderConfirmation.setBilling(readableCustomer.getBilling());
        orderConfirmation.setDelivery(readableCustomer.getDelivery());

        ReadableTotal readableTotal = new ReadableTotal();

        Set<OrderTotal> totals = order.getOrderTotal();
        List<ReadableOrderTotal> readableTotals = totals.stream()
                .sorted(Comparator.comparingInt(OrderTotal::getSortOrder))
                .map(tot -> convertOrderTotal(tot, store, language))
                .collect(Collectors.toList());

        readableTotal.setTotals(readableTotals);


        Optional<ReadableOrderTotal> grandTotal = readableTotals.stream().filter(tot -> tot.getCode().equals("order.total.total")).findFirst();


        grandTotal.ifPresent(readableOrderTotal -> readableTotal.setGrandTotal(readableOrderTotal.getText()));
        orderConfirmation.setTotal(readableTotal);


        List<ReadableOrderProduct> products = order.getOrderProducts().stream().map(pr -> convertOrderProduct(pr, store, language)).collect(Collectors.toList());
        orderConfirmation.setProducts(products);

        if (!StringUtils.isBlank(order.getShippingModuleCode())) {
            StringBuilder optionCodeBuilder = new StringBuilder();
            try {

                optionCodeBuilder
                        .append("module.shipping.")
                        .append(order.getShippingModuleCode());
                String shippingName = messages.getMessage(optionCodeBuilder.toString(), new String[]{store.getStorename()}, languageService.toLocale(language, store));
                orderConfirmation.setShipping(shippingName);
            } catch (Exception e) { // label not found
                log.warn("No shipping code found for {}", optionCodeBuilder);
            }
        }

        if (order.getPaymentType() != null) {
            orderConfirmation.setPayment(order.getPaymentType().name());
        }


        /**
         * Confirmation may be formatted
         */
        orderConfirmation.setId(order.getId());


        return orderConfirmation;
    }

    @Override
    public com.asrevo.cvhome.store.core.model.order.v0.ReadableOrderList getReadableOrderList(MerchantStore store, Customer customer, int start, int maxCount, Language language) throws Exception {
        OrderCriteria criteria = new OrderCriteria();
        criteria.setStartIndex(start);
        criteria.setMaxCount(maxCount);
        criteria.setCustomerId(customer.getId());

        return this.getReadableOrderList(criteria, store, language);
    }

    @Override
    public com.asrevo.cvhome.store.core.model.order.v0.ReadableOrderList getReadableOrderList(OrderCriteria criteria, MerchantStore store) {
        try {
            criteria.setLegacyPagination(false);

            OrderList orderList = orderService.getOrders(criteria, store);

            List<Order> orders = orderList.getOrders();
            com.asrevo.cvhome.store.core.model.order.v0.ReadableOrderList returnList = new com.asrevo.cvhome.store.core.model.order.v0.ReadableOrderList();

            if (CollectionUtils.isEmpty(orders)) {
                returnList.setRecordsTotal(0);
                return returnList;
            }

            List<com.asrevo.cvhome.store.core.model.order.v0.ReadableOrder> readableOrders = new ArrayList<>();
            for (Order order : orders) {
                com.asrevo.cvhome.store.core.model.order.v0.ReadableOrder readableOrder = new com.asrevo.cvhome.store.core.model.order.v0.ReadableOrder();
                readableOrderPopulator.populate(order, readableOrder, null, null);
                readableOrders.add(readableOrder);

            }
            returnList.setOrders(readableOrders);

            returnList.setRecordsTotal(orderList.getTotalCount());
            returnList.setTotalPages(orderList.getTotalPages());
            returnList.setNumber(orderList.getOrders().size());
            returnList.setRecordsFiltered(orderList.getOrders().size());

            return returnList;

        } catch (Exception e) {
            throw new ServiceRuntimeException("Error while getting orders", e);
        }
    }
    @Override
    public com.asrevo.cvhome.store.core.model.order.v0.ReadableOrderList getReadableOrderList(MerchantStore store, int start,
                                                                                       int maxCount, Language language) throws Exception {

        OrderCriteria criteria = new OrderCriteria();
        criteria.setStartIndex(start);
        criteria.setMaxCount(maxCount);

        return getReadableOrderList(criteria, store, language);
    }
    private ReadableOrderTotal convertOrderTotal(OrderTotal total, MerchantStore store, Language language) {

        return readableOrderTotalMapper.convert(total, store, language);
    }

    private ReadableOrderProduct convertOrderProduct(OrderProduct product, MerchantStore store, Language language) {

        return readableOrderProductMapper.convert(product, store, language);

    }
    private com.asrevo.cvhome.store.core.model.order.v0.ReadableOrderList getReadableOrderList(OrderCriteria criteria,
                                                                                        MerchantStore store, Language language) throws Exception {

        OrderList orderList = orderService.listByStore(store, criteria);

        // ReadableOrderPopulator orderPopulator = new ReadableOrderPopulator();
        Locale locale = LocaleUtils.getLocale(language);
        readableOrderPopulator.setLocale(locale);

        List<Order> orders = orderList.getOrders();
        com.asrevo.cvhome.store.core.model.order.v0.ReadableOrderList returnList = new com.asrevo.cvhome.store.core.model.order.v0.ReadableOrderList();

        if (CollectionUtils.isEmpty(orders)) {
            returnList.setRecordsTotal(0);
            // returnList.setMessage("No results for store code " + store);
            return null;
        }

        List<com.asrevo.cvhome.store.core.model.order.v0.ReadableOrder> readableOrders = new ArrayList<com.asrevo.cvhome.store.core.model.order.v0.ReadableOrder>();
        for (Order order : orders) {
            com.asrevo.cvhome.store.core.model.order.v0.ReadableOrder readableOrder = new com.asrevo.cvhome.store.core.model.order.v0.ReadableOrder();
            readableOrderPopulator.populate(order, readableOrder, store, language);
            readableOrders.add(readableOrder);

        }

        returnList.setRecordsTotal(orderList.getTotalCount());
        return this.populateOrderList(orderList, store, language);

    }
    private com.asrevo.cvhome.store.core.model.order.v0.ReadableOrderList populateOrderList(final OrderList orderList,
                                                                                     final MerchantStore store, final Language language) {
        List<Order> orders = orderList.getOrders();
        com.asrevo.cvhome.store.core.model.order.v0.ReadableOrderList returnList = new com.asrevo.cvhome.store.core.model.order.v0.ReadableOrderList();
        if (CollectionUtils.isEmpty(orders)) {
            log.info("Order list if empty..Returning empty list");
            returnList.setRecordsTotal(0);
            // returnList.setMessage("No results for store code " + store);
            return returnList;
        }

        // ReadableOrderPopulator orderPopulator = new ReadableOrderPopulator();
        Locale locale = LocaleUtils.getLocale(language);
        readableOrderPopulator.setLocale(locale);

        List<com.asrevo.cvhome.store.core.model.order.v0.ReadableOrder> readableOrders = new ArrayList<com.asrevo.cvhome.store.core.model.order.v0.ReadableOrder>();
        for (Order order : orders) {
            com.asrevo.cvhome.store.core.model.order.v0.ReadableOrder readableOrder = new com.asrevo.cvhome.store.core.model.order.v0.ReadableOrder();
            try {
                readableOrderPopulator.populate(order, readableOrder, store, language);
                setOrderProductList(order, locale, store, language, readableOrder);
            } catch (ConversionException ex) {
                log.error("Error while converting order to order data", ex);

            }
            readableOrders.add(readableOrder);

        }

        returnList.setOrders(readableOrders);
        return returnList;

    }
    private void setOrderProductList(final Order order, final Locale locale, final MerchantStore store,
                                     final Language language, final com.asrevo.cvhome.store.core.model.order.v0.ReadableOrder readableOrder)
            throws ConversionException {
        List<ReadableOrderProduct> orderProducts = new ArrayList<ReadableOrderProduct>();
        for (OrderProduct p : order.getOrderProducts()) {
            ReadableOrderProductPopulator orderProductPopulator = new ReadableOrderProductPopulator();
            orderProductPopulator.setLocale(locale);
            orderProductPopulator.setProductService(productService);
            orderProductPopulator.setPricingService(pricingService);
            orderProductPopulator.setimageUtils(imageUtils);
            ReadableOrderProduct orderProduct = new ReadableOrderProduct();
            orderProductPopulator.populate(p, orderProduct, store, language);

            // image

            // attributes

            orderProducts.add(orderProduct);
        }

        readableOrder.setProducts(orderProducts);
    }

    @Override
    public com.asrevo.cvhome.store.core.model.order.v0.ReadableOrder getReadableOrder(Long orderId, MerchantStore store,
                                                                               Language language) {
        Validate.notNull(store, "MerchantStore cannot be null");
        Order modelOrder = orderService.getOrder(orderId, store);
        if (modelOrder == null) {
            throw new ResourceNotFoundException("Order not found with id " + orderId);
        }

        com.asrevo.cvhome.store.core.model.order.v0.ReadableOrder readableOrder = new com.asrevo.cvhome.store.core.model.order.v0.ReadableOrder();

        Long customerId = modelOrder.getCustomerId();
        if (customerId != null) {
            ReadableCustomer readableCustomer = customerFacade.getCustomerById(customerId, store, language);
            if (readableCustomer == null) {
                log.warn("Customer id " + customerId + " not found in order " + orderId);
            } else {
                readableOrder.setCustomer(readableCustomer);
            }
        }

        try {
            readableOrderPopulator.populate(modelOrder, readableOrder, store, language);

            // order products
            List<ReadableOrderProduct> orderProducts = new ArrayList<ReadableOrderProduct>();
            for (OrderProduct p : modelOrder.getOrderProducts()) {
                ReadableOrderProductPopulator orderProductPopulator = new ReadableOrderProductPopulator();
                orderProductPopulator.setProductService(productService);
                orderProductPopulator.setPricingService(pricingService);
                orderProductPopulator.setimageUtils(imageUtils);

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
    public List<ReadableOrderStatusHistory> getReadableOrderHistory(Long orderId, MerchantStore store,
                                                                    Language language) {

        Order order = orderService.getOrder(orderId, store);
        if (order == null) {
            throw new ResourceNotFoundException(
                    "Order id [" + orderId + "] not found for merchand [" + store.getId() + "]");
        }

        Set<OrderStatusHistory> historyList = order.getOrderHistory();
        List<ReadableOrderStatusHistory> returnList = historyList.stream().map(f -> mapToReadbleOrderStatusHistory(f))
                .collect(Collectors.toList());
        return returnList;
    }

    ReadableOrderStatusHistory mapToReadbleOrderStatusHistory(OrderStatusHistory source) {
        ReadableOrderStatusHistory readable = new ReadableOrderStatusHistory();
        readable.setComments(source.getComments());
        readable.setDate(DateUtil.formatLongDate(source.getDateAdded()));
        readable.setId(source.getId());
        readable.setOrderId(source.getOrder().getId());
        readable.setOrderStatus(source.getStatus().name());

        return readable;
    }
    @Override
    public void createOrderStatus(PersistableOrderStatusHistory status, Long id, MerchantStore store) {
        Validate.notNull(status, "OrderStatusHistory must not be null");
        Validate.notNull(id, "Order id must not be null");
        Validate.notNull(store, "MerchantStore must not be null");

        // retrieve original order
        Order order = orderService.getOrder(id, store);
        if (order == null) {
            throw new ResourceNotFoundException(
                    "Order with id [" + id + "] does not exist for merchant [" + store.getCode() + "]");
        }

        try {
            OrderStatusHistory history = new OrderStatusHistory();
            history.setComments(status.getComments());
            history.setDateAdded(DateUtil.getDate(status.getDate()));
            history.setOrder(order);
            history.setStatus(status.getStatus());

            orderService.addOrderStatusHistory(order, history);

        } catch (Exception e) {
            throw new ServiceRuntimeException("An error occured while converting orderstatushistory", e);
        }

    }

    private void notify(Order modelOrder, Customer customer, MerchantStore store, Language language, Locale locale) {
//@TODO ASHRAF
    }


}
