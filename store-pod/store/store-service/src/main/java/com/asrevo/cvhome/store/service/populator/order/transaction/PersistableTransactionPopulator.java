package com.asrevo.cvhome.store.service.populator.order.transaction;

import com.asrevo.cvhome.store.controller.exception.ConversionException;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.order.Order;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;
import com.asrevo.cvhome.store.core.entity.payments.Transaction;
import com.asrevo.cvhome.store.core.entity.payments.TransactionType;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.order.transaction.PersistableTransaction;
import com.asrevo.cvhome.store.core.services.catalog.pricing.PricingService;
import com.asrevo.cvhome.store.core.services.order.OrderService;
import com.asrevo.cvhome.store.utils.AbstractDataPopulator;
import com.asrevo.cvhome.store.utils.DateUtil;
import org.springframework.util.Assert;

public class PersistableTransactionPopulator extends AbstractDataPopulator<PersistableTransaction, Transaction> {

    private OrderService orderService;
    private PricingService pricingService;

    @Override
    public Transaction populate(PersistableTransaction source, Transaction target, MerchantStore store,
                                Language language) throws ConversionException {

        Assert.notNull(source, "PersistableTransaction must not be null");
        Assert.notNull(orderService, "OrderService must not be null");
        Assert.notNull(pricingService, "OrderService must not be null");

        if (target == null) {
            target = new Transaction();
        }


        try {


            target.setAmount(pricingService.getAmount(source.getAmount()));
            target.setDetails(source.getDetails());
            target.setPaymentType(PaymentType.valueOf(source.getPaymentType()));
            target.setTransactionType(TransactionType.valueOf(source.getTransactionType()));
            target.setTransactionDate(DateUtil.getDate(source.getTransactionDate()));

            if (source.getOrderId() != null && source.getOrderId().longValue() > 0) {
                Order order = orderService.getById(source.getOrderId());
/*				if(source.getCustomerId() == null) {
					throw new ConversionException("Cannot add a transaction for an Order without specyfing the customer");
				}*/

                if (order == null) {
                    throw new ConversionException("Order with id " + source.getOrderId() + "does not exist");
                }
                target.setOrder(order);
            }

            return target;


        } catch (Exception e) {
            throw new ConversionException(e);
        }

    }

    @Override
    protected Transaction createTarget() {
        // TODO Auto-generated method stub
        return null;
    }

    public OrderService getOrderService() {
        return orderService;
    }

    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    public PricingService getPricingService() {
        return pricingService;
    }

    public void setPricingService(PricingService pricingService) {
        this.pricingService = pricingService;
    }

}
