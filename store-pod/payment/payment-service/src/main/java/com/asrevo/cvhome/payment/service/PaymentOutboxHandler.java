package com.asrevo.cvhome.payment.service;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.checkout.api.errors.CheckoutApiUnavailableException;
import com.asrevo.cvhome.checkout.model.signal.PaymentSignal;
import com.asrevo.cvhome.checkout.services.order.ExternalOrderSignalService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.errors.UncheckedBaseException;
import com.asrevo.cvhome.payment.model.payment.event.payment.PaymentCanceledEvent;
import com.asrevo.cvhome.payment.model.payment.event.payment.PaymentFailedEvent;
import com.asrevo.cvhome.payment.model.payment.event.payment.PaymentPaidEvent;
import com.asrevo.cvhome.payment.model.payment.event.payment.PaymentRejectedEvent;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;

import io.namastack.outbox.annotation.OutboxHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentOutboxHandler {
    private final ExternalOrderSignalService orderSignals;

    @OutboxHandler
    public void handlePaymentPaidEvent(PaymentPaidEvent event) {
        log.info("processing paid payment event for store {} for requestRef {}", event.storeId(), event.requestRef());
        signal(event.storeId(), event.requestRef(), event.internalRef(), PaymentStatus.PAID);
    }

    @OutboxHandler
    public void handlePaymentFailedEvent(PaymentFailedEvent event) {
        log.info("processing failed payment event for store {} for requestRef {}", event.storeId(), event.requestRef());
        signal(event.storeId(), event.requestRef(), event.internalRef(), PaymentStatus.FAILED);
    }

    @OutboxHandler
    public void handlePaymentCanceledEvent(PaymentCanceledEvent event) {
        log.info("processing canceled payment event for store {} for requestRef {}", event.storeId(), event.requestRef());
        signal(event.storeId(), event.requestRef(), event.internalRef(), PaymentStatus.CANCELLED);
    }

    @OutboxHandler
    public void handlePaymentRejectedEvent(PaymentRejectedEvent event) {
        log.info("processing rejected payment event for store {} for requestRef {}", event.storeId(), event.requestRef());
        signal(event.storeId(), event.requestRef(), event.internalRef(), PaymentStatus.REJECTED);
    }

    /**
     * {@code requestRef} is the order ref checkout gave us; {@code internalRef} is our transaction, which with the
     * status is checkout's dedup key — so a redelivered event is a recorded no-op on their side. An unreachable
     * checkout is rethrown unchecked so the outbox retries the record; that is the whole reason this runs from the
     * outbox and not from the webhook request.
     */
    private void signal(String storeId, String requestRef, String internalRef, PaymentStatus status) {
        try {
            orderSignals.signalPayment(new StoreMerchantId(storeId), requestRef, new PaymentSignal(status, internalRef));
        } catch (CheckoutApiUnavailableException e) {
            throw new UncheckedBaseException(e);
        }
    }
}
