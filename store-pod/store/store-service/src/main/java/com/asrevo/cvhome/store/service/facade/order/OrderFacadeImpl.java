package com.asrevo.cvhome.store.service.facade.order;

import com.asrevo.cvhome.store.controller.exception.ConversionException;
import com.asrevo.cvhome.store.core.entity.customer.Customer;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.order.Order;
import com.asrevo.cvhome.store.core.entity.order.OrderTotal;
import com.asrevo.cvhome.store.core.entity.order.attributes.OrderAttribute;
import com.asrevo.cvhome.store.core.entity.order.orderproduct.OrderProduct;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.entity.shoppingcart.ShoppingCart;
import com.asrevo.cvhome.store.core.entity.shoppingcart.ShoppingCartItem;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.customer.ReadableCustomer;
import com.asrevo.cvhome.store.core.model.order.OrderSummary;
import com.asrevo.cvhome.store.core.model.order.OrderTotalSummary;
import com.asrevo.cvhome.store.core.model.order.ReadableOrderProduct;
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
import com.asrevo.cvhome.store.core.services.shipping.ShippingService;
import com.asrevo.cvhome.store.core.services.shoppingcart.ShoppingCartService;
import com.asrevo.cvhome.store.service.facade.cart.ShoppingCartFacade;
import com.asrevo.cvhome.store.service.facade.customer.CustomerFacade;
import com.asrevo.cvhome.store.service.mapper.customer.ReadableCustomerMapper;
import com.asrevo.cvhome.store.service.mapper.order.ReadableOrderProductMapper;
import com.asrevo.cvhome.store.service.mapper.order.ReadableOrderTotalMapper;
import com.asrevo.cvhome.store.service.populator.order.OrderProductPopulator;
import com.asrevo.cvhome.store.service.populator.order.PersistableOrderApiPopulator;
import com.asrevo.cvhome.store.service.populator.order.transaction.PersistablePaymentPopulator;
import com.asrevo.cvhome.store.utils.CoreConfiguration;
import com.asrevo.cvhome.store.utils.LabelUtils;
import com.asrevo.cvhome.store.utils.ProductPriceUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service("orderFacade")
public class OrderFacadeImpl implements OrderFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderFacadeImpl.class);

    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private ShoppingCartFacade shoppingCartFacade;

    @Autowired
    private ShippingService shippingService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private ShippingQuoteService shippingQuoteService;
    @Autowired
    private ProductAttributeService productAttributeService;
    @Autowired
    private ProductService productService;
    @Autowired
    private PersistableOrderApiPopulator persistableOrderApiPopulator;
    @Autowired
    private ProductPriceUtils productPriceUtils;
    @Autowired
    private PricingService pricingService;
    @Autowired
    private CustomerFacade customerFacade;
    @Autowired
    private CoreConfiguration coreConfiguration;
    @Autowired
    private LabelUtils messages;
    @Autowired
    private ReadableOrderTotalMapper readableOrderTotalMapper;
    @Autowired
    private LanguageService languageService;
    @Autowired
    private ReadableCustomerMapper readableCustomerMapper;
    @Autowired
    private ReadableOrderProductMapper readableOrderProductMapper;

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

            if (order.getAttributes() != null && order.getAttributes().size() > 0) {
                Set<OrderAttribute> attrs = new HashSet<OrderAttribute>();
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
            List<ShoppingCartItem> itemsSet = new ArrayList<ShoppingCartItem>(cart.getLineItems());
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
                LOGGER.error("Cannot delete cart " + cart.getId(), e);
            }

            //email management
            if ("true".equals(coreConfiguration.getProperty("ORDER_EMAIL_API"))) {
                // send email
                try {

                    notify(modelOrder, customer, store, language, locale);


                } catch (Exception e) {
                    LOGGER.error("Cannot send order confirmation email", e);
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


        if (grandTotal.isPresent()) {
            readableTotal.setGrandTotal(grandTotal.get().getText());
        }
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
                LOGGER.warn("No shipping code found for " + optionCodeBuilder.toString());
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

    private ReadableOrderTotal convertOrderTotal(OrderTotal total, MerchantStore store, Language language) {

        return readableOrderTotalMapper.convert(total, store, language);
    }

    private ReadableOrderProduct convertOrderProduct(OrderProduct product, MerchantStore store, Language language) {

        return readableOrderProductMapper.convert(product, store, language);

    }

    private void notify(Order modelOrder, Customer customer, MerchantStore store, Language language, Locale locale) {
//@TODO ASHRAF
    }


}
