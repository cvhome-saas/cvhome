package com.asrevo.cvhome.payment.service;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.model.payment.WebhookEvent;

import io.namastack.outbox.annotation.OutboxHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebhookOutboxHandler {

    private final PaymentGatewayService paymentGatewayService;

    @OutboxHandler
    public void handleWebhookEvent(WebhookEvent event) {
        log.info("Processing webhook event from outbox for store {} and type {}", event.storeId(), event.paymentType());
        paymentGatewayService.handleWebhook(
                new StoreMerchantId(event.storeId()),
                event.paymentType(),
                event.payload(),
                event.headers()
        );
    }
}
