package com.asrevo.cvhome.checkout.services.order;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.checkout.domain.ShopperId;
import com.asrevo.cvhome.checkout.errors.IllegalOrderTransitionException;
import com.asrevo.cvhome.checkout.errors.OrderNotFoundException;
import com.asrevo.cvhome.checkout.model.order.OrderFilter;
import com.asrevo.cvhome.checkout.model.order.PersistableOrderStatusHistory;
import com.asrevo.cvhome.checkout.model.order.ReadableOrder;
import com.asrevo.cvhome.checkout.model.order.ReadableOrderConfirmation;
import com.asrevo.cvhome.checkout.model.order.ReadableOrderList;
import com.asrevo.cvhome.checkout.model.order.ReadableOrderStatus;
import com.asrevo.cvhome.checkout.model.order.ReadableOrderStatusHistory;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * Orders after placement: the console's reads and its one write, and the shopper's view of their own orders.
 */
public interface OrderService {

    ReadableOrderList list(StoreMerchantId store, LanguageCode language, OrderFilter filter, Pageable pageable);

    ReadableOrder get(StoreMerchantId store, LanguageCode language, Long id) throws OrderNotFoundException;

    List<ReadableOrderStatusHistory> history(StoreMerchantId store, Long id) throws OrderNotFoundException;

    /** The console moving an order along; {@code actor} is the staff principal for the history row. */
    ReadableOrderStatusHistory transition(StoreMerchantId store, Long id, PersistableOrderStatusHistory change,
                                          String actor) throws OrderNotFoundException, IllegalOrderTransitionException;

    /**
     * The payment-return page's read. Owned by {@code shopper} when one is signed in; anonymous when the store
     * allows guest checkout and none is.
     */
    ReadableOrderStatus status(StoreMerchantId store, Long id, ShopperId shopper) throws OrderNotFoundException;

    ReadableOrderList listForShopper(StoreMerchantId store, LanguageCode language, ShopperId shopper,
                                     Pageable pageable);

    ReadableOrderConfirmation getForShopper(StoreMerchantId store, LanguageCode language, ShopperId shopper, Long id)
            throws OrderNotFoundException;

    List<ReadableOrderStatusHistory> historyForShopper(StoreMerchantId store, ShopperId shopper, Long id)
            throws OrderNotFoundException;
}
