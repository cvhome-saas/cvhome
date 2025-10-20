package com.asrevo.cvhome.manager.processors.event;

import com.asrevo.cvhome.commons.event.EventImpl;
import com.asrevo.cvhome.manager.commons.event.store.StoreCreatedEvent;
import com.asrevo.cvhome.manager.service.StoreProvisioningService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class ManagerStoreCreatedEventImpl implements EventImpl<StoreCreatedEvent> {
    private final StoreProvisioningService storeProvisioningService;

    @Override
    public void process(StoreCreatedEvent event) {
        log.info("Received store created event: {}", event);
        storeProvisioningService.provisioning(
                event.orgId(), event.store(), event.podId(), event.request());
    }

    @Override
    public String type() {
        return StoreCreatedEvent.class.getSimpleName();
    }
}
