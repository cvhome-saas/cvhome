package com.asrevo.cvhome.sso.security;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestOperations;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * A plain-OAuth2 provider's user, resolved to a local account.
 *
 * <p>
 * GitHub is the case this class exists for. It keeps a member's address private by default and answers {@code null}
 * for {@code email} on {@code /user}, so without the extra {@code /user/emails} call every GitHub login would arrive
 * with no address — and an address is how the broker links a federated login to an existing account. The address it
 * picks must be the primary <em>and</em> verified one: signing someone in on an unverified address a stranger also
 * controls is account takeover.
 * </p>
 *
 * <p>
 * That call is best-effort. The {@code user:email} scope may not have been granted, and GitHub may simply be down;
 * neither is a reason to fail a login that could still resolve, so a failure degrades to "no address" rather than
 * propagating.
 * </p>
 */
class BrokeredOAuth2UserServiceTest {

    private static final String ALIAS = "gh";
    private static final String SUBJECT = "4711";
    private static final String EMAIL = "someone@example.com";
    private static final String USERNAME = "someone";
    private static final String ID_ATTRIBUTE = "id";
    private static final String USER_INFO_URI = "https://api.github.com/user";
    private static final String DISPLAY_NAME = "GitHub";
    private static final String PRIMARY_VERIFIED = """
            [{"email":"old@example.com","primary":false,"verified":true},
             {"email":"unverified@example.com","primary":true,"verified":false},
             {"email":"someone@example.com","primary":true,"verified":true}]""";

    private final IdentityProviderService providers = mock(IdentityProviderService.class);
    private final IdentityBrokerService broker = mock(IdentityBrokerService.class);
    private final RestOperations userInfo = mock(RestOperations.class);

    private MockRestServiceServer github;
    private BrokeredOAuth2UserService service;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        github = MockRestServiceServer.bindTo(builder).build();
        service = new BrokeredOAuth2UserService(providers, broker, builder);
        service.setRestOperations(userInfo);
    }

    @Test
    void agithubLoginWithNoAddressOnTheProfileTakesThePrimaryVerifiedOneFromTheEmailsEndpoint() throws Exception {
        IdentityProvider provider = provider(IdpPreset.GITHUB);
        given(provider, profileWithoutEmail());
        github.expect(requestTo(BrokeredOAuth2UserService.GITHUB_EMAILS))
                .andRespond(withSuccess(PRIMARY_VERIFIED, MediaType.APPLICATION_JSON));

        service.loadUser(request());

        BrokeredIdentity identity = brokered(provider);
        // Neither the non-primary address nor the unverified one: signing in on either is account takeover.
        assertThat(identity.email()).isEqualTo(EMAIL);
        assertThat(identity.emailVerified()).isTrue();
        github.verify();
    }

    @Test
    void agithubProfileThatAlreadyCarriesAnAddressIsNotAskedForItsEmailsAgain() throws Exception {
        IdentityProvider provider = provider(IdpPreset.GITHUB);
        Map<String, Object> profile = profileWithoutEmail();
        profile.put(BrokeredAttributes.EMAIL, EMAIL);
        given(provider, profile);

        service.loadUser(request());

        assertThat(brokered(provider).email()).isEqualTo(EMAIL);
        github.verify();
    }

    @Test
    void aproviderThatIsNotGithubIsNeverAskedForGithubsEmails() throws Exception {
        IdentityProvider provider = provider(IdpPreset.GENERIC_OAUTH2);
        given(provider, profileWithoutEmail());

        service.loadUser(request());

        assertThat(brokered(provider).email()).isNull();
        github.verify();
    }

    @Test
    void anEmailsCallThatFailsDegradesToNoAddressRatherThanFailingTheLogin() throws Exception {
        IdentityProvider provider = provider(IdpPreset.GITHUB);
        given(provider, profileWithoutEmail());
        github.expect(requestTo(BrokeredOAuth2UserService.GITHUB_EMAILS)).andRespond(withServerError());

        OAuth2User principal = service.loadUser(request());

        // The user:email scope may not have been granted; that is not a reason to refuse a login.
        assertThat(principal.getName()).isEqualTo(USERNAME);
        assertThat(brokered(provider).email()).isNull();
    }

    @Test
    void anEmptyEmailsListLeavesTheLoginWithoutAnAddress() throws Exception {
        IdentityProvider provider = provider(IdpPreset.GITHUB);
        given(provider, profileWithoutEmail());
        github.expect(requestTo(BrokeredOAuth2UserService.GITHUB_EMAILS))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        service.loadUser(request());

        assertThat(brokered(provider).email()).isNull();
    }

    @Test
    void noAddressIsPrimaryAndVerifiedSoNoneIsTaken() throws Exception {
        IdentityProvider provider = provider(IdpPreset.GITHUB);
        given(provider, profileWithoutEmail());
        github.expect(requestTo(BrokeredOAuth2UserService.GITHUB_EMAILS)).andRespond(
                withSuccess("[{\"email\":\"x@example.com\",\"primary\":true,\"verified\":false}]",
                        MediaType.APPLICATION_JSON));

        service.loadUser(request());

        assertThat(brokered(provider).email()).isNull();
    }

    @Test
    void aresolvedLoginBecomesAbrokeredPrincipalNamedByTheLocalAccount() throws Exception {
        IdentityProvider provider = provider(IdpPreset.GENERIC_OAUTH2);
        given(provider, profileWithoutEmail());

        OAuth2User principal = service.loadUser(request());

        assertThat(principal).isInstanceOf(BrokeredPrincipal.class);
        assertThat(principal.getName()).isEqualTo(USERNAME);
        assertThat(((BrokeredPrincipal) principal).providerAlias()).isEqualTo(ALIAS);
    }

    @Test
    void anAliasTheRealmDoesNotKnowFailsAsAnAuthenticationError() {
        whenUserInfoReturns(profileWithoutEmail());
        when(providers.byAlias(ALIAS)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUser(request()))
                .isInstanceOf(OAuth2AuthenticationException.class)
                .extracting(e -> ((OAuth2AuthenticationException) e).getError().getErrorCode())
                .isEqualTo("idp_unknown");
    }

    @Test
    void anOutcomeNeedingConfirmationIsThrownSoTheFailureHandlerCanParkThePendingLink() throws Exception {
        IdentityProvider provider = provider(IdpPreset.GENERIC_OAUTH2);
        PendingLink pending = new PendingLink(provider.getId(), ALIAS, DISPLAY_NAME, SUBJECT, EMAIL,
                UUID.randomUUID(), USERNAME);
        whenUserInfoReturns(profileWithoutEmail());
        when(providers.byAlias(ALIAS)).thenReturn(Optional.of(provider));
        when(broker.resolve(eq(provider), any(BrokeredIdentity.class))).thenReturn(BrokerOutcome.confirm(pending));

        assertThatThrownBy(() -> service.loadUser(request()))
                .isInstanceOf(BrokerRefusedException.class)
                .extracting(e -> ((BrokerRefusedException) e).pending())
                .isSameAs(pending);
    }

    private void given(IdentityProvider provider, Map<String, Object> profile) throws BrokerRefusedException {
        whenUserInfoReturns(profile);
        when(providers.byAlias(ALIAS)).thenReturn(Optional.of(provider));
        when(broker.resolve(eq(provider), any(BrokeredIdentity.class))).thenReturn(BrokerOutcome.signedIn(user()));
    }

    @SuppressWarnings("unchecked")
    private void whenUserInfoReturns(Map<String, Object> profile) {
        when(userInfo.exchange(any(RequestEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(profile, HttpStatus.OK));
    }

    private BrokeredIdentity brokered(IdentityProvider provider) throws BrokerRefusedException {
        ArgumentCaptor<BrokeredIdentity> captor = ArgumentCaptor.forClass(BrokeredIdentity.class);
        verify(broker).resolve(eq(provider), captor.capture());
        return captor.getValue();
    }

    private static Map<String, Object> profileWithoutEmail() {
        Map<String, Object> profile = new HashMap<>();
        profile.put(ID_ATTRIBUTE, SUBJECT);
        profile.put("name", "Ada Lovelace");
        return profile;
    }

    private static User user() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername(USERNAME);
        user.setEmail(EMAIL);
        return user;
    }

    private static IdentityProvider provider(IdpPreset preset) {
        IdentityProvider provider = new IdentityProvider();
        provider.setId(UUID.randomUUID());
        provider.setAlias(ALIAS);
        provider.setDisplayName(DISPLAY_NAME);
        provider.setPreset(preset);
        provider.setType(preset.type());
        provider.setAccountLinking(AccountLinking.LINK);
        provider.setAttributeMapping("name=firstName");
        provider.setCreatedAt(Instant.EPOCH);
        provider.setUpdatedAt(Instant.EPOCH);
        return provider;
    }

    private static OAuth2UserRequest request() {
        Instant now = Instant.parse("2026-05-01T00:00:00Z");
        ClientRegistration registration = ClientRegistration.withRegistrationId(ALIAS)
                .clientId("client-1")
                .clientSecret("s3cret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("https://sso.example/login/oauth2/code/gh")
                .scope("user:email")
                .authorizationUri("https://github.com/login/oauth/authorize")
                .tokenUri("https://github.com/login/oauth/access_token")
                .userInfoUri(USER_INFO_URI)
                .userNameAttributeName(ID_ATTRIBUTE)
                .build();
        OAuth2AccessToken token = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "access", now,
                now.plusSeconds(300));
        return new OAuth2UserRequest(registration, token);
    }

}
