package com.asrevo.cvhome.billing.processors.command;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.billing.commons.errors.IllegalSubscriptionTransitionException;
import com.asrevo.cvhome.billing.commons.errors.SubscriptionNotFoundException;
import com.asrevo.cvhome.billing.events.command.ExpireTrialCommand;
import com.asrevo.cvhome.billing.service.SubscriptionService;
import com.asrevo.cvhome.commons.event.EventImpl;

import io.namastack.outbox.annotation.OutboxHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Ends a trial that ran out. Idempotent — a redelivery finds a store that is no longer trialling and does nothing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExpireTrialCommandImpl implements EventImpl<ExpireTrialCommand> {

    private final SubscriptionService subscriptionService;

    @Override
    @OutboxHandler
    public void process(ExpireTrialCommand command) {
        try {
            subscriptionService.expireTrial(command.store());
        } catch (SubscriptionNotFoundException | IllegalSubscriptionTransitionException e) {
            // Neither is retryable: the store is gone, or it has already moved past a trial. Rethrowing would have the
            // outbox retry until it gave up, for a record that will never succeed.
            log.warn("Not expiring the trial of store {}: {}", command.store(), e.getMessage());
        }
    }

}
