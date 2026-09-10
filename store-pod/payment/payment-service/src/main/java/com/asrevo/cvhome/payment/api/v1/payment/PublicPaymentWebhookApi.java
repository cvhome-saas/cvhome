package com.asrevo.cvhome.payment.api.v1.payment;

import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.errors.InvalidWebhookSignatureException;
import com.asrevo.cvhome.payment.errors.PaymentConfigurationNotFoundException;
import com.asrevo.cvhome.payment.model.payment.event.webhook.WebhookEvent;
import com.asrevo.cvhome.payment.service.PaymentGatewayService;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import io.namastack.outbox.Outbox;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The provider telling payment what became of a checkout session.
 *
 * <p>
 * Anonymous by design — Stripe holds no token of ours — and under {@code /public/} on purpose: the store's own
 * webhook secret is the credential. The delivery is verified against that store's enabled configuration <em>before</em>
 * it is scheduled on the outbox, so a forged or unsigned body is a 400, an unknown store or an unconfigured type a 404,
 * and only an authentic delivery ever writes a row. Before this, everything was scheduled first and verified on the
 * outbox, which let anyone fill the table for any store id and gave a misconfigured provider a 200 for every failure.
 * </p>
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Payment Gateway Webhook")
@Slf4j
@AllArgsConstructor
public class PublicPaymentWebhookApi {

    private final Outbox outbox;

    private final PaymentGatewayService paymentGatewayService;

    /**
     * @throws PaymentConfigurationNotFoundException the store id is not an id, or the store has nothing enabled for the
     *                                               type — the same 404 either way, so the endpoint confirms nothing
     * @throws InvalidWebhookSignatureException      the signature is missing or does not verify
     */
    @PostMapping("/public/webhook/{storeId}/{paymentType}")
    @Operation(method = "POST", description = "Payment Webhook")
    public void webhook(@PathVariable("storeId") String storeId,
                        @PathVariable("paymentType") PaymentType paymentType,
                        @RequestBody String payload,
                        @RequestHeader Map<String, String> headers)
            throws PaymentConfigurationNotFoundException, InvalidWebhookSignatureException {
        log.info("Received webhook for store {} and type {}", storeId, paymentType);
        if (!ObjectId.isValid(storeId)) {
            throw PaymentConfigurationNotFoundException.of(paymentType, storeId);
        }
        paymentGatewayService.authenticateWebhook(new StoreMerchantId(storeId), paymentType, payload, headers);
        outbox.schedule(
                new WebhookEvent(storeId, paymentType, payload, headers)
        );
    }


}
