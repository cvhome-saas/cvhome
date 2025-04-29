package com.asrevo.cvhome.subscription.processors.command;

import com.asrevo.cvhome.commons.event.EventImpl;
import com.asrevo.cvhome.subscription.commons.command.DeActivateNonRenewedSubscriptionCommand;
import com.asrevo.cvhome.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeActivateNonRenewedSubscriptionCommandImpl implements EventImpl<DeActivateNonRenewedSubscriptionCommand> {
    private final SubscriptionService subscriptionService;

    @Override
    public void process(DeActivateNonRenewedSubscriptionCommand command) {
        subscriptionService.deActivateSubscription(command.org());
    }

    @Override
    public String type() {
        return DeActivateNonRenewedSubscriptionCommand.class.getSimpleName();
    }
}
