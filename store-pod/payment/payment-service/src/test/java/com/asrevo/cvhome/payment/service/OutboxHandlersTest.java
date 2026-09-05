package com.asrevo.cvhome.payment.service;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.asrevo.cvhome.checkout.services.order.ExternalOrderService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.model.payment.event.payment.PaymentCanceledEvent;
import com.asrevo.cvhome.payment.model.payment.event.payment.PaymentFailedEvent;
import com.asrevo.cvhome.payment.model.payment.event.payment.PaymentPaidEvent;
import com.asrevo.cvhome.payment.model.payment.event.webhook.WebhookEvent;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * The two outbox consumers: settled payments are pushed to checkout as the matching order payment status, and
 * received webhooks are handed to the gateway with the store restored from the event key.
 */
@ExtendWith(MockitoExtension.class)
class OutboxHandlersTest {

    private static final String STORE_ID = "store-1";

    private static final StoreMerchantId STORE = new StoreMerchantId(STORE_ID);

    private static final String INTERNAL_REF = "tx-1";

    private static final String REQUEST_REF = "order-1";

    private static final String PAYLOAD = "{}";

    private static final Map<String, String> HEADERS = Map.of("stripe-signature", "t=1,v1=abc");

    @Mock
    private ExternalOrderService orders;

    @Mock
    private PaymentGatewayService gateway;

    @Test
    void paidFailedAndCanceledBecomeOrderPaymentStatuses() {
        PaymentOutboxHandler handler = new PaymentOutboxHandler(orders);

        handler.handlePaymentPaidEvent(PaymentPaidEvent.from(INTERNAL_REF, REQUEST_REF, STORE_ID));
        handler.handlePaymentFailedEvent(PaymentFailedEvent.from(INTERNAL_REF, REQUEST_REF, STORE_ID));
        handler.handlePaymentCanceledEvent(PaymentCanceledEvent.from(INTERNAL_REF, REQUEST_REF, STORE_ID));

        verify(orders).updatePaymentStatus(STORE, REQUEST_REF, PaymentStatus.PAID);
        verify(orders).updatePaymentStatus(STORE, REQUEST_REF, PaymentStatus.FAILED);
        verify(orders).updatePaymentStatus(STORE, REQUEST_REF, PaymentStatus.CANCELLED);
        verifyNoMoreInteractions(orders);
    }

    @Test
    void webhookEventsReachTheGatewayWithStoreTypePayloadAndHeaders() {
        new WebhookOutboxHandler(gateway).handleWebhookEvent(new WebhookEvent(STORE_ID, PaymentType.STRIPE, PAYLOAD,
                HEADERS));

        verify(gateway).handleWebhook(STORE, PaymentType.STRIPE, PAYLOAD, HEADERS);
    }

    /**
     * A redelivered payment event must set the same status again rather than doing something different.
     *
     * <p>
     * The outbox guarantees at-least-once, so every one of these handlers <em>will</em> run twice sooner or later.
     * They are idempotent because they assign an absolute status rather than applying a delta — a handler that
     * incremented, appended or toggled would double-count a redelivery, and for a payment that means an order
     * marked paid twice or flipped back out of paid.
     * </p>
     */
    @Test
    void aredeliveredPaymentEventAssignsTheSameStatusRatherThanApplyingAdelta() {
        PaymentOutboxHandler handler = new PaymentOutboxHandler(orders);
        PaymentPaidEvent paid = PaymentPaidEvent.from(INTERNAL_REF, REQUEST_REF, STORE_ID);

        handler.handlePaymentPaidEvent(paid);
        handler.handlePaymentPaidEvent(paid);

        verify(orders, times(2)).updatePaymentStatus(STORE, REQUEST_REF, PaymentStatus.PAID);
        verifyNoMoreInteractions(orders);
    }

    @Test
    void aredeliveredFailureOrCancellationIsEquallyRepeatable() {
        PaymentOutboxHandler handler = new PaymentOutboxHandler(orders);

        handler.handlePaymentFailedEvent(PaymentFailedEvent.from(INTERNAL_REF, REQUEST_REF, STORE_ID));
        handler.handlePaymentFailedEvent(PaymentFailedEvent.from(INTERNAL_REF, REQUEST_REF, STORE_ID));
        handler.handlePaymentCanceledEvent(PaymentCanceledEvent.from(INTERNAL_REF, REQUEST_REF, STORE_ID));
        handler.handlePaymentCanceledEvent(PaymentCanceledEvent.from(INTERNAL_REF, REQUEST_REF, STORE_ID));

        verify(orders, times(2)).updatePaymentStatus(STORE, REQUEST_REF, PaymentStatus.FAILED);
        verify(orders, times(2)).updatePaymentStatus(STORE, REQUEST_REF, PaymentStatus.CANCELLED);
        verifyNoMoreInteractions(orders);
    }

    /**
     * A redelivered webhook is handed to the gateway again with exactly the same arguments. Deduplication is the
     * gateway's — it is the only party that knows a provider's event id — so this handler must not try to be
     * clever about it, and must not mutate the payload on the way through.
     */
    @Test
    void aredeliveredWebhookReachesTheGatewayUnchanged() {
        WebhookOutboxHandler handler = new WebhookOutboxHandler(gateway);
        WebhookEvent event = new WebhookEvent(STORE_ID, PaymentType.STRIPE, PAYLOAD, HEADERS);

        handler.handleWebhookEvent(event);
        handler.handleWebhookEvent(event);

        verify(gateway, times(2)).handleWebhook(STORE, PaymentType.STRIPE, PAYLOAD, HEADERS);
        verifyNoMoreInteractions(gateway);
    }

}
