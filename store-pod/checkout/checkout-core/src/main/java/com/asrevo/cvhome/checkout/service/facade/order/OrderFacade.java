package com.asrevo.cvhome.checkout.service.facade.order;

import java.util.List;
import java.util.Locale;

import com.asrevo.cvhome.checkout.entity.customer.Customer;
import com.asrevo.cvhome.checkout.entity.order.Order;
import com.asrevo.cvhome.checkout.model.order.OrderCriteria;
import com.asrevo.cvhome.checkout.model.order.history.PersistableOrderStatusHistory;
import com.asrevo.cvhome.checkout.model.order.history.ReadableOrderStatusHistory;
import com.asrevo.cvhome.checkout.model.order.v0.ReadableOrder;
import com.asrevo.cvhome.checkout.model.order.v0.ReadableOrderList;
import com.asrevo.cvhome.checkout.model.order.v1.PersistableOrder;
import com.asrevo.cvhome.checkout.model.order.v1.ReadableOrderConfirmation;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;

public interface OrderFacade {

    Order processOrder(PersistableOrder order, Customer customer, StoreMerchantId store, LanguageCode language,
                       Locale locale) throws ServiceException;

    ReadableOrderConfirmation orderConfirmation(Order order, Customer customer, StoreMerchantId store,
                                                LanguageCode language);

    ReadableOrderList getReadableOrderList(OrderCriteria criteria, StoreMerchantId store);

    ReadableOrder getReadableOrder(Long orderId, StoreMerchantId store, LanguageCode language);

    ReadableOrder getReadableOrder(Long orderId, Long customerId, StoreMerchantId store, LanguageCode language);

    List<ReadableOrderStatusHistory> getReadableOrderHistory(Long orderId, StoreMerchantId store,
                                                             LanguageCode language);

    List<ReadableOrderStatusHistory> getReadableOrderHistory(Long orderId, Long customerId, StoreMerchantId store,
                                                             LanguageCode language);

    void createOrderStatus(PersistableOrderStatusHistory status, Long id, StoreMerchantId store);

}
