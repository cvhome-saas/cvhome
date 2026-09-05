package com.asrevo.cvhome.checkout.entity;

import java.math.BigDecimal;
import java.time.Instant;

import com.asrevo.cvhome.checkout.domain.CartCode;
import com.asrevo.cvhome.checkout.domain.OrderRef;
import com.asrevo.cvhome.commons.domain.CurrencyCode;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

/**
 * Orders in the states the tests need, built the way placement builds them.
 */
public final class Orders {

    public static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    public static final Instant T0 = Instant.parse("2026-09-05T10:00:00Z");

    public static final Instant T1 = T0.plusSeconds(60);

    public static final Instant T2 = T0.plusSeconds(120);

    public static final String SKU = "SKU-NK-RUN-001";

    public static final String TX = "tx-1";

    public static final String SUCCESS_URL = "http://shop/en/checkout/success";

    public static final String CANCEL_URL = "http://shop/en/checkout/cancel";

    private static final String FIRST_NAME = "Ada";

    private static final String LAST_NAME = "Lovelace";

    private Orders() {
    }

    public static Customer customer() {
        Customer customer = new Customer(STORE, "sub-1", "shopper@example.com");
        customer.setId(7L);
        customer.setFirstName(FIRST_NAME);
        customer.setLastName(LAST_NAME);
        return customer;
    }

    public static PlacementDraft draft(PaymentType type) {
        AddressSnapshot address = new AddressSnapshot();
        address.setFirstName(FIRST_NAME);
        address.setLastName(LAST_NAME);
        address.setStreetAddress("1 Analytical Way");
        address.setCity("London");
        return new PlacementDraft(STORE, OrderRef.of("11111111-1111-1111-1111-111111111111"), CartCode.of("cart-1"),
                customer(), LanguageCode.defaultLanguage(), new CurrencyCode("USD"), type, address, address, null);
    }

    /** CREATED / PENDING / NOT_REQUESTED, owing RESERVE, one line of 2 × 10.00. */
    public static Order placed(PaymentType type) {
        Order order = Order.place(draft(type), SUCCESS_URL, CANCEL_URL, T0);
        order.setId(100L);
        order.addLine(SKU, 1L, "Runner", new BigDecimal("10.00"), 2, null).addOption("Size", "L");
        order.computeTotals();
        return order;
    }

    /** RESERVED, owing INITIATE_PAYMENT. */
    public static Order reserved(PaymentType type) {
        Order order = placed(type);
        order.reserved(5L, T0.plusSeconds(3600), T0.plusSeconds(1800), T0);
        return order;
    }

    /** PENDING_PAYMENT / PENDING / RESERVED, owing nothing — a card or transfer order waiting on the shopper. */
    public static Order awaitingPayment(PaymentType type) {
        Order order = reserved(type);
        boolean redirects = type == PaymentType.STRIPE || type == PaymentType.PAYPAL;
        order.paymentPending(TX, redirects ? "https://pay/redirect" : null, T1);
        return order;
    }

    /** CONFIRMED / PAID / COMMITTED, owing nothing. */
    public static Order paid(PaymentType type) {
        Order order = awaitingPayment(type);
        order.applyPaymentSignal(com.asrevo.cvhome.store.core.entity.common.PaymentStatus.PAID, TX, T1);
        order.committed(T1);
        return order;
    }

    /** CANCELLED after the payment failed, stock released. */
    public static Order cancelled(PaymentType type) {
        Order order = awaitingPayment(type);
        order.applyPaymentSignal(com.asrevo.cvhome.store.core.entity.common.PaymentStatus.FAILED, TX, T1);
        order.released(T1);
        return order;
    }
}
