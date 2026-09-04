package com.asrevo.cvhome.tenancy.manager.controller;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.Roles;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.tenancy.manager.service.InvitationService;
import com.asrevo.cvhome.tenancy.manager.service.OrgMemberService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Organisation membership and its invitations.
 *
 * <p>
 * Every method takes the organisation from the caller's token rather than a request parameter, which is what stops
 * an org admin from listing or altering another organisation's members through the same endpoint. Accepting an
 * invitation is the one exception and is deliberately ungated: the person accepting is not a member yet, so there
 * is no organisation on their token to check — the token in the link is the authorisation.
 * </p>
 */
class OrgMemberApiTest {

    private static final ManagerOrgId ORG = new ManagerOrgId("21f023932bc66470c104b76f");
    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");
    private static final String OPERATOR = "ops@example.com";
    private static final String MEMBER_EMAIL = "member@example.com";
    private static final String USER_ID = "user-1";
    private static final String ROLE = "STORE_ADMIN";
    private static final String INVITATION_ID = "inv-1";
    private static final String LINK_TOKEN = "tok-1";

    private final OrgMemberService memberService = Mockito.mock(OrgMemberService.class);
    private final InvitationService invitationService = Mockito.mock(InvitationService.class);
    private final OrgMemberApi api = new OrgMemberApi(memberService, invitationService);

    private static UserOrgStoreIdentity identity() {
        return new UserOrgStoreIdentity(ORG, STORE, Set.of(Roles.ROLE_ORG_ADMIN));
    }

    private static Authentication operator() {
        return new UsernamePasswordAuthenticationToken(OPERATOR, null, List.of());
    }

    @Test
    void listingMembersAndInvitationsIsScopedToTheCallersOwnOrganization() {
        api.members(identity());
        api.invitations(identity());

        verify(memberService).list(ORG);
        verify(invitationService).list(ORG);
    }

    @Test
    void removingAMemberAnswersAsAKeyedFlagAndRecordsWhoDidIt() {
        when(memberService.remove(ORG, USER_ID, OPERATOR)).thenReturn(true);

        assertThat(api.remove(identity(), USER_ID, operator())).containsEntry("removed", true);
    }

    @Test
    void anUnauthenticatedRemovalIsRecordedAsUnknownRatherThanNull() {
        api.remove(identity(), USER_ID, null);

        verify(memberService).remove(ORG, USER_ID, "unknown");
    }

    @Test
    void invitingAndResendingBothCarryTheOrgTheRoleAndTheActor() throws Exception {
        api.invite(identity(), MEMBER_EMAIL, ROLE, operator());
        api.resend(identity(), MEMBER_EMAIL, ROLE, operator());

        verify(invitationService).invite(ORG, MEMBER_EMAIL, ROLE, OPERATOR);
        verify(invitationService).resend(ORG, MEMBER_EMAIL, ROLE, OPERATOR);
    }

    @Test
    void revokingIsScopedToTheOrganizationThatIssuedTheInvitation() throws Exception {
        api.revoke(identity(), INVITATION_ID, operator());

        verify(invitationService).revoke(ORG, INVITATION_ID, OPERATOR);
    }

    @Test
    void acceptingIsIdentifiedByTheLinkTokenAloneBecauseTheCallerIsNotAMemberYet() throws Exception {
        api.accept(LINK_TOKEN, operator());

        verify(invitationService).accept(LINK_TOKEN, OPERATOR);
    }
}
