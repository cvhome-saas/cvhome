package com.asrevo.cvhome.store.service.facade.order;


import com.asrevo.cvhome.store.core.entity.customer.Customer;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.order.Order;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.order.OrderCriteria;
import com.asrevo.cvhome.store.core.model.order.history.PersistableOrderStatusHistory;
import com.asrevo.cvhome.store.core.model.order.history.ReadableOrderStatusHistory;
import com.asrevo.cvhome.store.core.model.order.v1.PersistableOrder;
import com.asrevo.cvhome.store.core.model.order.v1.ReadableOrderConfirmation;

import java.util.List;
import java.util.Locale;

public interface OrderFacade {
    Order processOrder(PersistableOrder order, Customer customer, MerchantStore store, Language language, Locale locale) throws ServiceException;

    ReadableOrderConfirmation orderConfirmation(Order order, Customer customer, MerchantStore store,
                                                Language language);

    com.asrevo.cvhome.store.core.model.order.v0.ReadableOrderList getReadableOrderList(MerchantStore store, Customer customer, int start,
                                                                                       int maxCount, Language language) throws Exception;

    com.asrevo.cvhome.store.core.model.order.v0.ReadableOrderList getReadableOrderList(OrderCriteria criteria, MerchantStore store);

    com.asrevo.cvhome.store.core.model.order.v0.ReadableOrderList getReadableOrderList(MerchantStore store, int start,
                                                                                       int maxCount, Language language) throws Exception;

    com.asrevo.cvhome.store.core.model.order.v0.ReadableOrder getReadableOrder(Long orderId, MerchantStore store,
                                                                               Language language);

    List<ReadableOrderStatusHistory> getReadableOrderHistory(Long orderId, MerchantStore store,
                                                             Language language);

    void createOrderStatus(PersistableOrderStatusHistory status, Long id, MerchantStore store);
}
