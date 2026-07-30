package com.asrevo.cvhome.payment.api.v1.payment;

import java.util.Map;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.payment.model.payment.WebhookEvent;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import io.namastack.outbox.Outbox;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/api/v1")
@Tag(name = "Payment Gateway Webhook")
@Slf4j
@AllArgsConstructor
public class PublicPaymentWebhookApi {

    private final Outbox outbox;

    @PostMapping("/public/webhook/{storeId}/{paymentType}")
    @Operation(method = "POST", description = "Payment Webhook")
    public void webhook(@PathVariable("storeId") String storeId,
                        @PathVariable("paymentType") PaymentType paymentType,
                        @RequestBody String payload,
                        @RequestHeader Map<String, String> headers) {
        log.info("Received webhook for store {} and type {}", storeId, paymentType);
        outbox.schedule(
                new WebhookEvent(storeId, paymentType, payload, headers)
        );
    }


}
