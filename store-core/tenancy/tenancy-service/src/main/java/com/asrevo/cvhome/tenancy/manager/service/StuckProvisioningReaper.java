package com.asrevo.cvhome.tenancy.manager.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.asrevo.cvhome.tenancy.commons.dto.AuditEntityType;
import com.asrevo.cvhome.tenancy.commons.dto.ProvisioningState;
import com.asrevo.cvhome.tenancy.manager.entity.ManagerStoreEntity;
import com.asrevo.cvhome.tenancy.manager.repository.ManagerStoreRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Finds stores stranded mid-provisioning and puts them back in the queue.
 *
 * <p>
 * A store reaches {@code IN_PROGRESS_PROVISIONING} and stays there if the instance handling it dies between
 * marking it and hearing back from the pod. Nothing noticed: the outbox record was already consumed, so no retry
 * was ever going to happen, and the store simply sat half-built with no error anywhere. That is the failure this
 * exists for.
 * </p>
 *
 * <p>
 * It resets the state rather than calling the pod itself. Provisioning belongs to
 * {@link StoreProvisioningService}, which is idempotent and knows how to tell a refusal from a timeout;
 * duplicating that here would mean two code paths that have to agree forever. Moving the store back to
 * {@code NOT_STARTED_PROVISIONING} makes it eligible for the ordinary path again.
 * </p>
 */
@Service
@Slf4j
public class StuckProvisioningReaper {

    private final ManagerStoreRepository storeRepository;

    private final TenancyAuditService auditService;

    private final Duration stuckAfter;

    public StuckProvisioningReaper(ManagerStoreRepository storeRepository, TenancyAuditService auditService,
                                   @Value("${com.asrevo.cvhome.tenancy.provisioning.stuck-after:PT15M}")
                                   Duration stuckAfter) {
        this.storeRepository = storeRepository;
        this.auditService = auditService;
        this.stuckAfter = stuckAfter;
    }

    /**
     * The threshold has to be generous. A pod that is merely slow is not stuck, and resetting a store that is
     * still being built would have the pod create it twice — which is only survivable because provisioning checks
     * its own state first.
     */
    @Scheduled(fixedRateString = "${com.asrevo.cvhome.tenancy.provisioning.reap-rate:PT5M}")
    public void reap() {
        Instant cutoff = Instant.now().minus(stuckAfter);
        List<ManagerStoreEntity> stuck = storeRepository.findStuckInProvisioning(cutoff);
        if (stuck.isEmpty()) {
            return;
        }
        log.warn("Found {} store(s) stuck in provisioning since before {}; resetting them to be retried",
                stuck.size(), cutoff);
        for (ManagerStoreEntity store : stuck) {
            store.setProvisioningState(ProvisioningState.NOT_STARTED_PROVISIONING);
            storeRepository.save(store);
            auditService.recordJob(AuditEntityType.STORE, store.getId(), "REPROVISION",
                    ProvisioningState.IN_PROGRESS_PROVISIONING, ProvisioningState.NOT_STARTED_PROVISIONING,
                    "stranded mid-provisioning and reset by the reaper");
        }
    }

}
