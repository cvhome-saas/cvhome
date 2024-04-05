package com.asrevo.cvhome.store.service.facade.order;


import com.asrevo.cvhome.store.core.entity.customer.Customer;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.order.Order;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.order.v1.PersistableOrder;
import com.asrevo.cvhome.store.core.model.order.v1.ReadableOrderConfirmation;

import java.util.Locale;

public interface OrderFacade {
    Order processOrder(PersistableOrder order, Customer customer, MerchantStore store, Language language, Locale locale) throws ServiceException;

    ReadableOrderConfirmation orderConfirmation(Order order, Customer customer, MerchantStore store,
                                                Language language);
}
