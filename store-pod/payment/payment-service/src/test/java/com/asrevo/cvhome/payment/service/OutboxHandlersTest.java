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

}
