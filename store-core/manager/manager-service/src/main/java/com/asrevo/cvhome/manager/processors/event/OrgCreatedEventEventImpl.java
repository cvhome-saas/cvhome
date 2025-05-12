package com.asrevo.cvhome.manager.processors.event;

import com.asrevo.cvhome.commons.event.EventImpl;
import com.asrevo.cvhome.manager.commons.event.store.OrgCreatedEvent;
import com.asrevo.cvhome.subscription.api.SubscriptionHttpEventsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class OrgCreatedEventEventImpl implements EventImpl<OrgCreatedEvent> {
    private final SubscriptionHttpEventsService subscriptionHttpEventsService;

    @Override
    public void process(OrgCreatedEvent event) {
        log.info("Received org created event: {}", event);
        subscriptionHttpEventsService.on(event).subscribe();
    }

    @Override
    public String type() {
        return OrgCreatedEvent.class.getSimpleName();
    }
}
