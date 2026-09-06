package com.asrevo.cvhome.sso.token;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

import com.asrevo.cvhome.commons.domain.Permission;
import com.asrevo.cvhome.sso.audit.AuditActorType;
import com.asrevo.cvhome.sso.audit.AuditEventType;
import com.asrevo.cvhome.sso.audit.AuditOutcome;
import com.asrevo.cvhome.sso.audit.AuditRecord;
import com.asrevo.cvhome.sso.audit.AuditRecords;
import com.asrevo.cvhome.sso.audit.AuditService;
import com.asrevo.cvhome.sso.domain.Role;
import com.asrevo.cvhome.sso.domain.User;
import com.asrevo.cvhome.sso.repo.RoleRepository;
import com.asrevo.cvhome.sso.repo.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The rules of the grant, one case each, and the shape of what it issues.
 *
 * <p>
 * Every refusal must leave a {@code user.impersonation.denied} row naming which rule fired — the audit trail is
 * the point of the feature — so each refusing case asserts the row as well as the error.
 * </p>
 */
class ImpersonationExchangeProviderTest {

    private static final Instant NOW = Instant.parse("2026-04-01T09:30:00Z");

    private static final String SUBJECT_TOKEN = "operator-token";

    private static final String STORE = "65f023632bc46470c104b76f";

    private static final String OTHER_STORE = "65f023632bc46470c104b75f";

    private static final String REASON = "ticket 42";

    private static final String SUPER_ADMIN = "SUPER_ADMIN";

    private static final String SUPPORT = "SUPPORT";

    private static final String STORE_ADMIN = "STORE_ADMIN";

    private static final String ORG_ADMIN = "ORG_ADMIN";

    private static final String STORE_RETAIL = "STORE_RETAIL";

    private static final String READ = "read";

    private static final String WRITE = "write";

    private static final String JWT_VALUE = "exchanged-jwt";

    private static final String SECRET = "secret";

    private static final String STORE_MODERATOR = "STORE_MODERATOR";

    private static final String ORG = "org";

    private static final String ORG_ID = "org-1";

    private static final String STORE_KEY = "store";

    private static final String ORG1_ADMIN = "org1-admin";

    private static final String ADMIN_SDK = "admin-sdk";

    private final OAuth2AuthorizationService authorizations = mock(OAuth2AuthorizationService.class);

    private final UserRepository users = mock(UserRepository.class);

    private final RoleRepository roles = mock(RoleRepository.class);

    @SuppressWarnings("unchecked")
    private final OAuth2TokenGenerator<OAuth2Token> generator = mock(OAuth2TokenGenerator.class);

    private final AuditService audit = mock(AuditService.class);

    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    private final ImpersonationExchangeProvider provider =
            new ImpersonationExchangeProvider(authorizations, users, roles, generator, audit, clock);

    private final RegisteredClient client = RegisteredClient.withId("id").clientId("console-impersonation")
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.TOKEN_EXCHANGE).scope("openid").build();

    private final OAuth2ClientAuthenticationToken clientPrincipal =
            new OAuth2ClientAuthenticationToken(client, ClientAuthenticationMethod.CLIENT_SECRET_BASIC, SECRET);

    {
        Role moderator = new Role(STORE_MODERATOR);
        moderator.getPermissions().add(Permission.USERS_READ.key());
        when(roles.findByName(STORE_MODERATOR)).thenReturn(Optional.of(moderator));
    }

    // --- fixtures -------------------------------------------------------------------------------------------------

    private static Role role(String name, Permission... permissions) {
        Role role = new Role(name);
        for (Permission permission : permissions) {
            role.getPermissions().add(permission.key());
        }
        return role;
    }

    private User user(String username, Map<String, Object> metadata, Role... granted) {
        User user = User.create(username, String.format("%s@mail.com", username), username, username);
        user.setId(UUID.randomUUID());
        user.getMetadata().putAll(metadata);
        for (Role role : granted) {
            user.getRoles().add(role);
        }
        when(users.findById(user.getId())).thenReturn(Optional.of(user));
        return user;
    }

    private User superAdmin() {
        return user("super-admin", Map.of(), role(SUPER_ADMIN, Permission.USERS_IMPERSONATE, Permission.USERS_WRITE));
    }

    private User support() {
        return user("support", Map.of(), role(SUPPORT, Permission.USERS_IMPERSONATE));
    }

    private User storeAdmin() {
        return user("org1-store1-admin", Map.of(ORG, ORG_ID, STORE_KEY, STORE), role(STORE_ADMIN, Permission.USERS_READ));
    }

    /** The operator's own token, as the authorization store hands it back. */
    private void subjectToken(User operator, Map<String, Object> extraClaims, Duration remaining) {
        Map<String, Object> claims = new HashMap<>(extraClaims);
        claims.put("uid", operator.getId().toString());
        claims.put(JwtClaimNames.EXP, NOW.plus(remaining));
        OAuth2AccessToken token = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, SUBJECT_TOKEN,
                NOW.minus(Duration.ofMinutes(1)), NOW.plus(remaining));
        OAuth2Authorization authorization = OAuth2Authorization.withRegisteredClient(client)
                .principalName(operator.getId().toString())
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .token(token, metadata -> metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME, claims))
                .build();
        when(authorizations.findByToken(SUBJECT_TOKEN, OAuth2TokenType.ACCESS_TOKEN)).thenReturn(authorization);
    }

    private void subjectToken(User operator) {
        subjectToken(operator, Map.of(), Duration.ofMinutes(10));
    }

    private ImpersonationExchangeAuthenticationToken request(User target, String store, String mode) {
        return new ImpersonationExchangeAuthenticationToken(clientPrincipal, SUBJECT_TOKEN, target.getId().toString(),
                store, mode, REASON, Set.of(), Map.of());
    }

    private void generatorIssues() {
        when(generator.generate(any())).thenAnswer(invocation -> {
            OAuth2TokenContext context = invocation.getArgument(0);
            return Jwt.withTokenValue(JWT_VALUE).header("alg", "RS256")
                    .subject(context.getPrincipal().getName())
                    .issuedAt(NOW).expiresAt(NOW.plus(Duration.ofMinutes(5))).build();
        });
    }

    private OAuth2Authorization saved() {
        ArgumentCaptor<OAuth2Authorization> captor = ArgumentCaptor.forClass(OAuth2Authorization.class);
        verify(authorizations).save(captor.capture());
        return captor.getValue();
    }

    private AuditRecord recorded() {
        ArgumentCaptor<AuditRecord> captor = ArgumentCaptor.forClass(AuditRecord.class);
        verify(audit).recordDetached(captor.capture());
        return captor.getValue();
    }

    private void assertDenied(ImpersonationExchangeAuthenticationToken request, String refusal, String errorCode) {
        assertThatThrownBy(() -> provider.authenticate(request))
                .isInstanceOf(OAuth2AuthenticationException.class)
                .extracting(e -> ((OAuth2AuthenticationException) e).getError().getErrorCode())
                .isEqualTo(errorCode);
        AuditRecord record = recorded();
        assertThat(AuditRecords.typeOf(record)).isEqualTo(AuditEventType.USER_IMPERSONATION_DENIED);
        assertThat(AuditRecords.outcomeOf(record)).isEqualTo(AuditOutcome.FAILURE);
        assertThat(AuditRecords.reasonCodeOf(record)).isEqualTo(refusal);
        assertThat(AuditRecords.detailOf(record)).isEqualTo(REASON);
        verify(authorizations, never()).save(any());
    }

    // --- the happy paths ------------------------------------------------------------------------------------------

    @Test
    void aSuperAdminActingReadOnlyGetsAmoderatorTokenForTheStore() {
        User operator = superAdmin();
        User target = storeAdmin();
        subjectToken(operator);
        generatorIssues();

        Authentication issued = provider.authenticate(request(target, STORE, READ));

        OAuth2AccessTokenAuthenticationToken token = (OAuth2AccessTokenAuthenticationToken) issued;
        assertThat(token.getAccessToken().getTokenValue()).isEqualTo(JWT_VALUE);
        // Never a refresh token: an impersonation that can renew itself is one nobody can end.
        assertThat(token.getRefreshToken()).isNull();
        assertThat(token.getAdditionalParameters())
                .containsEntry(ImpersonationExchangeProvider.ISSUED_TOKEN_TYPE, ImpersonationExchangeConverter.ACCESS_TOKEN_TYPE)
                .containsEntry(ImpersonationExchangeProvider.ACT_MODE, READ);

        OAuth2Authorization authorization = saved();
        assertThat(authorization.getPrincipalName()).isEqualTo(target.getId().toString());
        assertThat(authorization.getAuthorizationGrantType()).isEqualTo(AuthorizationGrantType.TOKEN_EXCHANGE);
        assertThat(authorization.getRefreshToken()).isNull();
        ImpersonationContext context = ImpersonationContext.from(authorization).orElseThrow();
        assertThat(context.operatorId()).isEqualTo(operator.getId());
        assertThat(context.targetUsername()).isEqualTo(target.getUsername());
        assertThat(context.mode()).isEqualTo(ImpersonationMode.READ);
        assertThat(context.store()).isEqualTo(STORE);
        assertThat(context.roles()).containsExactly(STORE_MODERATOR);
        assertThat(context.permissions()).containsExactly(Permission.USERS_READ.key());
        // Ten minutes left on the operator's token beats the fifteen-minute ceiling.
        assertThat(context.notAfter()).isEqualTo(NOW.plus(Duration.ofMinutes(10)));

        AuditRecord record = recorded();
        assertThat(AuditRecords.typeOf(record)).isEqualTo(AuditEventType.USER_IMPERSONATION_STARTED);
        assertThat(AuditRecords.actorOf(record).type()).isEqualTo(AuditActorType.USER);
        assertThat(AuditRecords.actorOf(record).name()).isEqualTo(operator.getUsername());
        assertThat(AuditRecords.targetIdOf(record)).isEqualTo(target.getId().toString());
        assertThat(AuditRecords.reasonCodeOf(record)).isEqualTo(READ);
        assertThat(AuditRecords.detailOf(record)).isEqualTo(REASON);
    }

    @Test
    void writeModeKeepsTheTargetsOwnRolesAndCapsAtFifteenMinutes() {
        User operator = superAdmin();
        User target = storeAdmin();
        subjectToken(operator, Map.of(), Duration.ofHours(1));
        generatorIssues();

        provider.authenticate(request(target, STORE, WRITE));

        ImpersonationContext context = ImpersonationContext.from(saved()).orElseThrow();
        assertThat(context.mode()).isEqualTo(ImpersonationMode.WRITE);
        assertThat(context.overridesRoles()).isFalse();
        assertThat(context.notAfter()).isEqualTo(NOW.plus(Duration.ofMinutes(ImpersonationExchangeProvider.MAX_MINUTES)));
    }

    /** An org admin has no {@code store} metadata; which stores they own is tenancy's to know, not uaa's. */
    @Test
    void anOrgAdminTargetActsInAnyStoreTheCallerNamed() {
        User operator = superAdmin();
        User target = user(ORG1_ADMIN, Map.of(ORG, ORG_ID), role(ORG_ADMIN, Permission.USERS_WRITE));
        subjectToken(operator);
        generatorIssues();

        provider.authenticate(request(target, OTHER_STORE, READ));

        assertThat(ImpersonationContext.from(saved()).orElseThrow().store()).isEqualTo(OTHER_STORE);
    }

    // --- the refusals ---------------------------------------------------------------------------------------------

    @Test
    void aSubjectTokenThatIsNotLiveIsAnInvalidGrant() {
        User target = storeAdmin();
        when(authorizations.findByToken(SUBJECT_TOKEN, OAuth2TokenType.ACCESS_TOKEN)).thenReturn(null);

        assertThatThrownBy(() -> provider.authenticate(request(target, STORE, READ)))
                .isInstanceOf(OAuth2AuthenticationException.class)
                .extracting(e -> ((OAuth2AuthenticationException) e).getError().getErrorCode())
                .isEqualTo(OAuth2ErrorCodes.INVALID_GRANT);
        verify(audit, never()).recordDetached(any());
    }

    @Test
    void aClientCredentialsSubjectTokenHasNoOperatorBehindIt() {
        User target = storeAdmin();
        OAuth2AccessToken token = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, SUBJECT_TOKEN, NOW,
                NOW.plus(Duration.ofMinutes(10)));
        when(authorizations.findByToken(SUBJECT_TOKEN, OAuth2TokenType.ACCESS_TOKEN)).thenReturn(
                OAuth2Authorization.withRegisteredClient(client).principalName(ADMIN_SDK)
                        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                        .token(token, metadata -> metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME,
                                Map.of(JwtClaimNames.SUB, ADMIN_SDK)))
                        .build());

        assertThatThrownBy(() -> provider.authenticate(request(target, STORE, READ)))
                .isInstanceOf(OAuth2AuthenticationException.class)
                .extracting(e -> ((OAuth2AuthenticationException) e).getError().getErrorCode())
                .isEqualTo(OAuth2ErrorCodes.INVALID_GRANT);
    }

    @Test
    void anImpersonatedTokenCannotBeExchangedAgain() {
        User operator = superAdmin();
        User target = storeAdmin();
        subjectToken(operator, Map.of("act", Map.of("sub", "someone")), Duration.ofMinutes(10));

        assertDenied(request(target, STORE, READ), "CHAINED", OAuth2ErrorCodes.INVALID_GRANT);
    }

    @Test
    void anOperatorWithoutThePermissionIsRefused() {
        User operator = user(ORG1_ADMIN, Map.of(ORG, ORG_ID), role(ORG_ADMIN, Permission.USERS_WRITE));
        User target = storeAdmin();
        subjectToken(operator);

        assertDenied(request(target, STORE, READ), "OPERATOR_NOT_ALLOWED", OAuth2ErrorCodes.ACCESS_DENIED);
    }

    @Test
    void anUnknownTargetIsAnInvalidRequest() {
        User operator = superAdmin();
        subjectToken(operator);
        ImpersonationExchangeAuthenticationToken request = new ImpersonationExchangeAuthenticationToken(clientPrincipal,
                SUBJECT_TOKEN, "not-an-id", STORE, READ, REASON, Set.of(), Map.of());

        assertDenied(request, "TARGET_UNKNOWN", OAuth2ErrorCodes.INVALID_REQUEST);
    }

    @Test
    void aDisabledTargetIsRefused() {
        User operator = superAdmin();
        User target = storeAdmin();
        target.setEnabled(false);
        subjectToken(operator);

        assertDenied(request(target, STORE, READ), "TARGET_DISABLED", OAuth2ErrorCodes.ACCESS_DENIED);
    }

    @Test
    void aPlatformPrincipalCannotBeImpersonated() {
        User operator = superAdmin();
        User target = support();
        subjectToken(operator);

        assertDenied(request(target, STORE, READ), "TARGET_PRIVILEGED", OAuth2ErrorCodes.ACCESS_DENIED);
    }

    @Test
    void supportMayNotActInWriteMode() {
        User operator = support();
        User target = storeAdmin();
        subjectToken(operator);

        assertDenied(request(target, STORE, WRITE), "WRITE_NOT_ALLOWED", OAuth2ErrorCodes.ACCESS_DENIED);
    }

    @Test
    void aStoreTheTargetDoesNotActInIsRefused() {
        User operator = superAdmin();
        User target = storeAdmin();
        subjectToken(operator);

        assertDenied(request(target, OTHER_STORE, READ), "STORE_NOT_TARGETS", OAuth2ErrorCodes.INVALID_REQUEST);
    }

    /** Minting {@code STORE_MODERATOR} for a retail account would widen it; read mode refuses rather than widens. */
    @Test
    void readModeOnAtargetWithNoReadRoleIsRefused() {
        User operator = superAdmin();
        User target = user("org1-store1-pos", Map.of(ORG, ORG_ID, STORE_KEY, STORE), role(STORE_RETAIL));
        subjectToken(operator);

        assertDenied(request(target, STORE, READ), "TARGET_NOT_READABLE", OAuth2ErrorCodes.ACCESS_DENIED);
    }

    @Test
    void anUnknownModeIsAnInvalidRequestBeforeAnyRuleRuns() {
        User operator = superAdmin();
        User target = storeAdmin();
        subjectToken(operator);

        assertThatThrownBy(() -> provider.authenticate(request(target, STORE, "rw")))
                .isInstanceOf(OAuth2AuthenticationException.class)
                .extracting(e -> ((OAuth2AuthenticationException) e).getError().getErrorCode())
                .isEqualTo(OAuth2ErrorCodes.INVALID_REQUEST);
        verify(audit, never()).recordDetached(any());
    }

    @Test
    void aClientWithoutTheGrantIsRefusedBeforeTheSubjectTokenIsRead() {
        RegisteredClient other = RegisteredClient.withId("web").clientId("web-app")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE).redirectUri("http://x/cb").build();
        OAuth2ClientAuthenticationToken principal =
                new OAuth2ClientAuthenticationToken(other, ClientAuthenticationMethod.CLIENT_SECRET_BASIC, SECRET);
        ImpersonationExchangeAuthenticationToken request = new ImpersonationExchangeAuthenticationToken(principal,
                SUBJECT_TOKEN, UUID.randomUUID().toString(), STORE, READ, REASON, Set.of(), Map.of());

        assertThatThrownBy(() -> provider.authenticate(request))
                .isInstanceOf(OAuth2AuthenticationException.class)
                .extracting(e -> ((OAuth2AuthenticationException) e).getError().getErrorCode())
                .isEqualTo(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT);
        verify(authorizations, never()).findByToken(any(), any());
    }

    @Test
    void supportsOnlyItsOwnToken() {
        assertThat(provider.supports(ImpersonationExchangeAuthenticationToken.class)).isTrue();
        assertThat(provider.supports(OAuth2ClientAuthenticationToken.class)).isFalse();
        assertThat(List.of(ImpersonationExchangeProvider.READ_CAPABLE)).isNotEmpty();
    }

}
