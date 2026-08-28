package com.asrevo.cvhome.tenancy.manager.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.tenancy.commons.dto.AuditEntityType;
import com.asrevo.cvhome.tenancy.commons.dto.ProvisioningState;
import com.asrevo.cvhome.tenancy.manager.entity.ManagerStoreEntity;
import com.asrevo.cvhome.tenancy.manager.repository.ManagerStoreRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Stores that entered provisioning and never left it.
 *
 * <p>
 * A store reaches {@code IN_PROGRESS_PROVISIONING} and stays there if the instance handling it dies between marking
 * it and hearing back from the pod: the outbox record was already consumed, so no retry was ever going to happen and
 * the store sat half-built with no error anywhere.
 * </p>
 *
 * <p>
 * The reaper resets the state rather than calling the pod itself — provisioning is idempotent and knows how to tell
 * a refusal from a timeout, and duplicating that here would be two code paths that have to agree forever. What is
 * pinned below is that a sweep which found nothing writes nothing at all, that a reset lands on
 * {@code NOT_STARTED_PROVISIONING} so the ordinary path picks the store up again, and that the audit row is a
 * {@code JOB} rather than something that looks like a person's action.
 * </p>
 */
class StuckProvisioningReaperTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    private static final ManagerOrgId ORG = new ManagerOrgId("21f023932bc66470c104b76f");

    private static final PodId POD = new PodId("507f1f77bcf86cd799439011");

    private static final Duration STUCK_AFTER = Duration.ofMinutes(15);

    private ManagerStoreRepository repository;

    private TenancyAuditService auditService;

    private StuckProvisioningReaper reaper;

    @BeforeEach
    void setUp() {
        repository = mock(ManagerStoreRepository.class);
        auditService = mock(TenancyAuditService.class);
        reaper = new StuckProvisioningReaper(repository, auditService, STUCK_AFTER);
    }

    private static ManagerStoreEntity stuck() {
        ManagerStoreEntity entity = new ManagerStoreEntity();
        entity.setId(STORE);
        entity.setName("a-store");
        entity.setOrgId(ORG);
        entity.setPodId(POD);
        entity.setCreatedDate(Instant.EPOCH);
        entity.setProvisioningState(ProvisioningState.IN_PROGRESS_PROVISIONING);
        return entity;
    }

    @Test
    void aSweepThatFindsNothingWritesNothing() {
        when(repository.findStuckInProvisioning(any())).thenReturn(List.of());

        reaper.reap();

        verify(repository, never()).save(any());
        verify(auditService, never()).recordJob(any(), any(), any(), any(), any(), any());
    }

    @Test
    void aStrandedStoreIsPutBackInTheQueueRatherThanFailed() {
        ManagerStoreEntity store = stuck();
        when(repository.findStuckInProvisioning(any())).thenReturn(List.of(store));

        reaper.reap();

        assertThat(store.getProvisioningState()).isEqualTo(ProvisioningState.NOT_STARTED_PROVISIONING);
        verify(repository).save(store);
    }

    /** Recorded against the JOB source: nobody asked for this change, and the audit has to say so. */
    @Test
    void theResetIsAuditedAsAJobWithBothStates() {
        when(repository.findStuckInProvisioning(any())).thenReturn(List.of(stuck()));

        reaper.reap();

        verify(auditService).recordJob(eq(AuditEntityType.STORE), eq(STORE), eq("REPROVISION"),
                eq(ProvisioningState.IN_PROGRESS_PROVISIONING), eq(ProvisioningState.NOT_STARTED_PROVISIONING),
                any());
    }

    /**
     * The threshold has to be generous and the cutoff therefore in the past: a pod that is merely slow is not stuck,
     * and resetting a store still being built would have the pod create it twice.
     */
    @Test
    void theCutoffIsTheThresholdBehindNow() {
        when(repository.findStuckInProvisioning(any())).thenReturn(List.of());

        reaper.reap();

        ArgumentCaptor<Instant> cutoff = ArgumentCaptor.captor();
        verify(repository).findStuckInProvisioning(cutoff.capture());
        assertThat(cutoff.getValue()).isBeforeOrEqualTo(Instant.now().minus(STUCK_AFTER));
    }

}
