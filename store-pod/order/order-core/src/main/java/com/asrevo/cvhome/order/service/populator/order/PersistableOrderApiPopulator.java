package com.asrevo.cvhome.order.service.populator.order;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.customer.model.customer.PersistableCustomer;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;
import com.asrevo.cvhome.order.entity.customer.Customer;
import com.asrevo.cvhome.order.entity.order.Order;
import com.asrevo.cvhome.order.entity.order.OrderChannel;
import com.asrevo.cvhome.order.entity.order.orderstatus.OrderStatusHistory;
import com.asrevo.cvhome.order.model.order.v1.PersistableAnonymousOrder;
import com.asrevo.cvhome.order.model.order.v1.PersistableOrder;
import com.asrevo.cvhome.order.service.populator.customer.CustomerPopulator;
import com.asrevo.cvhome.order.services.customer.CustomerService;
import com.asrevo.cvhome.order.services.reference.currency.CurrencyService;
import com.asrevo.cvhome.store.core.entity.common.Billing;
import com.asrevo.cvhome.store.core.entity.common.Delivery;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;
import com.asrevo.cvhome.store.core.exception.ConversionException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.populator.AbstractDataPopulator;
import com.asrevo.cvhome.store.utils.LocaleUtils;
import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;

@Component
public class PersistableOrderApiPopulator
        extends AbstractDataPopulator<PersistableOrder, StoreMerchantId, Order> {

    private final CustomerService customerService;
    private final CustomerPopulator customerPopulator;
    private final CurrencyService currencyService;
    private final ExternalMerchantStoreService externalMerchantStoreService;

    public PersistableOrderApiPopulator(
            CustomerService customerService,
            CustomerPopulator customerPopulator,
            CurrencyService currencyService,
            ExternalMerchantStoreService externalMerchantStoreService) {
        this.customerService = customerService;
        this.customerPopulator = customerPopulator;
        this.currencyService = currencyService;
        this.externalMerchantStoreService = externalMerchantStoreService;
    }

    @Override
    public Order populate(
            PersistableOrder source, Order target, StoreMerchantId store, LanguageCode language)
            throws ConversionException {

        Validate.notNull(source.getPayment(), "Payment cannot be null");

        try {

            if (target == null) {
                target = new Order();
            }

            // target.setLocale(LocaleUtils.getLocale(store));

            ReadableMerchantStore baseStore = externalMerchantStoreService.getStore(store);
            target.setLocale(LocaleUtils.getLocale(baseStore.getDefaultLanguage()));

            // Customer
            Customer customer;
            if (source.getCustomerId() != null && source.getCustomerId() > 0) {
                Long customerId = source.getCustomerId();
                customer = customerService.getById(customerId);

                if (customer == null) {
                    throw new ConversionException(
                            "Customer with id " + source.getCustomerId() + " does not exist");
                }
                target.setCustomerId(customerId);

            } else {
                if (source instanceof PersistableAnonymousOrder) {
                    PersistableCustomer persistableCustomer =
                            ((PersistableAnonymousOrder) source).getCustomer();
                    customer = new Customer();
                    customer =
                            customerPopulator.populate(
                                    persistableCustomer, customer, store, language);
                } else {
                    throw new ConversionException("Customer details or id not set in request");
                }
            }

            target.setCustomerEmailAddress(customer.getEmailAddress());

            Delivery delivery = customer.getDelivery();
            target.setDelivery(delivery);

            Billing billing = customer.getBilling();
            target.setBilling(billing);

            target.setDatePurchased(new Date());
            target.setCurrency(baseStore.getCurrency());
            target.setCurrencyValue(new BigDecimal(0));
            target.setStore(store);
            target.setChannel(OrderChannel.API);
            // need this
            target.setStatus(OrderStatus.ORDERED);
            target.setPaymentModuleCode(source.getPayment().getPaymentModule());
            target.setPaymentType(PaymentType.valueOf(source.getPayment().getPaymentType()));

            target.setCustomerAgreement(source.isCustomerAgreement());
            target.setConfirmedAddress(
                    true); // force this to true, cannot perform this activity from the API

            if (!StringUtils.isBlank(source.getComments())) {
                OrderStatusHistory statusHistory = new OrderStatusHistory();
                statusHistory.setStatus(null);
                statusHistory.setOrder(target);
                statusHistory.setComments(source.getComments());
                target.getOrderHistory().add(statusHistory);
            }

            return target;

        } catch (Exception e) {
            throw new ConversionException(e);
        }
    }

    @Override
    protected Order createTarget() {
        // TODO Auto-generated method stub
        return null;
    }
}
