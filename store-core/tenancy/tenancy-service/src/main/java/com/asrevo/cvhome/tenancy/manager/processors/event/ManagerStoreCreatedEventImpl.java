package com.asrevo.cvhome.tenancy.manager.processors.event;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.commons.event.EventImpl;
import com.asrevo.cvhome.errors.RemoteServiceTimeoutException;
import com.asrevo.cvhome.errors.RemoteServiceUnavailableException;
import com.asrevo.cvhome.errors.UncheckedBaseException;
import com.asrevo.cvhome.tenancy.errors.StoreNotFoundException;
import com.asrevo.cvhome.tenancy.events.store.StoreCreatedEvent;
import com.asrevo.cvhome.tenancy.manager.service.StoreProvisioningService;

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
        try {
            storeProvisioningService.provisioning(event.orgId(), event.store(), event.podId(), event.request());
        } catch (StoreNotFoundException e) {
            // The store row no longer exists, so there is nothing to build and no retry that would help. Swallowed
            // rather than rethrown, so the outbox record completes instead of retrying against a deleted store.
            log.error("Store {} vanished before it could be provisioned; abandoning", event.store(), e);
        } catch (RemoteServiceUnavailableException | RemoteServiceTimeoutException e) {
            // Rethrown unchecked because the handler signature cannot declare it. The outbox retries, which is
            // right: the pod was unreachable, not unwilling, and the store is still waiting to be built.
            throw new UncheckedBaseException(e);
        }
    }

}
