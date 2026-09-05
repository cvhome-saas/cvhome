package com.asrevo.cvhome.sso.web.pub;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import com.asrevo.cvhome.sso.dto.LoginContext;
import com.asrevo.cvhome.sso.idp.PendingLink;
import com.asrevo.cvhome.sso.settings.RealmSettings;
import com.asrevo.cvhome.sso.settings.SettingsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * What the sign-in page may read before anyone is signed in.
 *
 * <p>
 * Both endpoints are public, so the assertion that matters is what they do <em>not</em> carry: the settings
 * response exposes the display name, the locale, whether remember-me is offered and the lockout numbers the page
 * explains — and nothing else from {@link RealmSettings}, which also holds the password policy and the token TTLs.
 * </p>
 *
 * <p>
 * {@code context} reads the session it finds and never creates one. Creating a session here would hand every
 * unauthenticated visitor a cookie, which is both a wasted row in the session table and a way to fill it.
 * </p>
 */
class PublicLoginControllerTest {

    private static final String CLIENT_ID = "console";
    private static final String CLIENT_NAME = "The Console";
    private static final String ALIAS = "google";
    private static final String EMAIL = "someone@example.com";
    private static final String REALM = "Realm";
    private static final String EN = "en";
    private static final String GOOGLE = "Google";

    private final SettingsService settings = mock(SettingsService.class);
    private final RequestCache requestCache = mock(RequestCache.class);
    private final RegisteredClientRepository clients = mock(RegisteredClientRepository.class);
    private final PublicLoginController controller = new PublicLoginController(settings, requestCache, clients);

    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @Test
    void theSettingsCarryWhatThePageDrawsAndTheLockoutInMinutes() {
        when(settings.current()).thenReturn(realmSettings());

        PublicLoginController.LoginSettings loginSettings = controller.settings();

        assertThat(loginSettings.displayName()).isEqualTo(REALM);
        assertThat(loginSettings.defaultLocale()).isEqualTo(EN);
        assertThat(loginSettings.rememberMeEnabled()).isTrue();
        assertThat(loginSettings.lockoutThreshold()).isEqualTo(5);
        // 900 seconds, told to the person as the 15 minutes they will actually wait.
        assertThat(loginSettings.lockoutMinutes()).isEqualTo(15);
    }

    @Test
    void thecontextNamesTheClientWhoseAuthorizationIsWaiting() {
        givenSavedAuthorizationFor(CLIENT_ID);
        when(clients.findByClientId(CLIENT_ID)).thenReturn(client());

        LoginContext context = controller.context(request, response);

        assertThat(context.clientId()).isEqualTo(CLIENT_ID);
        assertThat(context.clientName()).isEqualTo(CLIENT_NAME);
    }

    @Test
    void aclientIdThatNoLongerResolvesIsReportedWithoutAname() {
        givenSavedAuthorizationFor(CLIENT_ID);
        when(clients.findByClientId(CLIENT_ID)).thenReturn(null);

        LoginContext context = controller.context(request, response);

        assertThat(context.clientId()).isEqualTo(CLIENT_ID);
        assertThat(context.clientName()).isNull();
    }

    @Test
    void nosavedRequestMeansNoClientRatherThanAfailure() {
        when(requestCache.getRequest(any(), any())).thenReturn(null);

        LoginContext context = controller.context(request, response);

        assertThat(context.clientId()).isNull();
        assertThat(context.clientName()).isNull();
        assertThat(context.pendingLink()).isNull();
    }

    @Test
    void asavedRequestWithNoClientIdParameterIsToleratedRatherThanIndexedInto() {
        SavedRequest saved = mock(SavedRequest.class);
        when(saved.getParameterValues(OAuth2ParameterNames.CLIENT_ID)).thenReturn(new String[0]);
        when(requestCache.getRequest(any(), any())).thenReturn(saved);

        assertThat(controller.context(request, response).clientId()).isNull();
    }

    @Test
    void apendingBrokeredLoginIsSurfacedSoThePageCanAskForThePassword() {
        when(requestCache.getRequest(any(), any())).thenReturn(null);
        request.getSession().setAttribute(PendingLink.SESSION_KEY, new PendingLink(UUID.randomUUID(), ALIAS,
                GOOGLE, "sub-1", EMAIL, UUID.randomUUID(), "someone"));

        LoginContext.PendingLinkView link = controller.context(request, response).pendingLink();

        assertThat(link).isEqualTo(new LoginContext.PendingLinkView(ALIAS, GOOGLE, EMAIL));
    }

    @Test
    void thecontextNeverCreatesAsessionForAvisitorWhoDoesNotHaveOne() {
        when(requestCache.getRequest(any(), any())).thenReturn(null);

        controller.context(request, response);

        // A cookie per unauthenticated visitor is a row per visitor in the session table.
        assertThat(request.getSession(false)).isNull();
    }

    private void givenSavedAuthorizationFor(String clientId) {
        SavedRequest saved = mock(SavedRequest.class);
        when(saved.getParameterValues(OAuth2ParameterNames.CLIENT_ID)).thenReturn(new String[] {clientId});
        when(requestCache.getRequest(any(), any())).thenReturn(saved);
    }

    private static RegisteredClient client() {
        return RegisteredClient.withId("reg-1")
                .clientId(CLIENT_ID)
                .clientName(CLIENT_NAME)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .build();
    }

    private static RealmSettings realmSettings() {
        return new RealmSettings(REALM, "ops@example.com", EN, false, true,
                new RealmSettings.PasswordPolicy(12, true, true, true, false, 3, 0, true),
                new RealmSettings.Lockout(5, 900, 0),
                new RealmSettings.Sessions(1800, 28800, true, 0, false),
                new RealmSettings.Tokens(3600, 300, 86400, 365, 24),
                new RealmSettings.Keys(30, 7), 90, java.time.Instant.EPOCH, "ops", 1L);
    }

}
