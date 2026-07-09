package com.asrevo.cvhome.payment.api.v1.payment;

import java.util.Map;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.payment.service.PaymentGatewayService;

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

    private final PaymentGatewayService paymentGatewayService;

    @PostMapping("/public/webhook/{internalId}/success")
    @Operation(method = "POST", description = "Webhook success")
    public void successHandler(@PathVariable("internalId") Long internalId,
                               @RequestBody String payload,
                               @RequestHeader Map<String, Object> headers) {
        paymentGatewayService.handleSuccessWebhook(internalId, payload, headers);
    }

    @PostMapping("/public/webhook/{internalId}/fail")
    @Operation(method = "POST", description = "Webhook failure")
    public void failHandler(
            @PathVariable("internalId") Long internalId,
            @RequestBody String payload,
            @RequestHeader Map<String, Object> headers) {
        paymentGatewayService.handleFailedWebhook(internalId, payload, headers);
    }


}
