package com.asrevo.cvhome.sso.web.account;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.asrevo.cvhome.sso.domain.User;
import com.asrevo.cvhome.sso.dto.MeResponse;
import com.asrevo.cvhome.sso.dto.UserIdentityDto;
import com.asrevo.cvhome.sso.idp.UserIdentityService;
import com.asrevo.cvhome.sso.security.CurrentUserResolver;
import com.asrevo.cvhome.sso.service.AccountService;
import com.asrevo.cvhome.sso.session.SessionAdminService;
import com.asrevo.cvhome.sso.session.SessionSummary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * What a signed-in person may do to their own account.
 *
 * <p>
 * Every method resolves the caller from the {@link Authentication} rather than taking an id, which is what keeps
 * one account from acting on another's sessions or identities — a path parameter naming the account would be an
 * IDOR waiting to happen.
 * </p>
 *
 * <p>
 * The current session id is threaded into the three calls that end sessions so that the caller's own session is the
 * one kept. Passing {@code null} instead would sign the person out of the request they are making, which is why the
 * absent-session case is pinned rather than left to chance.
 * </p>
 */
class AccountControllerTest {

    private static final String SESSION_ID = "session-1";
    private static final String OTHER_SESSION = "session-2";
    private static final String OLD_PASSWORD = "old-password";
    private static final String NEW_PASSWORD = "new-password";
    private static final String SOMEONE = "someone";

    private final CurrentUserResolver currentUser = mock(CurrentUserResolver.class);
    private final AccountService account = mock(AccountService.class);
    private final SessionAdminService sessions = mock(SessionAdminService.class);
    private final UserIdentityService identities = mock(UserIdentityService.class);
    private final AccountController controller =
            new AccountController(currentUser, account, sessions, identities);

    private final Authentication authentication = new TestingAuthenticationToken(SOMEONE, null, List.of());
    private final User user = user();

    @Test
    void meIsWhateverTheResolverMakesOfTheCallersPrincipal() throws Exception {
        MeResponse me = mock(MeResponse.class);
        when(currentUser.describe(authentication)).thenReturn(me);

        assertThat(controller.me(authentication)).isSameAs(me);
    }

    @Test
    void changingThePasswordKeepsTheCallersOwnSessionAndEndsTheRest() throws Exception {
        givenResolved();

        controller.changePassword(new AccountController.ChangePasswordRequest(OLD_PASSWORD, NEW_PASSWORD),
                authentication, session());

        verify(account).changePassword(user, OLD_PASSWORD, NEW_PASSWORD, SESSION_ID);
    }

    @Test
    void achangeMadeWithNoSessionPassesNoneRatherThanFailing() throws Exception {
        givenResolved();

        controller.changePassword(new AccountController.ChangePasswordRequest(OLD_PASSWORD, NEW_PASSWORD),
                authentication, null);

        verify(account).changePassword(user, OLD_PASSWORD, NEW_PASSWORD, null);
    }

    @Test
    void theSessionListIsTheCallersOwnAndMarksTheCurrentOne() throws Exception {
        givenResolved();
        SessionSummary summary = mock(SessionSummary.class);
        when(sessions.list(user, SESSION_ID)).thenReturn(List.of(summary));

        assertThat(controller.sessions(authentication, session())).containsExactly(summary);
    }

    @Test
    void alistedSessionWithNoCurrentSessionMarksNoneAsCurrent() throws Exception {
        givenResolved();
        when(sessions.list(user, null)).thenReturn(List.of());

        assertThat(controller.sessions(authentication, null)).isEmpty();
    }

    @Test
    void revokingOneSessionIsScopedToTheCallersOwnAccount() throws Exception {
        givenResolved();

        controller.revokeSession(OTHER_SESSION, authentication);

        // Resolved from the principal, never from a path parameter: an id in the path would be an IDOR.
        verify(sessions).revoke(user, OTHER_SESSION);
    }

    @Test
    void revokingTheOthersReportsHowManyWentAndKeepsThisOne() throws Exception {
        givenResolved();
        when(sessions.revokeAll(user, SESSION_ID)).thenReturn(3);

        Map<String, Integer> result = controller.revokeOtherSessions(authentication, session());

        assertThat(result).containsExactly(Map.entry("revoked", 3));
    }

    @Test
    void theLinkedIdentitiesAreTheCallersOwn() throws Exception {
        givenResolved();
        UserIdentityDto identity = mock(UserIdentityDto.class);
        when(identities.list(user.getId())).thenReturn(List.of(identity));

        assertThat(controller.identities(authentication)).containsExactly(identity);
    }

    @Test
    void unlinkingIsScopedToTheCallersOwnAccount() throws Exception {
        givenResolved();
        UUID identityId = UUID.randomUUID();

        controller.unlinkIdentity(identityId, authentication);

        verify(identities).unlink(user, identityId);
    }

    @Test
    void thewholeControllerIsGatedOnBeingAuthenticated() {
        PreAuthorize gate = AccountController.class.getAnnotation(PreAuthorize.class);

        assertThat(gate).isNotNull();
        assertThat(gate.value()).isEqualTo("isAuthenticated()");
    }

    private void givenResolved() throws Exception {
        when(currentUser.resolve(authentication)).thenReturn(user);
    }

    private static MockHttpSession session() {
        return new MockHttpSession(null, SESSION_ID);
    }

    private static User user() {
        User user = new User();
        user.setId(UUID.fromString("00000000-0000-0000-0000-0000000000b1"));
        user.setUsername(SOMEONE);
        user.setEmail("someone@example.com");
        return user;
    }

}
