package com.asrevo.cvhome.manager.processors.event;

import com.asrevo.cvhome.commons.event.EventImpl;
import com.asrevo.cvhome.manager.commons.event.store.StoreCreatedEvent;
import org.springframework.stereotype.Service;

@Service
public class ManagerStoreCreatedEventImpl implements EventImpl<StoreCreatedEvent> {

    @Override
    public void process(StoreCreatedEvent event) {}

    @Override
    public String type() {
        return StoreCreatedEvent.class.getName();
    }
}
