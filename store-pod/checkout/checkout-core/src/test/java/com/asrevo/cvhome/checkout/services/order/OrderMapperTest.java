package com.asrevo.cvhome.checkout.services.order;

import java.util.Locale;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.checkout.entity.Order;
import com.asrevo.cvhome.checkout.entity.Orders;
import com.asrevo.cvhome.checkout.model.order.ReadableOrder;
import com.asrevo.cvhome.checkout.model.order.ReadableOrderConfirmation;
import com.asrevo.cvhome.checkout.model.order.ReadableOrderProduct;
import com.asrevo.cvhome.checkout.model.order.ReadableOrderStatusHistory;
import com.asrevo.cvhome.store.core.entity.common.InventoryStatus;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The wire shapes both frontends read, field by field, with money formatted in the store currency.
 */
class OrderMapperTest {

    private static final String HTTPS_PAY_REDIRECT = "https://pay/redirect";

    private static final String LIT_20_00 = "$20.00";

    private static final String LIT_20_00_2 = "20.00";

    private static final String TOTAL_2 = "TOTAL";

    private static final String LOOK = "look";

    @Test
    void theConsoleShapeCarriesEveryStatusTheTotalsBlockAndFormattedLines() {
        Order order = Orders.paid(PaymentType.STRIPE);
        order.setNeedsAttention(true);
        order.setAttentionReason(LOOK);

        ReadableOrder readable = OrderMapper.toReadable(order, Orders.customer(), true, Locale.US);

        assertThat(readable.getId()).isEqualTo(100L);
        assertThat(readable.getOrderRef()).isEqualTo("11111111-1111-1111-1111-111111111111");
        assertThat(readable.getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(readable.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(readable.getInventoryStatus()).isEqualTo(InventoryStatus.COMMITTED);
        assertThat(readable.getPaymentType()).isEqualTo(PaymentType.STRIPE);
        assertThat(readable.getCurrency().code()).isEqualTo("USD");
        assertThat(readable.getDatePurchased()).isEqualTo(Orders.T0);
        assertThat(readable.isNeedsAttention()).isTrue();
        assertThat(readable.getAttentionReason()).isEqualTo(LOOK);
        assertThat(readable.getTotals()).extracting("code", "total").containsExactly(
                org.assertj.core.groups.Tuple.tuple("SUBTOTAL", LIT_20_00),
                org.assertj.core.groups.Tuple.tuple(TOTAL_2, LIT_20_00));
        assertThat(readable.getTotal().getCode()).isEqualTo(TOTAL_2);
        assertThat(readable.getTotal().getValue()).isEqualByComparingTo(LIT_20_00_2);
        assertThat(readable.getBilling().getFirstName()).isEqualTo("Ada");
        assertThat(readable.getBilling().getEmail()).isEqualTo("shopper@example.com");
        assertThat(readable.getDelivery().getCity()).isEqualTo("London");
        assertThat(readable.getCustomer().getId()).isEqualTo(7L);
        ReadableOrderProduct line = readable.getProducts().getFirst();
        assertThat(line.getSku()).isEqualTo(Orders.SKU);
        assertThat(line.getProductName()).isEqualTo("Runner");
        assertThat(line.getOrderedQuantity()).isEqualTo(2);
        assertThat(line.getPrice()).isEqualTo("$10.00");
        assertThat(line.getSubTotal()).isEqualTo(LIT_20_00);
        assertThat(line.getAttributes()).singleElement().satisfies(attribute -> {
            assertThat(attribute.getAttributeName()).isEqualTo("Size");
            assertThat(attribute.getAttributeValue()).isEqualTo("L");
        });
    }

    @Test
    void theListShapeOmitsLinesAndCustomer() {
        ReadableOrder readable = OrderMapper.toReadable(Orders.paid(PaymentType.STRIPE), null, false, Locale.US);

        assertThat(readable.getProducts()).isNull();
        assertThat(readable.getCustomer()).isNull();
        assertThat(readable.getTotals()).hasSize(2);
    }

    @Test
    void aLineWithoutOptionsHasNullAttributesAsTheFrontendsExpect() {
        Order order = Orders.placed(PaymentType.STRIPE);
        order.addLine("SKU-PLAIN", 2L, "Plain", new java.math.BigDecimal("1.00"), 1, null);
        order.computeTotals();

        ReadableOrder readable = OrderMapper.toReadable(order, null, true, Locale.US);

        assertThat(readable.getProducts().get(1).getAttributes()).isNull();
    }

    @Test
    void theConfirmationIsTheStorefrontsOrderShape() {
        Order order = Orders.awaitingPayment(PaymentType.STRIPE);

        ReadableOrderConfirmation confirmation = OrderMapper.toConfirmation(order, Locale.GERMANY);

        assertThat(confirmation.getId()).isEqualTo(100L);
        assertThat(confirmation.getPayment()).isEqualTo(PaymentType.STRIPE);
        assertThat(confirmation.getOrderStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(confirmation.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(confirmation.getRedirectUrl()).isEqualTo(HTTPS_PAY_REDIRECT);
        assertThat(confirmation.getDatePurchased()).isEqualTo(Orders.T0);
        assertThat(confirmation.getBilling().getAddress()).isEqualTo("1 Analytical Way");
        assertThat(confirmation.getTotal().getValue()).isEqualByComparingTo(LIT_20_00_2);
        assertThat(confirmation.getTotal().getGrandTotal()).contains("20,00");
        assertThat(confirmation.getTotal().getTotals()).hasSize(2);
        assertThat(confirmation.getProducts()).hasSize(1);
    }

    @Test
    void theStatusReadHidesTheRedirectOnceTheOrderStoppedWaiting() {
        assertThat(OrderMapper.toStatus(Orders.awaitingPayment(PaymentType.STRIPE)).getRedirectUrl())
                .isEqualTo(HTTPS_PAY_REDIRECT);
        assertThat(OrderMapper.toStatus(Orders.paid(PaymentType.STRIPE)).getRedirectUrl()).isNull();
        assertThat(OrderMapper.toStatus(Orders.cancelled(PaymentType.STRIPE)).getPaymentStatus())
                .isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    void theHistoryIsTheStatusTrailInOrder() {
        Order order = Orders.paid(PaymentType.STRIPE);

        var history = OrderMapper.toHistory(order);

        assertThat(history).extracting(ReadableOrderStatusHistory::getOrderStatus)
                .containsExactly(OrderStatus.CREATED, OrderStatus.PENDING_PAYMENT, OrderStatus.CONFIRMED);
        assertThat(history).allSatisfy(entry -> {
            assertThat(entry.getOrderId()).isEqualTo(100L);
            assertThat(entry.getDate()).isNotNull();
        });
    }
}
