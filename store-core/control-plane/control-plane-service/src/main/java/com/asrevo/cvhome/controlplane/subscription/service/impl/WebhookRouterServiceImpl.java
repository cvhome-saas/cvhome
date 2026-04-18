package com.asrevo.cvhome.controlplane.subscription.service.impl;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.controlplane.subscription.service.WebhookHandler;
import com.asrevo.cvhome.controlplane.subscription.service.WebhookRouterService;
import com.stripe.model.Event;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class WebhookRouterServiceImpl implements WebhookRouterService {

    private final List<WebhookHandler> handlers;

    private final Set<String> ignoredEventTypes = Set.of("payment_method.attached", "charge.succeeded",
            "customer.subscription.created", "customer.subscription.updated", "checkout.session.completed",
            "payment_intent.succeeded", "payment_intent.created", "invoice.created", "invoice.finalized",
            "invoice.updated");

    @Override
    public void route(Event event) {
        if (ignoredEventTypes.contains(event.getType())) {
            return;
        }
        List<WebhookHandler> currentHandlersImpl = handlers.stream()
                .filter(it -> it.type().equals(event.getType()))
                .toList();

        if (currentHandlersImpl.isEmpty()) {
            log.warn("No handler found for event {}", event.getType());
        }
        currentHandlersImpl.forEach(it -> {
            String handlerName = it.getClass().getSimpleName();
            log.info("Handling event {} using {}", event.getType(), handlerName);
            try {
                it.handle(event);
            } catch (Exception e) {
                log.error("Error handling event {} in {}", event.getType(), handlerName, e);
            }
        });
    }

}
