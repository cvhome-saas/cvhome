package com.asrevo.cvhome.sso.idp;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import com.asrevo.cvhome.sso.domain.AccountLinking;
import com.asrevo.cvhome.sso.domain.IdentityProvider;
import com.asrevo.cvhome.sso.domain.IdpType;
import com.asrevo.cvhome.uaa.errors.IdpConfigInvalidException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A stored provider as Spring's OAuth2 client sees it.
 *
 * <p>
 * <strong>Apple's registration turns {@code requireProofKey} off.</strong> Spring Security 7 defaults it to
 * {@code true}, and the default resolver applies PKCE from the registration <em>before</em> any resolver
 * customizer is consulted — so without this the realm sent Apple a {@code code_challenge} despite owning a whole
 * second resolver whose only job is not to. Every other provider keeps the default on.
 * </p>
 *
 * <p>
 * A provider missing what its type needs is refused when the registration is built rather than at the first
 * sign-in: an OIDC provider with no JWK Set URI cannot verify an id token, and an OAuth2 one with no user-info
 * endpoint has nowhere to read an identity from.
 * </p>
 */
class ClientRegistrationFactoryTest {

    private static final String ALIAS = "corp";
    private static final String CLIENT_ID = "client-1";
    private static final String SECRET = "s3cret";
    private static final String AUTHORIZE = "https://idp.example/authorize";
    private static final String TOKEN = "https://idp.example/token";
    private static final String JWKS = "https://idp.example/jwks";
    private static final String USER_INFO = "https://idp.example/userinfo";
    private static final String ID_ATTRIBUTE = "id";

    private final IdentityProviderMapper mapper = mock(IdentityProviderMapper.class);
    private final ClientRegistrationFactory factory = new ClientRegistrationFactory(mapper);

    @Test
    void appleIsRegisteredWithoutProofKeyBecauseTheRealmDoesNotSendItPkce() throws Exception {
        IdentityProvider provider = oidc(IdpPreset.APPLE);

        ClientRegistration registration = factory.build(provider);

        // Spring 7 defaults this to true and would add the code_challenge before any resolver runs.
        assertThat(registration.getClientSettings().isRequireProofKey()).isFalse();
    }

    @Test
    void everyOtherProviderKeepsProofKeyOn() throws Exception {
        ClientRegistration registration = factory.build(oidc(IdpPreset.GENERIC_OIDC));

        assertThat(registration.getClientSettings().isRequireProofKey()).isTrue();
    }

    @Test
    void theStoredEndpointsCredentialsAndScopesAllReachTheRegistration() throws Exception {
        ClientRegistration registration = factory.build(oidc(IdpPreset.GENERIC_OIDC));

        assertThat(registration.getRegistrationId()).isEqualTo(ALIAS);
        assertThat(registration.getClientId()).isEqualTo(CLIENT_ID);
        assertThat(registration.getClientSecret()).isEqualTo(SECRET);
        assertThat(registration.getScopes()).containsExactlyInAnyOrder("openid", "email");
        assertThat(registration.getAuthorizationGrantType()).isEqualTo(AuthorizationGrantType.AUTHORIZATION_CODE);
        assertThat(registration.getProviderDetails().getAuthorizationUri()).isEqualTo(AUTHORIZE);
        assertThat(registration.getProviderDetails().getTokenUri()).isEqualTo(TOKEN);
        assertThat(registration.getProviderDetails().getJwkSetUri()).isEqualTo(JWKS);
    }

    @Test
    void theRedirectUriIsTheTemplateSpringExpandsOnTheRealmsOwnOrigin() throws Exception {
        assertThat(factory.build(oidc(IdpPreset.GENERIC_OIDC)).getRedirectUri())
                .isEqualTo(ClientRegistrationFactory.REDIRECT_TEMPLATE);
    }

    @Test
    void aproviderWithNoSecretIsRegisteredWithoutOne() throws Exception {
        IdentityProvider provider = oidc(IdpPreset.GENERIC_OIDC);
        when(mapper.clientSecret(provider)).thenReturn(null);

        // Spring's own default for an unset secret; the point is that no blank is stored as if it were one.
        assertThat(factory.build(provider).getClientSecret()).isEmpty();
    }

    @Test
    void theUserNameAttributeFallsBackToSubWhenTheProviderDoesNotNameOne() throws Exception {
        assertThat(factory.build(oidc(IdpPreset.GENERIC_OIDC)).getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName()).isEqualTo("sub");
    }

    @Test
    void aprovidersOwnUserNameAttributeWins() throws Exception {
        IdentityProvider provider = oidc(IdpPreset.GENERIC_OIDC);
        provider.setUserNameAttribute(ID_ATTRIBUTE);

        assertThat(factory.build(provider).getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName())
                .isEqualTo(ID_ATTRIBUTE);
    }

    @Test
    void anOidcProviderWithNoJwkSetUriIsRefusedRatherThanFailingAtTheFirstSignIn() {
        IdentityProvider provider = oidc(IdpPreset.GENERIC_OIDC);
        provider.setJwkSetUri(null);

        // Without a JWK Set there is nothing to verify the id token's signature against.
        assertThatThrownBy(() -> factory.build(provider)).isInstanceOf(IdpConfigInvalidException.class);
    }

    @Test
    void anOauth2ProviderWithNoUserInfoEndpointIsRefused() {
        IdentityProvider provider = oidc(IdpPreset.GENERIC_OAUTH2);
        provider.setType(IdpType.OAUTH2);
        provider.setUserInfoUri(null);

        assertThatThrownBy(() -> factory.build(provider)).isInstanceOf(IdpConfigInvalidException.class);
    }

    @Test
    void aproviderWithNeitherEndpointsNorAnIssuerHasNothingToDiscoverFrom() {
        IdentityProvider provider = oidc(IdpPreset.GENERIC_OIDC);
        provider.setAuthorizationUri(null);
        provider.setTokenUri(null);
        provider.setIssuerUri(null);

        assertThatThrownBy(() -> factory.build(provider)).isInstanceOf(IdpConfigInvalidException.class);
    }

    @Test
    void aproviderIsAskedForOpenIdOnlyWhenItsScopesSaySo() {
        IdentityProvider provider = oidc(IdpPreset.GENERIC_OIDC);

        assertThat(ClientRegistrationFactory.requestsOpenId(provider)).isTrue();

        provider.setScopes("email profile");
        assertThat(ClientRegistrationFactory.requestsOpenId(provider)).isFalse();

        provider.setScopes(null);
        assertThat(ClientRegistrationFactory.requestsOpenId(provider)).isFalse();
    }

    private IdentityProvider oidc(IdpPreset preset) {
        IdentityProvider provider = new IdentityProvider();
        provider.setId(UUID.randomUUID());
        provider.setAlias(ALIAS);
        provider.setDisplayName("Corp");
        provider.setPreset(preset);
        provider.setType(IdpType.OIDC);
        provider.setAccountLinking(AccountLinking.LINK);
        provider.setAuthorizationUri(AUTHORIZE);
        provider.setTokenUri(TOKEN);
        provider.setJwkSetUri(JWKS);
        provider.setUserInfoUri(USER_INFO);
        provider.setScopes("openid email");
        provider.setClientAuthMethod("client_secret_post");
        provider.setCreatedAt(Instant.EPOCH);
        provider.setUpdatedAt(Instant.EPOCH);
        when(mapper.clientId(provider)).thenReturn(CLIENT_ID);
        when(mapper.clientSecret(provider)).thenReturn(SECRET);
        return provider;
    }

}
