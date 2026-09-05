package com.asrevo.cvhome.checkout.services.order;

import java.time.Clock;
import java.time.Instant;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.checkout.entity.Order;
import com.asrevo.cvhome.checkout.entity.OrderStatusHistory;
import com.asrevo.cvhome.checkout.errors.IllegalOrderTransitionException;
import com.asrevo.cvhome.checkout.errors.OrderNotFoundException;
import com.asrevo.cvhome.checkout.model.order.PersistableOrderStatusHistory;
import com.asrevo.cvhome.checkout.model.order.ReadableOrderStatusHistory;
import com.asrevo.cvhome.checkout.repositories.OrderRepository;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import lombok.RequiredArgsConstructor;

/**
 * The console's status change, as one transaction, apart from the remote step that may follow it.
 */
@Component
@RequiredArgsConstructor
public class OrderTransitionTransaction {

    private final OrderRepository orders;

    private final Clock clock;

    @Transactional(rollbackFor = Exception.class)
    public ReadableOrderStatusHistory apply(StoreMerchantId store, Long id, PersistableOrderStatusHistory change,
                                            String actor)
            throws OrderNotFoundException, IllegalOrderTransitionException {
        Order order = orders.findByStoreMerchantIdAndId(store, id).orElseThrow(() -> OrderNotFoundException.of(id, store.getId()));
        Instant now = clock.instant();
        order.fulfil(change.getOrderStatus(), change.getComments(), actor, now);
        orders.saveAndFlush(order);
        OrderStatusHistory latest = order.getHistory().getLast();
        return OrderMapper.toHistory(order, latest);
    }
}
