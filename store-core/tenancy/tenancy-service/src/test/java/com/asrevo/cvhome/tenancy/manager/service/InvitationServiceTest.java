package com.asrevo.cvhome.tenancy.manager.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.tenancy.commons.dto.CreatedInvitationDto;
import com.asrevo.cvhome.tenancy.commons.dto.InvitationDto;
import com.asrevo.cvhome.tenancy.commons.dto.InvitationStatus;
import com.asrevo.cvhome.tenancy.errors.InvitationAlreadyExistsException;
import com.asrevo.cvhome.tenancy.errors.InvitationNotUsableException;
import com.asrevo.cvhome.tenancy.manager.entity.OrgInvitationEntity;
import com.asrevo.cvhome.tenancy.manager.repository.OrgInvitationRepository;
import com.asrevo.cvhome.tenancy.manager.repository.OrgMemberRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Invitations, and specifically the properties that make the token safe to hand out.
 */
class InvitationServiceTest {

    private static final ManagerOrgId ORG = new ManagerOrgId("21f023932bc66470c104b76f");

    private static final String EMAIL = "invitee@example.com";

    private static final String ROLE = "STORE_ADMIN";

    private static final String ACTOR = "admin";

    private static final String SECRET_HASH = "secret-hash";

    private static final String HASH = "hash";

    private static final String USER = "user-1";

    private static final String TOKEN = "token";

    private static final String INVITATION_ID = "31f023932bc66470c104b76f";

    private OrgInvitationRepository invitations;

    private OrgMemberRepository members;

    private InvitationService service;

    @BeforeEach
    void setUp() {
        invitations = mock(OrgInvitationRepository.class);
        members = mock(OrgMemberRepository.class);
        when(invitations.save(any())).thenAnswer(it -> it.getArgument(0));
        service = new InvitationService(invitations, members, mock(TenancyAuditService.class), Duration.ofDays(7));
    }

    @Test
    @DisplayName("the token is returned once and only its hash is stored")
    void tokenIsReturnedOnceAndStoredHashed() throws InvitationAlreadyExistsException {
        when(invitations.findByOrgIdAndEmailAndStatus(any(), anyString(), any())).thenReturn(Optional.empty());

        CreatedInvitationDto created = service.invite(ORG, EMAIL, ROLE, ACTOR);

        assertThat(created.token()).isNotBlank();
        // Anyone who can read the table must not be able to accept the invitation.
        assertThat(created.invitation().toString()).doesNotContain(created.token());
    }

    @Test
    @DisplayName("the address is normalised, so Invitee@Example.com and invitee@example.com are one invitation")
    void emailIsNormalised() throws InvitationAlreadyExistsException {
        when(invitations.findByOrgIdAndEmailAndStatus(any(), anyString(), any())).thenReturn(Optional.empty());

        CreatedInvitationDto created = service.invite(ORG, "  Invitee@Example.COM ", ROLE, ACTOR);

        assertThat(created.invitation().email()).isEqualTo(EMAIL);
    }

    @Test
    @DisplayName("a second live invitation for the same address is refused")
    void duplicateInviteIsRefused() {
        OrgInvitationEntity live = OrgInvitationEntity.create(ORG, EMAIL, ROLE, HASH,
                Instant.now().plusSeconds(3600), ACTOR);
        when(invitations.findByOrgIdAndEmailAndStatus(any(), anyString(), any())).thenReturn(Optional.of(live));

        assertThatThrownBy(() -> service.invite(ORG, EMAIL, ROLE, ACTOR))
                .isInstanceOf(InvitationAlreadyExistsException.class);
    }

    @Test
    @DisplayName("an unknown token is refused without saying why")
    void unknownTokenIsRefused() {
        when(invitations.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.accept("made-up", USER))
                .isInstanceOf(InvitationNotUsableException.class);
        verify(members, never()).add(any(), any(), any(), any());
    }

    @Test
    @DisplayName("an expired invitation is refused and relabelled, not left looking pending forever")
    void expiredTokenIsRefusedAndRelabelled() {
        OrgInvitationEntity expired = OrgInvitationEntity.create(ORG, EMAIL, ROLE, HASH,
                Instant.now().minusSeconds(60), ACTOR);
        when(invitations.findByTokenHash(anyString())).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> service.accept(TOKEN, USER))
                .isInstanceOf(InvitationNotUsableException.class);

        assertThat(expired.getStatus()).isEqualTo(InvitationStatus.EXPIRED);
        verify(members, never()).add(any(), any(), any(), any());
    }

    @Test
    @DisplayName("accepting adds the member and spends the invitation")
    void acceptAddsMemberAndSpendsInvitation() throws Exception {
        OrgInvitationEntity live = OrgInvitationEntity.create(ORG, EMAIL, ROLE, HASH,
                Instant.now().plusSeconds(3600), ACTOR);
        when(invitations.findByTokenHash(anyString())).thenReturn(Optional.of(live));

        service.accept(TOKEN, USER);

        verify(members).add(ORG.getId().toString(), USER, ROLE, ACTOR);
        assertThat(live.getStatus()).isEqualTo(InvitationStatus.ACCEPTED);
        assertThat(live.getAcceptedBy()).isEqualTo(USER);
    }

    @Test
    @DisplayName("a spent invitation cannot be accepted twice")
    void acceptedTokenCannotBeReused() {
        OrgInvitationEntity spent = OrgInvitationEntity.create(ORG, EMAIL, ROLE, HASH,
                Instant.now().plusSeconds(3600), ACTOR);
        spent.setStatus(InvitationStatus.ACCEPTED);
        when(invitations.findByTokenHash(anyString())).thenReturn(Optional.of(spent));

        assertThatThrownBy(() -> service.accept(TOKEN, "user-2"))
                .isInstanceOf(InvitationNotUsableException.class);
    }

    @Test
    @DisplayName("resending rotates the token, so a link that went astray stops working")
    void resendRotatesTheToken() throws InvitationAlreadyExistsException {
        OrgInvitationEntity live = OrgInvitationEntity.create(ORG, EMAIL, ROLE, "old-hash",
                Instant.now().plusSeconds(3600), ACTOR);
        when(invitations.findByOrgIdAndEmailAndStatus(any(), anyString(), any()))
                .thenReturn(Optional.of(live))
                .thenReturn(Optional.empty());

        CreatedInvitationDto created = service.resend(ORG, EMAIL, ROLE, ACTOR);

        assertThat(live.getStatus()).isEqualTo(InvitationStatus.REVOKED);
        assertThat(created.token()).isNotBlank();
    }

    @Test
    @DisplayName("listing never carries the token")
    void listOmitsTheToken() {
        OrgInvitationEntity live = OrgInvitationEntity.create(ORG, EMAIL, ROLE, SECRET_HASH,
                Instant.now().plusSeconds(3600), ACTOR);
        when(invitations.findByOrgId(ORG)).thenReturn(List.of(live));

        assertThat(service.list(ORG)).hasSize(1)
                .allSatisfy(it -> assertThat(it.toString()).doesNotContain(SECRET_HASH));
    }

    @Test
    @DisplayName("an invitation id that belongs to no invitation of this org cannot be revoked")
    void revokingAnUnknownInvitationIsRefused() {
        when(invitations.findByOrgId(ORG)).thenReturn(List.of());

        // Scoped by org first, so one organisation cannot revoke another's invitation by guessing its id.
        assertThatThrownBy(() -> service.revoke(ORG, "no-such-id", ACTOR))
                .isInstanceOf(InvitationNotUsableException.class);
    }

    @Test
    @DisplayName("an invitation that is no longer pending cannot be revoked twice")
    void revokingAnAlreadyRevokedInvitationIsRefused() {
        OrgInvitationEntity revoked = OrgInvitationEntity.create(ORG, EMAIL, ROLE, SECRET_HASH,
                Instant.now().plusSeconds(3600), ACTOR);
        revoked.setId(new StoreMerchantId(INVITATION_ID));
        revoked.setStatus(InvitationStatus.REVOKED);
        when(invitations.findByOrgId(ORG)).thenReturn(List.of(revoked));

        assertThatThrownBy(() -> service.revoke(ORG, INVITATION_ID, ACTOR))
                .isInstanceOf(InvitationNotUsableException.class);
    }

    @Test
    @DisplayName("revoking a pending invitation records the transition it made")
    void revokingAPendingInvitationMarksItRevoked() throws Exception {
        OrgInvitationEntity pending = OrgInvitationEntity.create(ORG, EMAIL, ROLE, SECRET_HASH,
                Instant.now().plusSeconds(3600), ACTOR);
        pending.setId(new StoreMerchantId(INVITATION_ID));
        when(invitations.findByOrgId(ORG)).thenReturn(List.of(pending));

        InvitationDto revoked = service.revoke(ORG, INVITATION_ID, ACTOR);

        assertThat(revoked.status()).isEqualTo(InvitationStatus.REVOKED);
    }

}
