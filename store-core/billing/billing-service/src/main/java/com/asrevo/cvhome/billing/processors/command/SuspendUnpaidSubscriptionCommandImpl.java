package com.asrevo.cvhome.billing.processors.command;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.billing.commons.errors.IllegalSubscriptionTransitionException;
import com.asrevo.cvhome.billing.commons.errors.SubscriptionNotFoundException;
import com.asrevo.cvhome.billing.events.command.SuspendUnpaidSubscriptionCommand;
import com.asrevo.cvhome.billing.service.SubscriptionService;
import com.asrevo.cvhome.commons.event.EventImpl;

import io.namastack.outbox.annotation.OutboxHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Suspends a store whose grace window closed. Idempotent, for the same reason as {@link ExpireTrialCommandImpl}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SuspendUnpaidSubscriptionCommandImpl implements EventImpl<SuspendUnpaidSubscriptionCommand> {

    private final SubscriptionService subscriptionService;

    @Override
    @OutboxHandler
    public void process(SuspendUnpaidSubscriptionCommand command) {
        try {
            subscriptionService.suspendUnpaid(command.store());
        } catch (SubscriptionNotFoundException | IllegalSubscriptionTransitionException e) {
            log.warn("Not suspending store {}: {}", command.store(), e.getMessage());
        }
    }

}
