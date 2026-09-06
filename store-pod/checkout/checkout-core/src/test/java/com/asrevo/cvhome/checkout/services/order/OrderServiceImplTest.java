package com.asrevo.cvhome.checkout.services.order;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.asrevo.cvhome.checkout.domain.ShopperId;
import com.asrevo.cvhome.checkout.entity.Order;
import com.asrevo.cvhome.checkout.entity.Orders;
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
import com.asrevo.cvhome.checkout.services.customer.CustomerService;
import com.asrevo.cvhome.checkout.services.store.StoreSettings;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.errors.remote.RemoteErrorContext;
import com.asrevo.cvhome.inventory.api.errors.InventoryApiUnavailableException;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Reads are store-scoped and, for a shopper, owner-scoped — someone else's order is a 404, never a 403.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    private static final String ADMIN_STORE = "admin@store";

    private static final String PACKING = "packing";

    private static final String ADMIN = "admin";

    private static final ShopperId SHOPPER = new ShopperId("sub-1");

    private static final LanguageCode EN = LanguageCode.defaultLanguage();

    private static final Pageable PAGE = PageRequest.of(0, 20);

    @Mock
    private OrderRepository orders;

    @Mock
    private CustomerRepository customers;

    @Mock
    private CustomerService customerService;

    @Mock
    private StoreSettings storeSettings;

    @Mock
    private OrderStepRunner steps;

    private OrderServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new OrderServiceImpl(orders, customers, customerService, storeSettings, steps,
                new OrderTransitionTransaction(orders, Clock.fixed(Orders.T2, ZoneOffset.UTC)),
                Clock.fixed(Orders.T2, ZoneOffset.UTC));
        lenient().when(storeSettings.locale(any())).thenReturn(Locale.US);
    }

    @Test
    void theConsoleListIsStoreScopedNewestFirstAndCarriesNoLinesOrCustomer() {
        Order order = Orders.paid(PaymentType.STRIPE);
        when(orders.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(order), PAGE, 1));
        when(customers.findById(7L)).thenReturn(Optional.of(Orders.customer()));

        ReadableOrderList list = service.list(Orders.STORE, EN, OrderFilter.none(), PAGE);

        assertThat(list.getTotalElements()).isEqualTo(1);
        assertThat(list.getContent()).singleElement().satisfies(readable -> {
            assertThat(readable.getProducts()).isNull();
            assertThat(readable.getCustomer()).isNull();
            assertThat(readable.getTotal().getTotal()).isEqualTo("$20.00");
        });
    }

    @Test
    void theConsoleDetailCarriesLinesAndCustomer() throws Exception {
        Order order = Orders.paid(PaymentType.STRIPE);
        when(orders.findByStoreMerchantIdAndId(Orders.STORE, 100L)).thenReturn(Optional.of(order));
        when(customers.findById(7L)).thenReturn(Optional.of(Orders.customer()));

        ReadableOrder readable = service.get(Orders.STORE, EN, 100L);

        assertThat(readable.getProducts()).hasSize(1);
        assertThat(readable.getCustomer().getEmailAddress()).isEqualTo("shopper@example.com");
        assertThat(service.history(Orders.STORE, 100L)).hasSize(order.getHistory().size());
    }

    @Test
    void aMissingOrDeletedCustomerDoesNotBreakTheDetail() throws Exception {
        when(orders.findByStoreMerchantIdAndId(Orders.STORE, 100L))
                .thenReturn(Optional.of(Orders.paid(PaymentType.STRIPE)));
        when(customers.findById(7L)).thenReturn(Optional.empty());

        assertThat(service.get(Orders.STORE, EN, 100L).getCustomer()).isNull();
    }

    @Test
    void anotherStoresOrderIs404() {
        when(orders.findByStoreMerchantIdAndId(Orders.STORE, 100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(Orders.STORE, EN, 100L)).isInstanceOf(OrderNotFoundException.class);
        assertThatThrownBy(() -> service.history(Orders.STORE, 100L)).isInstanceOf(OrderNotFoundException.class);
        assertThatThrownBy(() -> service.status(Orders.STORE, 100L, null)).isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void aConsoleTransitionMovesTheOrderAndRunsAnyStepItLeft() throws Exception {
        Order order = Orders.paid(PaymentType.STRIPE);
        when(orders.findByStoreMerchantIdAndId(Orders.STORE, 100L)).thenReturn(Optional.of(order));
        PersistableOrderStatusHistory change = new PersistableOrderStatusHistory();
        change.setOrderStatus(OrderStatus.PROCESSING);
        change.setComments(PACKING);

        ReadableOrderStatusHistory entry = service.transition(Orders.STORE, 100L, change, ADMIN_STORE);

        assertThat(entry.getOrderStatus()).isEqualTo(OrderStatus.PROCESSING);
        assertThat(entry.getComments()).isEqualTo(PACKING);
        assertThat(entry.getOrderId()).isEqualTo(100L);
        assertThat(order.getHistory().getLast().getActor()).isEqualTo(ADMIN_STORE);
        verify(orders).saveAndFlush(order);
        verify(steps).runUntilSettled(100L, 1);
    }

    @Test
    void anIllegalConsoleTransitionIs409AndRunsNothing() throws Exception {
        when(orders.findByStoreMerchantIdAndId(Orders.STORE, 100L))
                .thenReturn(Optional.of(Orders.cancelled(PaymentType.STRIPE)));
        PersistableOrderStatusHistory change = new PersistableOrderStatusHistory();
        change.setOrderStatus(OrderStatus.SHIPPED);

        assertThatThrownBy(() -> service.transition(Orders.STORE, 100L, change, ADMIN))
                .isInstanceOf(IllegalOrderTransitionException.class);
        verify(steps, never()).runUntilSettled(any(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void aStepFailingAfterAConsoleChangeIsLeftToRecovery() throws Exception {
        when(orders.findByStoreMerchantIdAndId(Orders.STORE, 100L))
                .thenReturn(Optional.of(Orders.awaitingPayment(PaymentType.MANUAL_TRANSFER)));
        when(steps.runUntilSettled(100L, 1)).thenThrow(InventoryApiUnavailableException.from(new RemoteErrorContext(
                null, null, java.util.Map.of(), java.util.List.of(), "inventory", 0, null, new RuntimeException())));
        PersistableOrderStatusHistory change = new PersistableOrderStatusHistory();
        change.setOrderStatus(OrderStatus.CANCELLED);

        assertThat(service.transition(Orders.STORE, 100L, change, ADMIN).getOrderStatus())
                .isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void theStatusReadShowsTheRedirectOnlyWhileWaitingForPayment() throws Exception {
        when(orders.findByStoreMerchantIdAndId(Orders.STORE, 100L))
                .thenReturn(Optional.of(Orders.awaitingPayment(PaymentType.STRIPE)));

        ReadableOrderStatus pending = service.status(Orders.STORE, 100L, null);
        assertThat(pending.getRedirectUrl()).isEqualTo("https://pay/redirect");
        assertThat(pending.getOrderId()).isEqualTo(100L);

        when(orders.findByStoreMerchantIdAndId(Orders.STORE, 100L)).thenReturn(Optional.of(Orders.paid(PaymentType.STRIPE)));
        assertThat(service.status(Orders.STORE, 100L, null).getRedirectUrl()).isNull();
    }

    @Test
    void aShopperOnlySeesTheirOwnOrders() throws Exception {
        Order order = Orders.paid(PaymentType.STRIPE);
        when(customerService.find(Orders.STORE, SHOPPER)).thenReturn(Optional.of(Orders.customer()));
        when(orders.findByStoreMerchantIdAndIdAndCustomerId(Orders.STORE, 100L, 7L)).thenReturn(Optional.of(order));
        when(orders.findByStoreMerchantIdAndIdAndCustomerId(Orders.STORE, 101L, 7L)).thenReturn(Optional.empty());
        when(orders.findByStoreMerchantIdAndCustomerIdOrderByDatePurchasedDesc(Orders.STORE, 7L, PAGE))
                .thenReturn(new PageImpl<>(List.of(order), PAGE, 1));

        ReadableOrderConfirmation own = service.getForShopper(Orders.STORE, EN, SHOPPER, 100L);
        assertThat(own.getId()).isEqualTo(100L);
        assertThat(service.historyForShopper(Orders.STORE, SHOPPER, 100L)).isNotEmpty();
        assertThat(service.status(Orders.STORE, 100L, SHOPPER).getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(service.listForShopper(Orders.STORE, EN, SHOPPER, PAGE).getContent()).singleElement()
                .satisfies(readable -> assertThat(readable.getProducts()).hasSize(1));

        assertThatThrownBy(() -> service.getForShopper(Orders.STORE, EN, SHOPPER, 101L))
                .isInstanceOf(OrderNotFoundException.class);
        assertThatThrownBy(() -> service.status(Orders.STORE, 101L, SHOPPER)).isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void aShopperWithNoCustomerRowHasNoOrders() {
        when(customerService.find(Orders.STORE, SHOPPER)).thenReturn(Optional.empty());

        ReadableOrderList list = service.listForShopper(Orders.STORE, EN, SHOPPER, PAGE);

        assertThat(list.getContent()).isEmpty();
        assertThat(list.getTotalElements()).isZero();
        assertThatThrownBy(() -> service.getForShopper(Orders.STORE, EN, SHOPPER, 100L))
                .isInstanceOf(OrderNotFoundException.class);
        verify(orders, never()).findByStoreMerchantIdAndIdAndCustomerId(any(), any(), any());
    }

    @Test
    void theConsoleListKeepsAnExplicitSort() {
        Pageable sorted = PageRequest.of(1, 5, org.springframework.data.domain.Sort.by("id"));
        when(orders.findAll(any(Specification.class), eq(sorted))).thenReturn(Page.empty(sorted));

        ReadableOrderList list = service.list(Orders.STORE, EN, OrderFilter.none(), sorted);

        assertThat(list.getPageNumber()).isEqualTo(1);
        assertThat(list.getContent()).isEmpty();
    }
}
