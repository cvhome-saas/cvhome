package com.asrevo.cvhome.sso.security;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import com.asrevo.cvhome.sso.domain.AccountLinking;
import com.asrevo.cvhome.sso.domain.IdentityProvider;
import com.asrevo.cvhome.sso.domain.User;
import com.asrevo.cvhome.sso.idp.BrokerOutcome;
import com.asrevo.cvhome.sso.idp.BrokerRefusedException;
import com.asrevo.cvhome.sso.idp.BrokeredIdentity;
import com.asrevo.cvhome.sso.idp.IdentityBrokerService;
import com.asrevo.cvhome.sso.idp.IdentityProviderService;
import com.asrevo.cvhome.sso.idp.IdpPreset;
import com.asrevo.cvhome.sso.idp.PendingLink;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * An OpenID Connect provider's user, resolved to a local account.
 *
 * <p>
 * The provider is looked up by the registration id, so an alias the realm does not know must fail as an
 * authentication error rather than a {@code NullPointerException} — a stale registration in the client repository
 * is exactly the case where that happens.
 * </p>
 *
 * <p>
 * A broker outcome that needs confirmation is thrown as a {@link BrokerRefusedException} carrying the pending
 * link, because Spring's login filter is what routes it to the failure handler that parks the link; returning a
 * principal instead would sign the person in without the password step.
 * </p>
 */
class BrokeredOidcUserServiceTest {

    private static final String ALIAS = "corp";
    private static final String SUBJECT = "sub-1";
    private static final String EMAIL = "someone@example.com";
    private static final String USERNAME = "someone";
    private static final String FIRST_NAME = "Ada";
    private static final String LAST_NAME = "Lovelace";
    private static final String DISPLAY_NAME = "Corp";

    private final IdentityProviderService providers = mock(IdentityProviderService.class);
    private final IdentityBrokerService broker = mock(IdentityBrokerService.class);
    private final BrokeredOidcUserService service = new BrokeredOidcUserService(providers, broker);

    @Test
    void aresolvedLoginBecomesAbrokeredPrincipalNamedByTheLocalAccount() throws Exception {
        IdentityProvider provider = provider();
        when(providers.byAlias(ALIAS)).thenReturn(Optional.of(provider));
        when(broker.resolve(eq(provider), any(BrokeredIdentity.class))).thenReturn(BrokerOutcome.signedIn(user()));

        OidcUser principal = service.loadUser(request());

        assertThat(principal).isInstanceOf(BrokeredPrincipal.class);
        assertThat(principal.getName()).isEqualTo(USERNAME);
        assertThat(((BrokeredPrincipal) principal).providerAlias()).isEqualTo(ALIAS);
        assertThat(principal.getIdToken()).isNotNull();
    }

    @Test
    void theClaimsAreMappedThroughTheProvidersOwnAttributeMappingBeforeTheBrokerSeesThem() throws Exception {
        IdentityProvider provider = provider();
        when(providers.byAlias(ALIAS)).thenReturn(Optional.of(provider));
        when(broker.resolve(eq(provider), any(BrokeredIdentity.class))).thenReturn(BrokerOutcome.signedIn(user()));

        service.loadUser(request());

        BrokeredIdentity identity = brokered(provider);
        assertThat(identity.subject()).isEqualTo(SUBJECT);
        assertThat(identity.email()).isEqualTo(EMAIL);
        assertThat(identity.emailVerified()).isTrue();
        assertThat(identity.firstName()).isEqualTo(FIRST_NAME);
        assertThat(identity.lastName()).isEqualTo(LAST_NAME);
    }

    @Test
    void anAliasTheRealmDoesNotKnowFailsAsAnAuthenticationErrorRatherThanAnullPointer() {
        when(providers.byAlias(ALIAS)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUser(request()))
                .isInstanceOf(OAuth2AuthenticationException.class)
                .extracting(e -> ((OAuth2AuthenticationException) e).getError().getErrorCode())
                .isEqualTo("idp_unknown");
    }

    @Test
    void anOutcomeNeedingConfirmationIsThrownSoTheFailureHandlerCanParkThePendingLink() throws Exception {
        IdentityProvider provider = provider();
        PendingLink pending = new PendingLink(provider.getId(), ALIAS, DISPLAY_NAME, SUBJECT, EMAIL,
                UUID.randomUUID(), USERNAME);
        when(providers.byAlias(ALIAS)).thenReturn(Optional.of(provider));
        when(broker.resolve(eq(provider), any(BrokeredIdentity.class))).thenReturn(BrokerOutcome.confirm(pending));

        assertThatThrownBy(() -> service.loadUser(request()))
                .isInstanceOf(BrokerRefusedException.class)
                .extracting(e -> ((BrokerRefusedException) e).pending())
                .isSameAs(pending);
    }

    private BrokeredIdentity brokered(IdentityProvider provider) throws Exception {
        org.mockito.ArgumentCaptor<BrokeredIdentity> captor =
                org.mockito.ArgumentCaptor.forClass(BrokeredIdentity.class);
        org.mockito.Mockito.verify(broker).resolve(eq(provider), captor.capture());
        return captor.getValue();
    }

    private static User user() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername(USERNAME);
        user.setEmail(EMAIL);
        return user;
    }

    private static IdentityProvider provider() {
        IdentityProvider provider = new IdentityProvider();
        provider.setId(UUID.randomUUID());
        provider.setAlias(ALIAS);
        provider.setDisplayName(DISPLAY_NAME);
        provider.setPreset(IdpPreset.GENERIC_OIDC);
        provider.setType(IdpPreset.GENERIC_OIDC.type());
        provider.setAccountLinking(AccountLinking.LINK);
        provider.setAttributeMapping("email=email,given_name=firstName,family_name=lastName");
        provider.setCreatedAt(Instant.EPOCH);
        provider.setUpdatedAt(Instant.EPOCH);
        return provider;
    }

    /**
     * Only {@code openid} is requested, so {@link org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService}
     * answers from the id token alone and no userinfo call is attempted — the claims under test are the ones the
     * provider actually signed.
     */
    private static OidcUserRequest request() {
        Instant now = Instant.parse("2026-05-01T00:00:00Z");
        ClientRegistration registration = ClientRegistration.withRegistrationId(ALIAS)
                .clientId("client-1")
                .clientSecret("s3cret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("https://sso.example/login/oauth2/code/corp")
                .scope("openid")
                .authorizationUri("https://idp.example/authorize")
                .tokenUri("https://idp.example/token")
                .userNameAttributeName(IdTokenClaimNames.SUB)
                .build();
        OAuth2AccessToken token = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "access", now,
                now.plusSeconds(300));
        OidcIdToken idToken = OidcIdToken.withTokenValue("id-token")
                .subject(SUBJECT)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(300))
                .claim("email", EMAIL)
                .claim("email_verified", true)
                .claim("given_name", FIRST_NAME)
                .claim("family_name", LAST_NAME)
                .build();
        return new OidcUserRequest(registration, token, idToken);
    }

}
