package com.asrevo.cvhome.manager.processors.event;

import com.asrevo.cvhome.commons.event.EventImpl;
import com.asrevo.cvhome.manager.commons.event.store.StoreCreatedEvent;
import com.asrevo.cvhome.manager.service.OwnerService;
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
