package com.asrevo.cvhome.payment.service.webhook;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.entity.payment.PaymentConfiguration;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class StripeWebhook implements PaymentWebhook {

    private static Event getEvent(String payload, Map<String, Object> headers, PaymentConfiguration configuration)
            throws SignatureVerificationException {
        String sigHeader = headers.get("Stripe-Signature").toString();
        return Webhook.constructEvent(payload, sigHeader, configuration.getWebhookSecret());
    }

    @Override
    public void handleFailedWebhook(Long transactionId, StoreMerchantId store, String payload, Map<String, Object> headers,
                                    PaymentConfiguration configuration) {
        log.info("Handling Stripe webhook for store {}", store);

        try {
            Event event = getEvent(payload, headers, configuration);

            if ("checkout.session.completed".equals(event.getType())) {
                Session paymentIntent = (Session) event.getDataObjectDeserializer().getObject().orElseThrow();
                String transactionIdStr = paymentIntent.getClientReferenceId();
                log.info("Processing completed session for transaction ID: {}", transactionIdStr);
            }
        } catch (Exception e) {
            log.error("Error processing Stripe webhook for store {}", store, e);
        }
    }

    @Override
    public void handleSuccessWebhook(Long transactionId, StoreMerchantId store, String payload, Map<String, Object> headers,
                                     PaymentConfiguration configuration) {
        try {
            Event event = getEvent(payload, headers, configuration);

            if ("checkout.session.completed".equals(event.getType())) {
                Session paymentIntent = (Session) event.getDataObjectDeserializer().getObject().orElseThrow();
                String transactionIdStr = paymentIntent.getClientReferenceId();
                log.info("Processing completed session for transaction ID: {}", transactionIdStr);
            }
        } catch (Exception e) {
            log.error("Error processing Stripe webhook for store {}", store, e);
        }

    }
}
