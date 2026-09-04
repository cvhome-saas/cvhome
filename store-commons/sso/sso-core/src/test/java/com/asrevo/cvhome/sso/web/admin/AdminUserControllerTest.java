package com.asrevo.cvhome.sso.web.admin;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;

import com.asrevo.cvhome.sso.dto.CreateResetLinkRequest;
import com.asrevo.cvhome.sso.dto.UserDto;
import com.asrevo.cvhome.sso.dto.UserSearch;
import com.asrevo.cvhome.sso.idp.UserIdentityService;
import com.asrevo.cvhome.sso.invitation.InvitationService;
import com.asrevo.cvhome.sso.invitation.PasswordResetService;
import com.asrevo.cvhome.sso.service.AdminService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The super-admin console's user endpoints.
 *
 * <p>
 * The metadata filter is the only thing here that is not delegation. A console can narrow the user list by any
 * metadata key using {@code metadata[org]=…}, and the controller has to strip the wrapper to get the key back —
 * off-by-one on either end silently searches for a key nobody has, which reads as "no users match" rather than as
 * a bug.
 * </p>
 *
 * <p>
 * Every endpoint on this controller is gated on the super-admin authority, and the whole class is the platform
 * operator's. A method that lost its annotation would be reachable by any authenticated principal, so the gate is
 * asserted by reflection over all of them rather than one at a time.
 * </p>
 */
class AdminUserControllerTest {

    private static final String ORG_ONE = "org-1";

    private static final String PAGE_PARAM = "page";

    private static final String ZERO = "0";

    private static final String QUERY = "q";

    private static final String SOMEONE = "someone";

    private static final UUID USER = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final String SESSION = "s-1";
    private static final String ROLE = "STORE_ADMIN";

    private final AdminService adminService = mock(AdminService.class);
    private final InvitationService invitations = mock(InvitationService.class);
    private final PasswordResetService resets = mock(PasswordResetService.class);
    private final UserIdentityService identities = mock(UserIdentityService.class);

    private final AdminUserController controller =
            new AdminUserController(adminService, invitations, resets, identities);

    @Test
    void aMetadataFilterKeyIsUnwrappedFromItsBrackets() {
        when(adminService.getUsers(any(), any())).thenReturn(Page.empty());

        controller.users(null, null, null, Map.of("metadata[org]", ORG_ONE, PAGE_PARAM, ZERO), PageRequest.of(0, 20));

        ArgumentCaptor<UserSearch> search = ArgumentCaptor.forClass(UserSearch.class);
        verify(adminService).getUsers(search.capture(), any());
        // Off-by-one on either end searches for a key nobody has, which reads as "no users match".
        assertThat(search.getValue().metadata()).containsExactly(Map.entry("org", ORG_ONE));
    }

    @Test
    void aRequestWithNoMetadataParametersCarriesAnEmptyFilterRatherThanNull() {
        when(adminService.getUsers(any(), any())).thenReturn(Page.empty());

        controller.users(QUERY, null, ROLE, Map.of(PAGE_PARAM, ZERO), PageRequest.of(0, 20));

        ArgumentCaptor<UserSearch> search = ArgumentCaptor.forClass(UserSearch.class);
        verify(adminService).getUsers(search.capture(), any());
        assertThat(search.getValue().metadata()).isEmpty();
        assertThat(search.getValue().q()).isEqualTo(QUERY);
        assertThat(search.getValue().role()).isEqualTo(ROLE);
    }

    @Test
    void theReadEndpointsDelegateWithTheirOwnArguments() throws Exception {
        when(adminService.getUser(USER)).thenReturn(mock(UserDto.class));

        controller.counts();
        controller.user(USER);
        controller.usernameExist(SOMEONE);
        controller.assignableRoles();
        controller.sessions(USER);

        verify(adminService).counts();
        verify(adminService).getUser(USER);
        verify(adminService).usernameExist(SOMEONE);
        verify(adminService).getAssignableRoles();
        verify(adminService).listSessions(USER);
    }

    @Test
    void theLifecycleEndpointsAllAddressOneUserById() throws Exception {
        controller.enable(USER);
        controller.disable(USER);
        controller.delete(USER);
        controller.verifyEmail(USER);
        controller.unlock(USER);

        verify(adminService).enableUser(USER);
        verify(adminService).disableUser(USER);
        verify(adminService).delete(USER);
        verify(adminService).verifyEmail(USER);
        verify(adminService).unlock(USER);
    }

    @Test
    void revokingEverySessionAnswersWithHowManyWent() throws Exception {
        when(adminService.revokeSessions(USER)).thenReturn(2);

        assertThat(controller.revokeSessions(USER)).containsEntry("revoked", 2);
        controller.revokeSession(USER, SESSION);
        verify(adminService).revokeSession(USER, SESSION);
    }

    @Test
    void theRoleEndpointsPassTheirSetThrough() throws Exception {
        controller.assign(USER, Set.of(ROLE));
        controller.removeRoles(USER, Set.of(ROLE));

        verify(adminService).assignRoles(USER, Set.of(ROLE));
        verify(adminService).removeRoles(USER, Set.of(ROLE));
    }

    @Test
    void theInvitationEndpointsDelegateToTheInvitationService() throws Exception {
        controller.invitations(null, PageRequest.of(0, 20));
        controller.invite(null);
        controller.resendInvitation(USER);
        controller.revokeInvitation(USER);

        verify(invitations).list(eq(null), any());
        verify(invitations).invite(null);
        verify(invitations).resend(USER);
        verify(invitations).revoke(USER);
    }

    @Test
    void aResetLinkRevokesSessionsOnlyWhenTheRequestAsksItTo() throws Exception {
        controller.createResetLink(USER, null);
        controller.createResetLink(USER, new CreateResetLinkRequest(true));
        controller.createResetLink(USER, new CreateResetLinkRequest(false));

        // An absent body means "just issue the link"; revoking by default would sign the user out unasked.
        verify(resets, Mockito.times(2)).createLink(USER, false);
        verify(resets).createLink(USER, true);
    }

    @Test
    void theIdentityEndpointsResolveTheUserBeforeActing() throws Exception {
        UserDto dto = mock(UserDto.class);
        when(dto.id()).thenReturn(USER);
        when(adminService.getUser(USER)).thenReturn(dto);

        controller.identities(USER);
        controller.unlinkIdentity(USER, USER);

        verify(identities).list(USER);
        // Unlinking goes through getNonSuperAdmin, so the built-in operator cannot lose its own identity.
        verify(adminService).getNonSuperAdmin(USER);
    }

    private static Stream<Method> endpoints() {
        return Stream.of(AdminUserController.class.getDeclaredMethods())
                .filter(m -> java.lang.reflect.Modifier.isPublic(m.getModifiers()))
                .sorted((a, b) -> a.getName().compareTo(b.getName()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("endpoints")
    void everyEndpointOnThisControllerIsThePlatformOperatorsAlone(Method endpoint) {
        PreAuthorize gate = endpoint.getAnnotation(PreAuthorize.class);

        assertThat(gate).as("%s has no @PreAuthorize", endpoint.getName()).isNotNull();
        assertThat(gate.value()).contains("super_admin").contains("SUPER_ADMIN");
    }
}
