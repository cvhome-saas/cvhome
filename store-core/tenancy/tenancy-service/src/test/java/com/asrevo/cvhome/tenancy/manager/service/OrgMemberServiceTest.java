package com.asrevo.cvhome.tenancy.manager.service;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.tenancy.commons.dto.AuditEntityType;
import com.asrevo.cvhome.tenancy.manager.entity.OrgMemberEntity;
import com.asrevo.cvhome.tenancy.manager.repository.OrgMemberRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Membership, and the one thing a join table has to get right here: a grant or a removal that changed nothing must
 * not be audited as though it had.
 *
 * <p>
 * The insert is {@code on conflict do nothing}, so a repeated grant is tolerated rather than an error — but it
 * returns zero rows, and an audit row claiming a membership was added when it already existed is a lie in the one
 * place the platform keeps for answering "who did this".
 * </p>
 */
class OrgMemberServiceTest {

    private static final ManagerOrgId ORG = new ManagerOrgId("21f023932bc66470c104b76f");

    private static final String USER = "9f1c0e58-0000-4000-8000-000000000001";

    private static final String ACTOR = "org-admin";

    private static final String ROLE = "STORE_ADMIN";

    private OrgMemberRepository repository;

    private TenancyAuditService auditService;

    private OrgMemberService service;

    @BeforeEach
    void setUp() {
        repository = mock(OrgMemberRepository.class);
        auditService = mock(TenancyAuditService.class);
        service = new OrgMemberService(repository, auditService);
    }

    @Test
    void theListIsReadForTheOrganizationsBareId() {
        OrgMemberEntity member = mock(OrgMemberEntity.class);
        when(member.getOrgId()).thenReturn(ORG);
        when(member.getUserId()).thenReturn(USER);
        when(member.getRole()).thenReturn(ROLE);
        when(member.getAddedAt()).thenReturn(Instant.EPOCH);
        when(member.getAddedBy()).thenReturn(ACTOR);
        when(repository.findByOrg(ORG.getId().toString())).thenReturn(List.of(member));

        assertThat(service.list(ORG)).singleElement()
                .satisfies(it -> assertThat(it.userId()).isEqualTo(USER));
    }

    @Test
    void aNewMemberIsAddedAndAudited() {
        when(repository.add(anyString(), anyString(), anyString(), anyString())).thenReturn(1);

        assertThat(service.add(ORG, USER, ROLE, ACTOR)).isTrue();

        verify(auditService).record(eq(AuditEntityType.MEMBER), eq(ORG), eq("ADD"), isNull(), eq(ROLE), eq(ACTOR),
                eq(String.format("added %s", USER)));
    }

    /** A repeated grant is tolerated, and is not an event. */
    @Test
    void aGrantToSomeoneWhoAlreadyBelongsIsNotAudited() {
        when(repository.add(anyString(), anyString(), anyString(), anyString())).thenReturn(0);

        assertThat(service.add(ORG, USER, ROLE, ACTOR)).isFalse();

        verify(auditService, never()).record(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void removingAMemberIsAudited() {
        when(repository.remove(anyString(), anyString())).thenReturn(1);

        assertThat(service.remove(ORG, USER, ACTOR)).isTrue();

        verify(auditService).record(eq(AuditEntityType.MEMBER), eq(ORG), eq("REMOVE"), isNull(), isNull(), eq(ACTOR),
                eq(String.format("removed %s", USER)));
    }

    /**
     * The org comes from the caller's identity, so a removal that matches nothing is what a foreign admin's attempt
     * looks like — it reports that nothing was removed rather than pretending it was.
     */
    @Test
    void removingSomeoneWhoDoesNotBelongRemovesNothingAndIsNotAudited() {
        when(repository.remove(anyString(), anyString())).thenReturn(0);

        assertThat(service.remove(ORG, USER, ACTOR)).isFalse();

        verify(auditService, never()).record(any(), any(), any(), any(), any(), any(), any());
    }

}
