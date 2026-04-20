package com.asrevo.cvhome.controlplane.subscription.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.controlplane.subscription.service.WebhookRouterService;
import com.asrevo.cvhome.s2s.model.StripeProperties;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("api/v1/stripe-webhook")
@Slf4j
@AllArgsConstructor
public class StripeWebhookController {

    public static final String STRIPE_SIGNATURE_HEADER = "Stripe-Signature";

    private final WebhookRouterService webhookRouterService;

    private final StripeProperties stripeProperties;

    // http://localhost:7002/api/v1/stripe-webhook/public/events
    // https://gateway.com/control-plane/api/v1/stripe-webhook/public/events
    @PostMapping("public/events")
    public void events(@RequestBody String eventStr, @RequestHeader(STRIPE_SIGNATURE_HEADER) String sigHeader) {
        try {
            Event event = Webhook.constructEvent(eventStr, sigHeader, stripeProperties.webhookSigningKey());
            webhookRouterService.route(event);
        } catch (SignatureVerificationException e) {
            log.error(e.getMessage(), e);
        }
    }

}
