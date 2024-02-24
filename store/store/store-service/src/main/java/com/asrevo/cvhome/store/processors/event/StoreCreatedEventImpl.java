package com.asrevo.cvhome.store.processors.event;

import com.asrevo.cvhome.commons.event.EventImpl;
import com.asrevo.cvhome.store.commons.event.store.StoreCreatedEvent;
import com.asrevo.cvhome.store.service.OwnerService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class StoreCreatedEventImpl implements EventImpl<StoreCreatedEvent> {
    private final OwnerService ownerService;

    @Override
    public void process(StoreCreatedEvent event) {
        ownerService.addStore(event.storeId(), event.identityId());
    }

    @Override
    public String type() {
        return StoreCreatedEvent.class.getName();
    }
}
