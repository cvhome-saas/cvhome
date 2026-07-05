package com.asrevo.cvhome.controlplane.subscription.processors.command;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.commons.event.EventImpl;
import com.asrevo.cvhome.controlplane.subscription.commons.command.DeActivateNonRenewedSubscriptionCommand;
import com.asrevo.cvhome.controlplane.subscription.service.SubscriptionService;

import io.namastack.outbox.annotation.OutboxHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeActivateNonRenewedSubscriptionCommandImpl implements EventImpl<DeActivateNonRenewedSubscriptionCommand> {

    private final SubscriptionService subscriptionService;

    @OutboxHandler
    public void process(DeActivateNonRenewedSubscriptionCommand command) {
        log.info("Received DeActivateNonRenewedSubscriptionCommand {} from outbox", command);
        subscriptionService.deActivateSubscription(command.org());
    }

}
