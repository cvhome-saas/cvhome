package com.asrevo.cvhome.sso.security;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.PkceParameterNames;

import com.asrevo.cvhome.sso.domain.AccountLinking;
import com.asrevo.cvhome.sso.domain.IdentityProvider;
import com.asrevo.cvhome.sso.idp.IdentityProviderService;
import com.asrevo.cvhome.sso.idp.IdpPreset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * What the realm adds to the authorization request it sends a provider.
 *
 * <p>
 * PKCE goes on every provider but Apple. Apple rejects a request carrying {@code code_challenge}, so it gets the
 * plain resolver plus {@code response_mode=form_post} — Apple posts the callback rather than redirecting to it.
 * That split is the whole reason this class exists, so both halves are pinned: adding PKCE to Apple breaks Apple
 * logins, and dropping it everywhere else removes a defence against code interception.
 * </p>
 *
 * <p>
 * The {@code login_hint} is whatever the sign-in page learned at the email step, forwarded only when it has text so
 * an empty box does not send an empty hint.
 * </p>
 */
class LoginHintAuthorizationRequestResolverTest {

    private static final String ALIAS = "corp";
    private static final String APPLE_ALIAS = "apple";
    private static final String BASE = "/oauth2/authorization/";
    private static final String EMAIL = "someone@example.com";
    private static final String GET = "GET";
    private static final String LOGIN = "/login";

    private final ClientRegistrationRepository registrations = mock(ClientRegistrationRepository.class);
    private final IdentityProviderService providers = mock(IdentityProviderService.class);
    private final LoginHintAuthorizationRequestResolver resolver =
            new LoginHintAuthorizationRequestResolver(registrations, providers);

    @Test
    void anOrdinaryProviderGetsPkce() {
        givenRegistration(ALIAS, IdpPreset.GENERIC_OIDC);
        givenProvider(ALIAS, IdpPreset.GENERIC_OIDC);

        OAuth2AuthorizationRequest resolved = resolver.resolve(request(ALIAS, null));

        assertThat(resolved.getAdditionalParameters()).containsKey(PkceParameterNames.CODE_CHALLENGE);
        assertThat(resolved.getAdditionalParameters()).doesNotContainKey(
                LoginHintAuthorizationRequestResolver.RESPONSE_MODE);
    }

    @Test
    void appleGetsFormPostAndNoPkceBecauseItRejectsAcodeChallenge() {
        givenRegistration(APPLE_ALIAS, IdpPreset.APPLE);
        givenProvider(APPLE_ALIAS, IdpPreset.APPLE);

        OAuth2AuthorizationRequest resolved = resolver.resolve(request(APPLE_ALIAS, null));

        assertThat(resolved.getAdditionalParameters())
                .containsEntry(LoginHintAuthorizationRequestResolver.RESPONSE_MODE,
                        LoginHintAuthorizationRequestResolver.FORM_POST);
        assertThat(resolved.getAdditionalParameters()).doesNotContainKey(PkceParameterNames.CODE_CHALLENGE);
    }

    @Test
    void aloginHintFromTheEmailStepIsForwardedToTheProvider() {
        givenRegistration(ALIAS, IdpPreset.GENERIC_OIDC);
        givenProvider(ALIAS, IdpPreset.GENERIC_OIDC);

        OAuth2AuthorizationRequest resolved = resolver.resolve(request(ALIAS, EMAIL));

        assertThat(resolved.getAdditionalParameters())
                .containsEntry(LoginHintAuthorizationRequestResolver.LOGIN_HINT, EMAIL);
    }

    @Test
    void anEmptyHintIsNotForwardedAsAnEmptyOne() {
        givenRegistration(ALIAS, IdpPreset.GENERIC_OIDC);
        givenProvider(ALIAS, IdpPreset.GENERIC_OIDC);

        OAuth2AuthorizationRequest resolved = resolver.resolve(request(ALIAS, "  "));

        assertThat(resolved.getAdditionalParameters())
                .doesNotContainKey(LoginHintAuthorizationRequestResolver.LOGIN_HINT);
    }

    @Test
    void aloginHintReachesAppleAlongsideItsFormPost() {
        givenRegistration(APPLE_ALIAS, IdpPreset.APPLE);
        givenProvider(APPLE_ALIAS, IdpPreset.APPLE);

        OAuth2AuthorizationRequest resolved = resolver.resolve(request(APPLE_ALIAS, EMAIL));

        assertThat(resolved.getAdditionalParameters())
                .containsEntry(LoginHintAuthorizationRequestResolver.LOGIN_HINT, EMAIL)
                .containsEntry(LoginHintAuthorizationRequestResolver.RESPONSE_MODE,
                        LoginHintAuthorizationRequestResolver.FORM_POST);
    }

    @Test
    void arequestThatIsNotAnAuthorizationRequestResolvesToNothing() {
        MockHttpServletRequest request = new MockHttpServletRequest(GET, LOGIN);
        request.setServletPath(LOGIN);

        assertThat(resolver.resolve(request)).isNull();
    }

    @Test
    void theExplicitRegistrationIdOverloadCustomizesTheSameWay() {
        givenRegistration(ALIAS, IdpPreset.GENERIC_OIDC);
        givenProvider(ALIAS, IdpPreset.GENERIC_OIDC);
        MockHttpServletRequest request = request(ALIAS, EMAIL);

        OAuth2AuthorizationRequest resolved = resolver.resolve(request, ALIAS);

        assertThat(resolved.getAdditionalParameters())
                .containsEntry(LoginHintAuthorizationRequestResolver.LOGIN_HINT, EMAIL);
    }

    @Test
    void anUnknownRegistrationIdIsRefusedRatherThanResolvingToSomethingUnusable() {
        when(registrations.findByRegistrationId(ALIAS)).thenReturn(null);
        MockHttpServletRequest request = new MockHttpServletRequest(GET, LOGIN);

        // Spring's own contract for the explicit-id overload; the filter only ever passes ids it just matched.
        assertThatThrownBy(() -> resolver.resolve(request, ALIAS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(ALIAS);
    }

    @Test
    void aproviderTheRealmDoesNotKnowIsTreatedAsAnOrdinaryOneRatherThanAsApple() {
        givenRegistration(ALIAS, IdpPreset.GENERIC_OIDC);
        when(providers.byAlias(ALIAS)).thenReturn(Optional.empty());

        OAuth2AuthorizationRequest resolved = resolver.resolve(request(ALIAS, null));

        assertThat(resolved.getAdditionalParameters()).containsKey(PkceParameterNames.CODE_CHALLENGE);
    }

    /**
     * Mirrors {@code ClientRegistrationFactory}: the provider's own authentication method, and — for Apple only —
     * {@code requireProofKey(false)}. Both matter, because Spring Security 7 adds PKCE from the registration
     * before any resolver customizer is consulted.
     */
    private void givenRegistration(String alias, IdpPreset preset) {
        ClientRegistration.Builder builder = ClientRegistration.withRegistrationId(alias)
                .clientId("client-1")
                .clientSecret("s3cret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("https://sso.example/login/oauth2/code/{registrationId}")
                .scope("openid")
                .authorizationUri("https://idp.example/authorize")
                .tokenUri("https://idp.example/token")
                .userNameAttributeName("sub");
        if (preset == IdpPreset.APPLE) {
            builder.clientSettings(ClientRegistration.ClientSettings.builder().requireProofKey(false).build());
        }
        when(registrations.findByRegistrationId(alias)).thenReturn(builder.build());
    }

    private void givenProvider(String alias, IdpPreset preset) {
        IdentityProvider provider = new IdentityProvider();
        provider.setId(UUID.randomUUID());
        provider.setAlias(alias);
        provider.setDisplayName(alias);
        provider.setPreset(preset);
        provider.setType(preset.type());
        provider.setAccountLinking(AccountLinking.LINK);
        provider.setCreatedAt(Instant.EPOCH);
        provider.setUpdatedAt(Instant.EPOCH);
        when(providers.byAlias(alias)).thenReturn(Optional.of(provider));
    }

    private static MockHttpServletRequest request(String alias, String loginHint) {
        MockHttpServletRequest request = new MockHttpServletRequest(GET, BASE + alias);
        request.setServletPath(BASE + alias);
        if (loginHint != null) {
            request.setParameter(LoginHintAuthorizationRequestResolver.LOGIN_HINT, loginHint);
        }
        return request;
    }

}
