package com.asrevo.cvhome.payment.service;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.checkout.services.order.ExternalOrderService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.model.payment.event.payment.PaymentCanceledEvent;
import com.asrevo.cvhome.payment.model.payment.event.payment.PaymentFailedEvent;
import com.asrevo.cvhome.payment.model.payment.event.payment.PaymentPaidEvent;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;

import io.namastack.outbox.annotation.OutboxHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentOutboxHandler {
    private final ExternalOrderService externalOrderService;

    @OutboxHandler
    public void handlePaymentPaidEvent(PaymentPaidEvent event) {
        log.info("processing paid payment event for store {} for requestRef {}", event.storeId(), event.requestRef());
        externalOrderService.updatePaymentStatus(new StoreMerchantId(event.storeId()), event.requestRef(), PaymentStatus.PAID);
    }

    @OutboxHandler
    public void handlePaymentFailedEvent(PaymentFailedEvent event) {
        log.info("processing failed payment event for store {} for requestRef {}", event.storeId(), event.requestRef());
        externalOrderService.updatePaymentStatus(new StoreMerchantId(event.storeId()), event.requestRef(), PaymentStatus.FAILED);
    }

    @OutboxHandler
    public void handlePaymentCanceledEvent(PaymentCanceledEvent event) {
        log.info("processing canceled payment event for store {} for requestRef {}", event.storeId(), event.requestRef());
        externalOrderService.updatePaymentStatus(new StoreMerchantId(event.storeId()), event.requestRef(), PaymentStatus.CANCELLED);
    }
}
