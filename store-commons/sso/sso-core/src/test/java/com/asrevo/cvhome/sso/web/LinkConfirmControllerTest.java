package com.asrevo.cvhome.sso.web;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import com.asrevo.cvhome.sso.domain.AccountLinking;
import com.asrevo.cvhome.sso.domain.IdentityProvider;
import com.asrevo.cvhome.sso.domain.User;
import com.asrevo.cvhome.sso.dto.LinkConfirmRequest;
import com.asrevo.cvhome.sso.dto.LinkConfirmResponse;
import com.asrevo.cvhome.sso.idp.BrokerRefusedException;
import com.asrevo.cvhome.sso.idp.IdentityBrokerService;
import com.asrevo.cvhome.sso.idp.IdentityProviderService;
import com.asrevo.cvhome.sso.idp.IdpPreset;
import com.asrevo.cvhome.sso.idp.PendingLink;
import com.asrevo.cvhome.sso.security.BrokeredLoginSuccessHandler;
import com.asrevo.cvhome.uaa.errors.LinkConfirmationInvalidException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The password step of a brokered login that matched an existing account.
 *
 * <p>
 * Everything that can go wrong here answers the same way — {@link LinkConfirmationInvalidException} — and that is
 * deliberate: a caller must not be able to tell a wrong password from an expired pending link from a provider that
 * has since been deleted, because each distinction is a way to probe which addresses have accounts.
 * </p>
 *
 * <p>
 * The password goes through the realm's own authentication manager rather than a bare encoder comparison, so a
 * wrong one counts towards the lockout and a locked account is refused the same way it is on the sign-in form. And
 * the pending link is removed from the session before the sign-in completes, so a replayed request cannot link
 * twice.
 * </p>
 */
class LinkConfirmControllerTest {

    private static final String USERNAME = "someone";
    private static final String PASSWORD = "correct-horse";
    private static final String ALIAS = "corp";
    private static final String SAVED_URL = "https://sso.example/oauth2/authorize?client_id=console";
    private static final String NO = "no";
    private static final String DATABASE_IS_AWAY = "database is away";
    private static final String CORP = "Corp";
    private static final String SOMEONE_EXAMPLE_COM = "someone@example.com";

    private final AuthenticationConfiguration authenticationConfiguration = mock(AuthenticationConfiguration.class);
    private final AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
    private final IdentityProviderService providers = mock(IdentityProviderService.class);
    private final IdentityBrokerService broker = mock(IdentityBrokerService.class);
    private final BrokeredLoginSuccessHandler establish = mock(BrokeredLoginSuccessHandler.class);
    private final RequestCache requestCache = mock(RequestCache.class);
    private final LinkConfirmController controller = new LinkConfirmController(authenticationConfiguration,
            providers, broker, establish, requestCache);

    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();
    private final IdentityProvider provider = provider();
    private final PendingLink pending = pending(provider.getId());

    @BeforeEach
    void setUp() throws Exception {
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(authenticationManager);
    }

    @Test
    void aconfirmedPasswordLinksTheIdentityAndSignsTheSessionIn() throws Exception {
        givenPendingLink();
        givenTheProviderAndTheLink(user());
        when(requestCache.getRequest(any(), any())).thenReturn(null);

        LinkConfirmResponse confirmed = controller.confirm(new LinkConfirmRequest(PASSWORD), request, response);

        assertThat(confirmed).isEqualTo(new LinkConfirmResponse(USERNAME, "/"));
        verify(establish).establish(request, response, USERNAME,
                BrokeredLoginSuccessHandler.VIA_PREFIX + ALIAS);
    }

    @Test
    void thesavedAuthorizationIsWhereTheCallerIsSentBackTo() throws Exception {
        givenPendingLink();
        givenTheProviderAndTheLink(user());
        SavedRequest saved = mock(SavedRequest.class);
        when(saved.getRedirectUrl()).thenReturn(SAVED_URL);
        when(requestCache.getRequest(any(), any())).thenReturn(saved);

        LinkConfirmResponse confirmed = controller.confirm(new LinkConfirmRequest(PASSWORD), request, response);

        assertThat(confirmed.redirectTo()).isEqualTo(SAVED_URL);
        // Consumed, so a second confirmation does not resume an authorization that already finished.
        verify(requestCache).removeRequest(request, response);
    }

    @Test
    void thependingLinkIsClearedSoAreplayedRequestCannotLinkTwice() throws Exception {
        givenPendingLink();
        givenTheProviderAndTheLink(user());
        when(requestCache.getRequest(any(), any())).thenReturn(null);

        controller.confirm(new LinkConfirmRequest(PASSWORD), request, response);

        assertThat(request.getSession().getAttribute(PendingLink.SESSION_KEY)).isNull();
    }

    @Test
    void arequestWithNoSessionIsRefused() {
        assertThatThrownBy(() -> controller.confirm(new LinkConfirmRequest(PASSWORD), request, response))
                .isInstanceOf(LinkConfirmationInvalidException.class);
    }

    @Test
    void asessionWithNoPendingLinkIsRefused() {
        request.getSession();

        assertThatThrownBy(() -> controller.confirm(new LinkConfirmRequest(PASSWORD), request, response))
                .isInstanceOf(LinkConfirmationInvalidException.class);
    }

    @Test
    void awrongPasswordIsRefusedAsTheSameFailureAsAnExpiredLink() {
        givenPendingLink();
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException(NO));

        // Indistinguishable on purpose: telling them apart is a way to probe which addresses have accounts.
        assertThatThrownBy(() -> controller.confirm(new LinkConfirmRequest(PASSWORD), request, response))
                .isInstanceOf(LinkConfirmationInvalidException.class);
        verify(establish, never()).establish(any(), any(), any(), any());
    }

    @Test
    void alockedAccountIsRefusedTheSameWayItIsOnTheSignInForm() {
        givenPendingLink();
        when(authenticationManager.authenticate(any())).thenThrow(new LockedException("locked"));

        assertThatThrownBy(() -> controller.confirm(new LinkConfirmRequest(PASSWORD), request, response))
                .isInstanceOf(LinkConfirmationInvalidException.class);
    }

    @Test
    void aproviderDeletedSinceTheLoginStartedIsRefused() {
        givenPendingLink();
        givenTheCorrectPassword();
        when(providers.byId(provider.getId())).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> controller.confirm(new LinkConfirmRequest(PASSWORD), request, response))
                .isInstanceOf(LinkConfirmationInvalidException.class);
    }

    @Test
    void abrokerThatRefusesTheLinkIsRefused() throws Exception {
        givenPendingLink();
        givenTheCorrectPassword();
        when(providers.byId(provider.getId())).thenReturn(java.util.Optional.of(provider));
        when(broker.completeLink(pending, provider))
                .thenThrow(new BrokerRefusedException(BrokerRefusedException.REJECTED, NO));

        assertThatThrownBy(() -> controller.confirm(new LinkConfirmRequest(PASSWORD), request, response))
                .isInstanceOf(LinkConfirmationInvalidException.class);
    }

    @Test
    void aninfrastructureFailureDuringTheCheckIsAserverFaultRatherThanAbadPassword() {
        givenPendingLink();
        when(authenticationManager.authenticate(any()))
                .thenThrow(new org.springframework.dao.DataAccessResourceFailureException(DATABASE_IS_AWAY));

        // Only an AuthenticationException means "wrong password"; anything else must not be reported as one,
        // or a database outage would silently look like every account's password having changed.
        assertThatThrownBy(() -> controller.confirm(new LinkConfirmRequest(PASSWORD), request, response))
                .isInstanceOf(IllegalStateException.class)
                .hasRootCauseMessage(DATABASE_IS_AWAY);
    }

    private void givenPendingLink() {
        request.getSession().setAttribute(PendingLink.SESSION_KEY, pending);
    }

    private void givenTheCorrectPassword() {
        when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));
    }

    private void givenTheProviderAndTheLink(User user) throws BrokerRefusedException {
        givenTheCorrectPassword();
        when(providers.byId(provider.getId())).thenReturn(java.util.Optional.of(provider));
        when(broker.completeLink(pending, provider)).thenReturn(user);
    }

    private static PendingLink pending(UUID providerId) {
        return new PendingLink(providerId, ALIAS, CORP, "sub-1", SOMEONE_EXAMPLE_COM,
                UUID.randomUUID(), USERNAME);
    }

    private static User user() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername(USERNAME);
        user.setEmail(SOMEONE_EXAMPLE_COM);
        return user;
    }

    private static IdentityProvider provider() {
        IdentityProvider provider = new IdentityProvider();
        provider.setId(UUID.randomUUID());
        provider.setAlias(ALIAS);
        provider.setDisplayName(CORP);
        provider.setPreset(IdpPreset.GENERIC_OIDC);
        provider.setType(IdpPreset.GENERIC_OIDC.type());
        provider.setAccountLinking(AccountLinking.LINK);
        provider.setCreatedAt(Instant.EPOCH);
        provider.setUpdatedAt(Instant.EPOCH);
        return provider;
    }

}
