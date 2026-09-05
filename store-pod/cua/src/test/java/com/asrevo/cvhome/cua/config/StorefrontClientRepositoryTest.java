package com.asrevo.cvhome.cua.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.asrevo.cvhome.commons.domain.RealmId;
import com.asrevo.cvhome.sso.realm.RealmRegistry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * cua's client repository, which rewrites a stored client's redirect URIs to the storefront that is asking.
 *
 * <p>
 * Every store is its own realm and its own OAuth2 client, and a storefront can be reached on several origins — a
 * custom domain, the pod's own host, a language-prefixed path. Storing every one of them per client is not
 * workable, so the redirect is derived from the request instead. That is only safe because
 * {@link #findByClientId} refuses a client id that is not a known realm: without that check, an attacker could ask
 * for authorization as any client id and have their own origin written back as its redirect.
 * </p>
 */
class StorefrontClientRepositoryTest {

    private static final String STORE_REALM = "65f023632bc46470c104b76f";
    private static final String HOST = "shop.example.com";
    private static final String STORED_CALLBACK = "https://stored.example.com/callback";
    private static final String CALLBACK = "https://%s/callback";
    private static final String CLIENT_ROW_ID = "id-1";
    private static final String UNKNOWN_CLIENT = "not-a-store";
    private static final String MISSING_ID = "missing";

    private final RegisteredClientRepository delegate = Mockito.mock(RegisteredClientRepository.class);
    private final RealmRegistry realms = Mockito.mock(RealmRegistry.class);
    private final StorefrontClientRepository repository = new StorefrontClientRepository(delegate, realms);

    @AfterEach
    void clearRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

    private static RegisteredClient stored() {
        return RegisteredClient.withId(CLIENT_ROW_ID)
                .clientId(STORE_REALM)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(STORED_CALLBACK)
                .postLogoutRedirectUri("https://stored.example.com")
                .build();
    }

    private static void onRequest(String language) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oauth2/authorize");
        request.setScheme("https");
        request.setServerName(HOST);
        request.setServerPort(443);
        if (language != null) {
            request.setParameter("lang", language);
        }
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void aClientIdThatIsNotAKnownRealmResolvesToNothing() {
        when(realms.exists(RealmId.of(UNKNOWN_CLIENT))).thenReturn(false);

        assertThat(repository.findByClientId(UNKNOWN_CLIENT)).isNull();
        // The delegate is never asked, so an unknown id cannot have an origin written back as its redirect.
        verify(delegate, Mockito.never()).findByClientId(any());
    }

    @Test
    void aNullClientIdResolvesToNothingWithoutTouchingTheRegistry() {
        assertThat(repository.findByClientId(null)).isNull();

        verify(realms, Mockito.never()).exists(any());
    }

    @Test
    void aKnownRealmsClientHasItsRedirectsRewrittenToTheAskingStorefront() {
        when(realms.exists(RealmId.of(STORE_REALM))).thenReturn(true);
        when(delegate.findByClientId(STORE_REALM)).thenReturn(stored());
        onRequest(null);

        RegisteredClient client = repository.findByClientId(STORE_REALM);

        assertThat(client.getRedirectUris()).containsExactly(CALLBACK.formatted(HOST));
        assertThat(client.getPostLogoutRedirectUris()).containsExactly("https://%s".formatted(HOST));
    }

    @Test
    void theStoredRedirectsAreReplacedRatherThanAddedTo() {
        when(realms.exists(RealmId.of(STORE_REALM))).thenReturn(true);
        when(delegate.findByClientId(STORE_REALM)).thenReturn(stored());
        onRequest(null);

        // Keeping the stored ones would leave a redirect nobody controls on every store's client.
        assertThat(repository.findByClientId(STORE_REALM).getRedirectUris()).hasSize(1);
    }

    @Test
    void aLanguagePrefixedStorefrontGetsItsLanguageInTheRedirect() {
        when(realms.exists(RealmId.of(STORE_REALM))).thenReturn(true);
        when(delegate.findByClientId(STORE_REALM)).thenReturn(stored());
        onRequest("ar");

        assertThat(repository.findByClientId(STORE_REALM).getRedirectUris())
                .containsExactly("https://%s/ar/callback".formatted(HOST));
    }

    @Test
    void withNoRequestToDeriveFromTheStoredRowIsGoodEnough() {
        when(delegate.findById(CLIENT_ROW_ID)).thenReturn(stored());

        // A background read of a stored authorization has no request; rewriting would be guesswork.
        assertThat(repository.findById(CLIENT_ROW_ID).getRedirectUris())
                .containsExactly(STORED_CALLBACK);
    }

    @Test
    void anUnknownIdIsStillNull() {
        when(delegate.findById(MISSING_ID)).thenReturn(null);

        assertThat(repository.findById(MISSING_ID)).isNull();
    }

    @Test
    void savingIsPassedStraightThroughToTheStore() {
        RegisteredClient client = stored();

        repository.save(client);

        verify(delegate).save(client);
    }

    @Test
    void findByIdAlsoRewritesWhenThereIsARequest() {
        when(delegate.findById(CLIENT_ROW_ID)).thenReturn(stored());
        onRequest(null);

        assertThat(repository.findById(CLIENT_ROW_ID).getRedirectUris())
                .containsExactly(CALLBACK.formatted(HOST));
    }
}
