package com.asrevo.cvhome.tenancy.manager.service;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.asrevo.cvhome.commons.domain.Email;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.tenancy.commons.dto.AuditEntityType;
import com.asrevo.cvhome.tenancy.commons.dto.ManagerOrgDto;
import com.asrevo.cvhome.tenancy.commons.dto.OrgStatus;
import com.asrevo.cvhome.tenancy.errors.IllegalLifecycleTransitionException;
import com.asrevo.cvhome.tenancy.errors.OrgNotFoundException;
import com.asrevo.cvhome.tenancy.manager.entity.ManagerOrgEntity;
import com.asrevo.cvhome.tenancy.manager.mappers.ManagerOrgMappers;
import com.asrevo.cvhome.tenancy.manager.repository.ManagerOrgRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Organization lifecycle transitions.
 *
 * <p>
 * The legal moves are a table rather than scattered {@code if}s, and these pin the three properties that follow from
 * it: CLOSED is terminal, asking for the status an organization already holds is tolerated but still audited, and
 * suspending writes nothing to the stores — {@code InternalStoreService.requireOperable} reads the org's status, so
 * a fan-out write would only be a second copy to drift.
 * </p>
 */
class OrgLifecycleServiceTest {

    private static final ManagerOrgId ORG = new ManagerOrgId("21f023932bc66470c104b76f");

    private static final String OPERATOR = "super-admin";

    private static final String BEFORE = "Before";

    private static final String AFTER = "After";

    private static final String UNPAID = "unpaid";

    private static final String STATUS = "STATUS";

    private ManagerOrgRepository repository;

    private TenancyAuditService auditService;

    private OrgLifecycleService service;

    @BeforeEach
    void setUp() {
        repository = mock(ManagerOrgRepository.class);
        auditService = mock(TenancyAuditService.class);
        ManagerOrgMappers mappers = mock(ManagerOrgMappers.class);
        when(mappers.toDto(any())).thenAnswer(it -> {
            ManagerOrgEntity entity = it.getArgument(0);
            return new ManagerOrgDto(ORG, new Email("mail@example.com"), null, entity.getName(), entity.getStatus(),
                    null);
        });
        when(repository.save(any())).thenAnswer(it -> it.getArgument(0));
        service = new OrgLifecycleService(repository, mappers, auditService);
    }

    private ManagerOrgEntity orgIn(OrgStatus status) {
        ManagerOrgEntity entity = new ManagerOrgEntity();
        entity.setStatus(status);
        entity.setName(BEFORE);
        when(repository.findById(ORG)).thenReturn(Optional.of(entity));
        return entity;
    }

    @Test
    void renamingRecordsThePreviousNameAlongsideTheNewOne() throws Exception {
        orgIn(OrgStatus.ACTIVE);

        assertThat(service.rename(ORG, AFTER, OPERATOR).name()).isEqualTo(AFTER);

        verify(auditService).record(eq(AuditEntityType.ORG), eq(ORG), eq("RENAME"), eq(BEFORE), eq(AFTER),
                eq(OPERATOR), isNull());
    }

    @Test
    void suspendingCarriesTheReasonIntoTheAuditRow() throws Exception {
        orgIn(OrgStatus.ACTIVE);

        assertThat(service.suspend(ORG, OPERATOR, UNPAID).status()).isEqualTo(OrgStatus.SUSPENDED);

        verify(auditService).record(AuditEntityType.ORG, ORG, STATUS, OrgStatus.ACTIVE, OrgStatus.SUSPENDED,
                OPERATOR, UNPAID);
    }

    @Test
    void aSuspendedOrganizationCanBeResumed() throws Exception {
        orgIn(OrgStatus.SUSPENDED);

        assertThat(service.resume(ORG, OPERATOR).status()).isEqualTo(OrgStatus.ACTIVE);
    }

    @Test
    void anActiveOrganizationCanBeClosed() throws Exception {
        orgIn(OrgStatus.ACTIVE);

        assertThat(service.close(ORG, OPERATOR).status()).isEqualTo(OrgStatus.CLOSED);
    }

    /** There is deliberately no path off CLOSED. */
    @ParameterizedTest
    @CsvSource({"ACTIVE", "SUSPENDED"})
    void closedIsTerminal(OrgStatus to) {
        orgIn(OrgStatus.CLOSED);

        assertThatThrownBy(() -> move(to)).isInstanceOf(IllegalLifecycleTransitionException.class);
        verify(repository, never()).save(any());
    }

    /** Asking for the status it already holds is what a double-click looks like — tolerated, and still recorded. */
    @Test
    void aNoOpTransitionIsAuditedButNotWritten() throws Exception {
        orgIn(OrgStatus.ACTIVE);

        assertThat(service.resume(ORG, OPERATOR).status()).isEqualTo(OrgStatus.ACTIVE);

        verify(repository, never()).save(any());
        verify(auditService).record(AuditEntityType.ORG, ORG, STATUS, OrgStatus.ACTIVE, OrgStatus.ACTIVE, OPERATOR,
                "no-op: resumed by operator");
    }

    /** A row written before the status column existed is read as ACTIVE rather than as an illegal state. */
    @Test
    void anOrganizationWithNoRecordedStatusIsTreatedAsActive() throws Exception {
        orgIn(null);

        assertThat(service.suspend(ORG, OPERATOR, UNPAID).status()).isEqualTo(OrgStatus.SUSPENDED);
    }

    @Test
    void anUnknownOrganizationIsATypedNotFound() {
        when(repository.findById(ORG)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.rename(ORG, AFTER, OPERATOR)).isInstanceOf(OrgNotFoundException.class);
        assertThatThrownBy(() -> service.close(ORG, OPERATOR)).isInstanceOf(OrgNotFoundException.class);
    }

    private void move(OrgStatus to) throws OrgNotFoundException, IllegalLifecycleTransitionException {
        if (to == OrgStatus.ACTIVE) {
            service.resume(ORG, OPERATOR);
        } else {
            service.suspend(ORG, OPERATOR, UNPAID);
        }
    }

}
