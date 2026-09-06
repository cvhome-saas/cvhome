package com.asrevo.cvhome.sso.config;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;

import com.asrevo.cvhome.commons.domain.RealmId;
import com.asrevo.cvhome.sso.domain.Role;
import com.asrevo.cvhome.sso.domain.User;
import com.asrevo.cvhome.sso.keys.KeyRotationService;
import com.asrevo.cvhome.sso.realm.RealmContext;
import com.asrevo.cvhome.sso.realm.RealmMode;
import com.asrevo.cvhome.sso.realm.SsoRealmProperties;
import com.asrevo.cvhome.sso.realm.SsoTenantIdentifierResolver;
import com.asrevo.cvhome.sso.repo.UserRepository;
import com.asrevo.cvhome.sso.security.PrincipalNames;
import com.asrevo.cvhome.sso.settings.RealmSettings;
import com.asrevo.cvhome.sso.settings.SettingsService;
import com.asrevo.cvhome.sso.token.ImpersonationContext;
import com.asrevo.cvhome.sso.token.ImpersonationMode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * What ends up in a token, and — the point — what does not.
 */
class JwtCustomizerConfigTest {

    /** SINGLE, as uaa runs: the realm claims and the subject swap are the multi-realm deployment's business. */
    private static final SsoRealmProperties SINGLE_REALM = singleRealm();

    private static final String USERNAME = "org1-store1-admin";

    private static final String EMAIL = "store1@mail.com";

    private static final String FIRST = "Store1";

    private static final String LAST = "Admin";

    private static final String ORG = "org";

    private static final String ORG_ID = "org-1";

    private static final String STORE = "store";

    private static final String STORE_ID = "store-1";

    private static final String STORE_ADMIN = "STORE_ADMIN";

    private static final String STORE_MODERATOR = "STORE_MODERATOR";

    private static final String NOTE = "note";

    private static final String SCOPE = "scope";

    private static final String AUD = "aud";

    private static final String RESOURCE = "resource";

    private static final String POD = "pod-1";

    private static final String TIER = "cvhome.tier";

    private static final String GOLD = "gold";

    private static final String KNOWN_SETTING = "settings.client.jwk-set-url";

    private static final String INTERNAL = "internal";

    private static final String X = "x";

    private static final String RS256 = "RS256";

    private static final String USERS_READ = "users:read";

    private static final String ORG_ADMIN = "ORG_ADMIN";

    private static final String OPERATOR = "super-admin";

    private final UserRepository users = mock(UserRepository.class);

    private final PrincipalNames principals = new PrincipalNames(users);

    private final SettingsService settings = mock(SettingsService.class);

    private final RealmSettings realm = mock(RealmSettings.class);

    private final KeyRotationService keys = mock(KeyRotationService.class);

    private final JwtCustomizerConfig config = new JwtCustomizerConfig(principals, settings, keys, Clock.systemUTC(),
            SINGLE_REALM, new SsoTenantIdentifierResolver(SINGLE_REALM));

    private static SsoRealmProperties singleRealm() {
        SsoRealmProperties properties = new SsoRealmProperties();
        properties.setMode(RealmMode.SINGLE);
        return properties;
    }

    {
        when(keys.activeKid()).thenReturn("kid-1");
        when(settings.current()).thenReturn(realm);
        when(realm.tokens()).thenReturn(new RealmSettings.Tokens(3600, 900, 43200, 365, 24));
    }

    private static User user(Map<String, Object> metadata, String... roles) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername(USERNAME);
        user.setEmail(EMAIL);
        user.setFirstName(FIRST);
        user.setLastName(LAST);
        user.getMetadata().putAll(metadata);
        for (String role : roles) {
            user.getRoles().add(new Role(role));
        }
        return user;
    }

    /** The principal name is the account id — {@code JpaUserDetailsService} — or a client id for a client. */
    private static JwtEncodingContext context(OAuth2TokenType type, Map<String, Object> clientSettings,
                                              String principalName) {
        RegisteredClient client = RegisteredClient.withId("id").clientId("web-app")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost/cb")
                .clientSettings(ClientSettings.builder().settings(s -> s.putAll(clientSettings)).build())
                .build();
        return JwtEncodingContext.with(JwsHeader.with(() -> RS256), JwtClaimsSet.builder().subject(USERNAME))
                .registeredClient(client)
                .principal(UsernamePasswordAuthenticationToken.authenticated(principalName, null, Set.of()))
                .tokenType(type)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .build();
    }

    /** An access token whose authorization is an impersonation of {@code target} by {@code operator}. */
    private static JwtEncodingContext impersonated(User target, String operator, ImpersonationMode mode,
                                                   Instant notAfter, Instant exp) {
        RegisteredClient client = RegisteredClient.withId("imp").clientId("console-impersonation")
                .authorizationGrantType(AuthorizationGrantType.TOKEN_EXCHANGE).build();
        OAuth2Authorization.Builder authorization = OAuth2Authorization.withRegisteredClient(client)
                .principalName(target.getId().toString()).authorizationGrantType(AuthorizationGrantType.TOKEN_EXCHANGE);
        boolean read = mode == ImpersonationMode.READ;
        new ImpersonationContext(UUID.randomUUID(), operator, target.getId(), target.getUsername(), STORE_ID, mode,
                "ticket", notAfter, read ? List.of(STORE_MODERATOR) : List.of(),
                read ? List.of(USERS_READ) : List.of()).writeTo(authorization);
        return JwtEncodingContext.with(JwsHeader.with(() -> RS256),
                        JwtClaimsSet.builder().subject(target.getId().toString()).expiresAt(exp))
                .registeredClient(client)
                .principal(UsernamePasswordAuthenticationToken.authenticated(target.getId().toString(), null, Set.of()))
                .authorization(authorization.build())
                .tokenType(OAuth2TokenType.ACCESS_TOKEN)
                .authorizationGrantType(AuthorizationGrantType.TOKEN_EXCHANGE)
                .build();
    }

    private Map<String, Object> claims(JwtEncodingContext context) {
        config.oauth2TokenCustomizer().customize(context);
        return context.getClaims().build().getClaims();
    }

    @Test
    void accessTokenCarriesRolesUidAndOnlyTheTenancyMetadata() {
        User user = user(Map.of(ORG, ORG_ID, STORE, STORE_ID, NOTE, "ignored"), STORE_ADMIN);
        when(users.findById(user.getId())).thenReturn(Optional.of(user));

        Map<String, Object> claims = claims(context(OAuth2TokenType.ACCESS_TOKEN, Map.of(), user.getId().toString()));

        assertThat(claims).containsEntry(ORG, ORG_ID).containsEntry(STORE, STORE_ID)
                .containsEntry(JwtCustomizerConfig.UID, user.getId().toString())
                .doesNotContainKey(NOTE);
        assertThat(claims.get(JwtCustomizerConfig.ROLES)).asInstanceOf(InstanceOfAssertFactories.COLLECTION)
                .containsExactly(STORE_ADMIN);
    }

    /**
     * The claim a store-scoped endpoint checks a shopper against. It used to be read from {@code clientId}, which
     * held the store id only because a store had exactly one client; the name says what the value means now, and
     * the old one is gone rather than left to drift.
     */
    @Test
    void aMultiRealmTokenNamesItsRealmAndNothingElse() {
        SsoRealmProperties multi = new SsoRealmProperties();
        multi.setMode(RealmMode.MULTI);
        JwtCustomizerConfig perStore = new JwtCustomizerConfig(principals, settings, keys, Clock.systemUTC(), multi,
                new SsoTenantIdentifierResolver(multi));
        User user = user(Map.of(), STORE_ADMIN);
        when(users.findById(user.getId())).thenReturn(Optional.of(user));

        JwtEncodingContext context = context(OAuth2TokenType.ACCESS_TOKEN, Map.of(), user.getId().toString());
        RealmContext.runIn(RealmId.of(STORE_ID), () -> perStore.oauth2TokenCustomizer().customize(context));
        Map<String, Object> claims = context.getClaims().build().getClaims();

        assertThat(claims).containsEntry(JwtCustomizerConfig.REALM, STORE_ID)
                .containsEntry("sub", user.getId().toString())
                .doesNotContainKey("clientId");
    }

    /**
     * The one branch allowed to follow {@code roles}: a read-mode impersonation narrows the target's claims to a
     * moderator's on the chosen store, names the operator in {@code act}, and never outlives the operator's token.
     */
    @Test
    void aReadModeImpersonationNarrowsTheTargetToAmoderatorAndNamesTheOperator() {
        User target = user(Map.of(ORG, ORG_ID, STORE, "some-other-store"), STORE_ADMIN, ORG_ADMIN);
        when(users.findById(target.getId())).thenReturn(Optional.of(target));
        Instant notAfter = Instant.parse("2026-04-01T09:40:00Z");
        Instant later = notAfter.plus(Duration.ofMinutes(5));

        Map<String, Object> claims = claims(impersonated(target, OPERATOR, ImpersonationMode.READ, notAfter, later));

        assertThat(claims).containsEntry(ORG, ORG_ID).containsEntry(STORE, STORE_ID)
                .containsEntry(JwtCustomizerConfig.UID, target.getId().toString())
                .containsEntry(JwtCustomizerConfig.ACT_MODE, "read")
                .containsEntry(JwtClaimNames.EXP, notAfter);
        assertThat(claims.get(JwtCustomizerConfig.ROLES)).asInstanceOf(InstanceOfAssertFactories.COLLECTION)
                .containsExactly(STORE_MODERATOR);
        assertThat(claims.get(JwtCustomizerConfig.PERMISSIONS)).asInstanceOf(InstanceOfAssertFactories.COLLECTION)
                .containsExactly(USERS_READ);
        assertThat(claims.get(JwtCustomizerConfig.ACT)).asInstanceOf(InstanceOfAssertFactories.MAP)
                .containsEntry(JwtClaimNames.SUB, OPERATOR).containsKey(JwtCustomizerConfig.UID);
    }

    /** Write mode is the target verbatim — never wider, never narrower — plus the operator's name. */
    @Test
    void aWriteModeImpersonationKeepsTheTargetsOwnClaims() {
        User target = user(Map.of(ORG, ORG_ID, STORE, STORE_ID), STORE_ADMIN);
        when(users.findById(target.getId())).thenReturn(Optional.of(target));
        Instant notAfter = Instant.parse("2026-04-01T09:45:00Z");
        Instant sooner = notAfter.minus(Duration.ofMinutes(5));

        Map<String, Object> claims = claims(impersonated(target, OPERATOR, ImpersonationMode.WRITE, notAfter, sooner));

        assertThat(claims).containsEntry(JwtCustomizerConfig.ACT_MODE, "write")
                // The generator's own, earlier expiry stands; the ceiling only ever pulls exp back.
                .containsEntry(JwtClaimNames.EXP, sooner);
        assertThat(claims.get(JwtCustomizerConfig.ROLES)).asInstanceOf(InstanceOfAssertFactories.COLLECTION)
                .containsExactly(STORE_ADMIN);
        assertThat(claims.get(JwtCustomizerConfig.ACT)).asInstanceOf(InstanceOfAssertFactories.MAP)
                .containsEntry(JwtClaimNames.SUB, OPERATOR);
    }

    @Test
    void metadataCannotOverrideTheRolesClaim() {
        User user = user(Map.of(JwtCustomizerConfig.ROLES, Set.of("SUPER_ADMIN"), SCOPE, "super_admin", AUD, X),
                STORE_MODERATOR);
        when(users.findById(user.getId())).thenReturn(Optional.of(user));

        Map<String, Object> claims = claims(context(OAuth2TokenType.ACCESS_TOKEN, Map.of(), user.getId().toString()));

        assertThat(claims.get(JwtCustomizerConfig.ROLES)).asInstanceOf(InstanceOfAssertFactories.COLLECTION)
                .containsExactly(STORE_MODERATOR);
        assertThat(claims).doesNotContainKey(SCOPE).doesNotContainKey(AUD);
    }

    @Test
    void clientSettingsBecomeClaimsOnlyUnderThePrefixOrAsResource() {
        Map<String, Object> settings = Map.of(RESOURCE, POD, TIER, GOLD, KNOWN_SETTING, X, INTERNAL, "no");

        // A client_credentials principal: a client id, which is no account id, so no user claims are added.
        Map<String, Object> claims = claims(context(OAuth2TokenType.ACCESS_TOKEN, settings, "admin-sdk"));

        assertThat(claims).containsEntry(RESOURCE, POD).containsEntry(TIER, GOLD)
                .doesNotContainKey(KNOWN_SETTING).doesNotContainKey(INTERNAL);
    }

    @Test
    void idTokenCarriesProfileClaimsAndNoTenancyMetadata() {
        User user = user(Map.of(ORG, ORG_ID), ORG_ADMIN);
        when(users.findById(user.getId())).thenReturn(Optional.of(user));

        Map<String, Object> claims = claims(context(new OAuth2TokenType(OidcParameterNames.ID_TOKEN), Map.of(),
                user.getId().toString()));

        assertThat(claims).containsEntry("email", EMAIL).containsEntry("given_name", FIRST)
                .containsEntry("family_name", LAST).containsEntry("name", String.format("%s %s", FIRST, LAST))
                .containsEntry("preferred_username", USERNAME)
                .containsEntry(JwtCustomizerConfig.UID, user.getId().toString())
                .doesNotContainKey(ORG);
    }

}
