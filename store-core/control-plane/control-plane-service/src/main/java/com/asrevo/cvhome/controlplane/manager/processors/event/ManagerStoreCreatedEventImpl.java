package com.asrevo.cvhome.controlplane.manager.processors.event;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.commons.event.EventImpl;
import com.asrevo.cvhome.controlplane.manager.commons.event.store.StoreCreatedEvent;
import com.asrevo.cvhome.controlplane.manager.service.StoreProvisioningService;

import io.namastack.outbox.annotation.OutboxHandler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class ManagerStoreCreatedEventImpl implements EventImpl<StoreCreatedEvent> {

    private final StoreProvisioningService storeProvisioningService;

    @OutboxHandler
    public void process(StoreCreatedEvent event) {
        log.info("Received store created event from outbox: {}", event);
        storeProvisioningService.provisioning(event.orgId(), event.store(), event.podId(), event.request());
    }

}
