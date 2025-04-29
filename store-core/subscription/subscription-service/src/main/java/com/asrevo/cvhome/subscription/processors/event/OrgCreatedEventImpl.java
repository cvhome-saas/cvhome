package com.asrevo.cvhome.subscription.processors.event;

import com.asrevo.cvhome.commons.event.EventImpl;
import com.asrevo.cvhome.manager.commons.event.store.OrgCreatedEvent;
import com.asrevo.cvhome.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrgCreatedEventImpl implements EventImpl<OrgCreatedEvent> {
    private final SubscriptionService subscriptionService;

    @Override
    public void process(OrgCreatedEvent event) {
        subscriptionService.createInitialSubscription(event.org());
    }

    @Override
    public String type() {
        return OrgCreatedEvent.class.getSimpleName();
    }
}
