package com.asrevo.cvhome.billing.processors.command;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.billing.commons.errors.PlanPriceNotFoundException;
import com.asrevo.cvhome.billing.commons.errors.SubscriptionNotFoundException;
import com.asrevo.cvhome.billing.events.command.ApplyPendingPlanChangeCommand;
import com.asrevo.cvhome.billing.service.SubscriptionService;
import com.asrevo.cvhome.commons.event.EventImpl;

import io.namastack.outbox.annotation.OutboxHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Applies one deferred plan change. Idempotent: a subscription with nothing pending is left alone, which is the
 * normal outcome when the provider's webhook already did the work.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApplyPendingPlanChangeCommandImpl implements EventImpl<ApplyPendingPlanChangeCommand> {

    private final SubscriptionService subscriptionService;

    @Override
    @OutboxHandler
    public void process(ApplyPendingPlanChangeCommand command) {
        try {
            subscriptionService.applyPendingChange(command.store());
        } catch (SubscriptionNotFoundException | PlanPriceNotFoundException e) {
            // Neither becomes true later: the store is gone, or it points at a price the catalog no longer has.
            log.warn("Not applying the deferred plan change on store {}: {}", command.store(), e.getMessage());
        }
    }

}
