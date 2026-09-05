package com.asrevo.cvhome.checkout.services.order;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.checkout.domain.ShopperId;
import com.asrevo.cvhome.checkout.entity.Customer;
import com.asrevo.cvhome.checkout.entity.Order;
import com.asrevo.cvhome.checkout.errors.IllegalOrderTransitionException;
import com.asrevo.cvhome.checkout.errors.OrderNotFoundException;
import com.asrevo.cvhome.checkout.model.order.OrderFilter;
import com.asrevo.cvhome.checkout.model.order.PersistableOrderStatusHistory;
import com.asrevo.cvhome.checkout.model.order.ReadableOrder;
import com.asrevo.cvhome.checkout.model.order.ReadableOrderConfirmation;
import com.asrevo.cvhome.checkout.model.order.ReadableOrderList;
import com.asrevo.cvhome.checkout.model.order.ReadableOrderStatus;
import com.asrevo.cvhome.checkout.model.order.ReadableOrderStatusHistory;
import com.asrevo.cvhome.checkout.repositories.CustomerRepository;
import com.asrevo.cvhome.checkout.repositories.OrderRepository;
import com.asrevo.cvhome.checkout.repositories.OrderSpecifications;
import com.asrevo.cvhome.checkout.services.customer.CustomerService;
import com.asrevo.cvhome.checkout.services.store.StoreSettings;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.errors.BaseException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "datePurchased", "id");

    private final OrderRepository orders;

    private final CustomerRepository customers;

    private final CustomerService customerService;

    private final StoreSettings storeSettings;

    private final OrderStepRunner steps;

    private final OrderTransitionTransaction transitions;

    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public ReadableOrderList list(StoreMerchantId store, LanguageCode language, OrderFilter filter, Pageable pageable) {
        Pageable sorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                pageable.getSortOr(NEWEST_FIRST));
        Page<Order> page = orders.findAll(OrderSpecifications.orders(store, filter), sorted);
        return OrderMapper.toList(page, order -> OrderMapper.toReadable(order, customerOf(order), false,
                storeSettings.locale(language)));
    }

    @Override
    @Transactional(readOnly = true)
    public ReadableOrder get(StoreMerchantId store, LanguageCode language, Long id) throws OrderNotFoundException {
        Order order = require(store, id);
        return OrderMapper.toReadable(order, customerOf(order), true, storeSettings.locale(language));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReadableOrderStatusHistory> history(StoreMerchantId store, Long id) throws OrderNotFoundException {
        return OrderMapper.toHistory(require(store, id));
    }

    @Override
    public ReadableOrderStatusHistory transition(StoreMerchantId store, Long id, PersistableOrderStatusHistory change,
                                                 String actor)
            throws OrderNotFoundException, IllegalOrderTransitionException {
        ReadableOrderStatusHistory entry = transitions.apply(store, id, change, actor);
        try {
            steps.runUntilSettled(id, 1); // a cancel may owe a RELEASE
        } catch (BaseException e) {
            log.warn("Order {}: pending action after console change left to recovery: {}", id, e.getMessage());
        }
        return entry;
    }

    @Override
    @Transactional(readOnly = true)
    public ReadableOrderStatus status(StoreMerchantId store, Long id, ShopperId shopper) throws OrderNotFoundException {
        Order order = shopper == null ? require(store, id) : requireForShopper(store, shopper, id);
        return OrderMapper.toStatus(order);
    }

    @Override
    @Transactional(readOnly = true)
    public ReadableOrderList listForShopper(StoreMerchantId store, LanguageCode language, ShopperId shopper,
                                            Pageable pageable) {
        Optional<Customer> customer = customerService.find(store, shopper);
        if (customer.isEmpty()) {
            return OrderMapper.toList(Page.empty(pageable), order -> null);
        }
        Page<Order> page = orders.findByStoreMerchantIdAndCustomerIdOrderByDatePurchasedDesc(store,
                customer.get().getId(), pageable);
        return OrderMapper.toList(page, order -> OrderMapper.toReadable(order, customer.get(), true,
                storeSettings.locale(language)));
    }

    @Override
    @Transactional(readOnly = true)
    public ReadableOrderConfirmation getForShopper(StoreMerchantId store, LanguageCode language, ShopperId shopper,
                                                   Long id) throws OrderNotFoundException {
        return OrderMapper.toConfirmation(requireForShopper(store, shopper, id), storeSettings.locale(language));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReadableOrderStatusHistory> historyForShopper(StoreMerchantId store, ShopperId shopper, Long id)
            throws OrderNotFoundException {
        return OrderMapper.toHistory(requireForShopper(store, shopper, id));
    }

    private Order require(StoreMerchantId store, Long id) throws OrderNotFoundException {
        return orders.findByStoreMerchantIdAndId(store, id).orElseThrow(() -> OrderNotFoundException.of(id, store.getId()));
    }

    private Order requireForShopper(StoreMerchantId store, ShopperId shopper, Long id) throws OrderNotFoundException {
        Customer customer = customerService.find(store, shopper)
                .orElseThrow(() -> OrderNotFoundException.forShopper(id, shopper.sub()));
        return orders.findByStoreMerchantIdAndIdAndCustomerId(store, id, customer.getId())
                .orElseThrow(() -> OrderNotFoundException.forShopper(id, shopper.sub()));
    }

    private Customer customerOf(Order order) {
        return customers.findById(order.getCustomerId()).orElse(null);
    }
}
