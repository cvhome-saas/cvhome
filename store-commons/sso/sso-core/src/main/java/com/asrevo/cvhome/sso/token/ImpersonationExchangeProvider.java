package com.asrevo.cvhome.sso.token;

import java.security.Principal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClaimAccessor;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContext;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

import com.asrevo.cvhome.commons.domain.Permission;
import com.asrevo.cvhome.sso.audit.AuditActor;
import com.asrevo.cvhome.sso.audit.AuditActorType;
import com.asrevo.cvhome.sso.audit.AuditEventType;
import com.asrevo.cvhome.sso.audit.AuditRecord;
import com.asrevo.cvhome.sso.audit.AuditService;
import com.asrevo.cvhome.sso.domain.Role;
import com.asrevo.cvhome.sso.domain.UaaConstants;
import com.asrevo.cvhome.sso.domain.User;
import com.asrevo.cvhome.sso.repo.RoleRepository;
import com.asrevo.cvhome.sso.repo.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Issues an access token for one account to an operator who may act as it.
 *
 * <p>
 * The exchanged token's {@code sub} is the target, so every {@code @PreAuthorize}, every {@code hasPermission} and
 * every store-scoped query on every service works unchanged; {@code act} names the operator so the audit trail can
 * tell the two apart. The rules, in the order they are applied, each a
 * {@link AuditEventType#USER_IMPERSONATION_DENIED} row before the refusal:
 * </p>
 *
 * <ol>
 * <li>the subject token is a live access token this server issued, and it carries a {@code uid};</li>
 * <li>it does not already carry {@code act} — an impersonation cannot be chained;</li>
 * <li>the operator holds {@code users:impersonate};</li>
 * <li>the target is enabled, and is not a platform principal ({@code SUPER_ADMIN}, {@code SUPPORT});</li>
 * <li>{@code write} is for {@code SUPER_ADMIN} operators only — support acts read-only;</li>
 * <li>the store is one the target acts in: equal to their {@code store} metadata when they have one, otherwise —
 * an org admin — the caller's to have checked against tenancy, which uaa cannot see;</li>
 * <li>{@code read} needs a target with a store-level read role, or minting {@code STORE_MODERATOR} would
 * <em>widen</em> a retail account.</li>
 * </ol>
 *
 * <p>
 * The token lives at most {@value #MAX_MINUTES} minutes and never past the operator's own token; there is no refresh
 * token, so an impersonation cannot renew itself. The read/write choice is expressed as roles — see
 * {@link ImpersonationMode} — and written onto the authorization as an {@link ImpersonationContext}, which is what
 * {@code JwtCustomizerConfig} reads to shape the claims.
 * </p>
 */
@RequiredArgsConstructor
@Slf4j
public final class ImpersonationExchangeProvider implements AuthenticationProvider {

    public static final String ISSUED_TOKEN_TYPE = "issued_token_type";

    public static final String ACT_MODE = "act_mode";

    static final int MAX_MINUTES = 15;

    static final Duration MAX_TTL = Duration.ofMinutes(MAX_MINUTES);

    static final String SUPPORT_ROLE = "SUPPORT";

    static final String STORE_MODERATOR = "STORE_MODERATOR";

    static final String UID = "uid";

    static final String ACT = "act";

    static final String STORE_METADATA = "store";

    /** The roles whose holder reads a store, so a read-mode token for them narrows rather than widens. */
    static final Set<String> READ_CAPABLE = Set.of("ORG_ADMIN", "STORE_ADMIN", STORE_MODERATOR);

    static final Set<String> PLATFORM_ROLES = Set.of(UaaConstants.SUPER_ADMIN_ROLE, SUPPORT_ROLE);

    private static final String ROLE_PREFIX = "ROLE_";

    private final OAuth2AuthorizationService authorizations;

    private final UserRepository users;

    private final RoleRepository roles;

    private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;

    private final AuditService audit;

    private final Clock clock;

    @Override
    public boolean supports(Class<?> authentication) {
        return ImpersonationExchangeAuthenticationToken.class.isAssignableFrom(authentication);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        ImpersonationExchangeAuthenticationToken request = (ImpersonationExchangeAuthenticationToken) authentication;
        OAuth2ClientAuthenticationToken clientPrincipal = (OAuth2ClientAuthenticationToken) request.getPrincipal();
        RegisteredClient client = clientPrincipal.getRegisteredClient();
        if (client == null || !client.getAuthorizationGrantTypes().contains(AuthorizationGrantType.TOKEN_EXCHANGE)) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT);
        }
        OAuth2Authorization subject = subjectOf(request.getSubjectToken());
        Map<String, Object> subjectClaims = subject.getAccessToken().getClaims();
        User operator = operatorOf(subjectClaims);
        ImpersonationMode mode = ImpersonationMode.fromWire(request.getMode())
                .orElseThrow(() -> invalidRequest(ImpersonationExchangeConverter.MODE));
        User target = targetOf(operator, request);
        refuseUnlessAllowed(operator, target, request, mode, subjectClaims);

        Instant notAfter = notAfter(subjectClaims);
        ImpersonationContext context = new ImpersonationContext(operator.getId(), operator.getUsername(), target.getId(),
                target.getUsername(), request.getStore(), mode, request.getReason(), notAfter,
                mode == ImpersonationMode.READ ? List.of(STORE_MODERATOR) : List.of(),
                mode == ImpersonationMode.READ ? moderatorPermissions() : List.of());
        Set<String> scopes = scopesFor(client, request.getScopes());
        OAuth2AccessTokenAuthenticationToken issued = issue(client, clientPrincipal, request, target, context, scopes);
        audit.recordDetached(AuditRecord.of(AuditEventType.USER_IMPERSONATION_STARTED)
                .actor(actor(operator)).user(target.getId(), target.getUsername()).client(client.getClientId())
                .reason(mode.wire()).detail(request.getReason()));
        log.info("{} is acting as {} on store {} ({}) until {}", operator.getUsername(), target.getUsername(),
                request.getStore(), mode.wire(), notAfter);
        return issued;
    }

    private OAuth2Authorization subjectOf(String subjectToken) {
        OAuth2Authorization subject = authorizations.findByToken(subjectToken, OAuth2TokenType.ACCESS_TOKEN);
        if (subject == null || subject.getAccessToken() == null || !live(subject.getAccessToken())) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_GRANT,
                    "subject_token is not a live access token issued here.", null));
        }
        return subject;
    }

    /** Judged by this server's clock rather than {@code isActive()}, which reads the wall clock. */
    private boolean live(OAuth2Authorization.Token<OAuth2AccessToken> token) {
        Instant expiresAt = token.getToken().getExpiresAt();
        return !token.isInvalidated() && (expiresAt == null || expiresAt.isAfter(clock.instant()));
    }

    /** The person behind the subject token — a client-credentials token has no {@code uid} and is refused. */
    private User operatorOf(Map<String, Object> subjectClaims) {
        Object uid = subjectClaims.get(UID);
        Optional<User> operator = uid == null ? Optional.empty() : account(uid.toString());
        return operator.orElseThrow(() -> new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_GRANT,
                "subject_token does not belong to a user account.", null)));
    }

    private User targetOf(User operator, ImpersonationExchangeAuthenticationToken request) {
        return account(request.getRequestedSubject())
                .orElseThrow(() -> deny(operator, null, request, Refusal.TARGET_UNKNOWN));
    }

    private void refuseUnlessAllowed(User operator, User target, ImpersonationExchangeAuthenticationToken request,
                                     ImpersonationMode mode, Map<String, Object> subjectClaims) {
        if (subjectClaims.containsKey(ACT)) {
            throw deny(operator, target, request, Refusal.CHAINED);
        }
        if (!holds(operator, Permission.USERS_IMPERSONATE)) {
            throw deny(operator, target, request, Refusal.OPERATOR_NOT_ALLOWED);
        }
        if (!target.isEnabled()) {
            throw deny(operator, target, request, Refusal.TARGET_DISABLED);
        }
        if (hasAnyRole(target, PLATFORM_ROLES)) {
            throw deny(operator, target, request, Refusal.TARGET_PRIVILEGED);
        }
        if (mode == ImpersonationMode.WRITE && !hasAnyRole(operator, Set.of(UaaConstants.SUPER_ADMIN_ROLE))) {
            throw deny(operator, target, request, Refusal.WRITE_NOT_ALLOWED);
        }
        if (!actsIn(target, request.getStore())) {
            throw deny(operator, target, request, Refusal.STORE_NOT_TARGETS);
        }
        if (mode == ImpersonationMode.READ && !hasAnyRole(target, READ_CAPABLE)) {
            throw deny(operator, target, request, Refusal.TARGET_NOT_READABLE);
        }
    }

    /**
     * A target with a {@code store} in their metadata acts in that store and no other. One without — an org admin —
     * acts in any store of their organization, which uaa holds no registry of: that check is the gateway's, made
     * against tenancy before it asks for this exchange, and the resource servers repeat it for a write-mode token
     * through {@code ownsTheStore}.
     */
    private static boolean actsIn(User target, String store) {
        Object own = target.getMetadata().get(STORE_METADATA);
        return own == null || own.toString().equals(store);
    }

    private Instant notAfter(Map<String, Object> subjectClaims) {
        Instant ceiling = clock.instant().plus(MAX_TTL);
        Object exp = subjectClaims.get(JwtClaimNames.EXP);
        Instant subjectExpiry = exp instanceof Instant instant ? instant : null;
        return subjectExpiry != null && subjectExpiry.isBefore(ceiling) ? subjectExpiry : ceiling;
    }

    private List<String> moderatorPermissions() {
        return roles.findByName(STORE_MODERATOR).map(Role::effectivePermissions).orElse(Set.of()).stream()
                .map(Permission::key).sorted().toList();
    }

    private static Set<String> scopesFor(RegisteredClient client, Set<String> requested) {
        Set<String> allowed = client.getScopes();
        if (requested.isEmpty()) {
            return new LinkedHashSet<>(allowed);
        }
        if (!allowed.containsAll(requested)) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_SCOPE);
        }
        return new LinkedHashSet<>(requested);
    }

    private OAuth2AccessTokenAuthenticationToken issue(RegisteredClient client, OAuth2ClientAuthenticationToken clientPrincipal,
                                                       ImpersonationExchangeAuthenticationToken request, User target,
                                                       ImpersonationContext context, Set<String> scopes) {
        Authentication principal = principalFor(target, context);
        OAuth2Authorization.Builder builder = OAuth2Authorization.withRegisteredClient(client)
                .principalName(target.getId().toString())
                .authorizationGrantType(AuthorizationGrantType.TOKEN_EXCHANGE)
                .authorizedScopes(scopes)
                .attribute(Principal.class.getName(), principal);
        context.writeTo(builder);
        DefaultOAuth2TokenContext.Builder tokenContext = DefaultOAuth2TokenContext.builder()
                .registeredClient(client)
                .principal(principal)
                .authorization(builder.build())
                .authorizedScopes(scopes)
                .tokenType(OAuth2TokenType.ACCESS_TOKEN)
                .authorizationGrantType(AuthorizationGrantType.TOKEN_EXCHANGE)
                .authorizationGrant(request);
        // Set by the endpoint's filter on every real request; the builder refuses a null, so it is only passed on
        // when present.
        AuthorizationServerContext serverContext = AuthorizationServerContextHolder.getContext();
        if (serverContext != null) {
            tokenContext.authorizationServerContext(serverContext);
        }
        OAuth2Token generated = tokenGenerator.generate(tokenContext.build());
        if (generated == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR,
                    "The token generator failed to generate the access token.", null));
        }
        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, generated.getTokenValue(),
                generated.getIssuedAt(), generated.getExpiresAt(), scopes);
        if (generated instanceof ClaimAccessor claims) {
            builder.token(accessToken, metadata -> metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME, claims.getClaims()));
        } else {
            builder.accessToken(accessToken);
        }
        // Deliberately no refresh token: an impersonation that can renew itself is one nobody can end.
        authorizations.save(builder.build());
        Map<String, Object> additional = new HashMap<>();
        additional.put(ISSUED_TOKEN_TYPE, ImpersonationExchangeConverter.ACCESS_TOKEN_TYPE);
        additional.put(ACT_MODE, context.mode().wire());
        return new OAuth2AccessTokenAuthenticationToken(client, clientPrincipal, accessToken, null, additional);
    }

    /**
     * The target as a principal, the shape {@code JpaUserDetailsService} would give it: the name is the account id,
     * which is what {@code JwtCustomizerConfig} resolves the claims by. The authorities are the roles the token will
     * carry, so a read-mode principal already looks like a moderator.
     */
    private static Authentication principalFor(User target, ImpersonationContext context) {
        List<String> names = context.overridesRoles() ? context.roles()
                : target.getRoles().stream().map(Role::getName).sorted().toList();
        Set<GrantedAuthority> authorities = names.stream()
                .map(name -> new SimpleGrantedAuthority(ROLE_PREFIX.concat(name)))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        org.springframework.security.core.userdetails.User details = new org.springframework.security.core.userdetails.User(
                target.getId().toString(), "", authorities);
        return UsernamePasswordAuthenticationToken.authenticated(details, null, authorities);
    }

    private Optional<User> account(String id) {
        try {
            return users.findById(UUID.fromString(id));
        } catch (IllegalArgumentException notAnId) {
            return Optional.empty();
        }
    }

    private static boolean holds(User user, Permission permission) {
        return user.getRoles().stream().anyMatch(role -> role.effectivePermissions().contains(permission));
    }

    private static boolean hasAnyRole(User user, Set<String> names) {
        return user.getRoles().stream().map(Role::getName).anyMatch(names::contains);
    }

    private static AuditActor actor(User operator) {
        return new AuditActor(AuditActorType.USER, operator.getId().toString(), operator.getUsername());
    }

    private OAuth2AuthenticationException deny(User operator, User target, ImpersonationExchangeAuthenticationToken request,
                                               Refusal refusal) {
        AuditRecord record = AuditRecord.of(AuditEventType.USER_IMPERSONATION_DENIED)
                .actor(actor(operator)).failed(refusal.name()).detail(request.getReason());
        if (target != null) {
            record.user(target.getId(), target.getUsername());
        } else {
            record.user(null, request.getRequestedSubject());
        }
        audit.recordDetached(record);
        log.warn("Refused {} acting as {} on store {}: {}", operator.getUsername(), request.getRequestedSubject(),
                request.getStore(), refusal);
        return new OAuth2AuthenticationException(new OAuth2Error(refusal.errorCode, refusal.description, null));
    }

    private static OAuth2AuthenticationException invalidRequest(String parameter) {
        return new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST,
                String.format("OAuth 2.0 parameter '%s' is missing, repeated or malformed.", parameter), null));
    }

    /** Why an exchange was refused — the audit row's reason code, and the error the caller sees. */
    enum Refusal {

        CHAINED(OAuth2ErrorCodes.INVALID_GRANT, "subject_token is itself an impersonation."),
        OPERATOR_NOT_ALLOWED(OAuth2ErrorCodes.ACCESS_DENIED, "The operator may not impersonate."),
        TARGET_UNKNOWN(OAuth2ErrorCodes.INVALID_REQUEST, "requested_subject is not an account."),
        TARGET_DISABLED(OAuth2ErrorCodes.ACCESS_DENIED, "The target account is disabled."),
        TARGET_PRIVILEGED(OAuth2ErrorCodes.ACCESS_DENIED, "A platform principal cannot be impersonated."),
        WRITE_NOT_ALLOWED(OAuth2ErrorCodes.ACCESS_DENIED, "This operator may act read-only."),
        STORE_NOT_TARGETS(OAuth2ErrorCodes.INVALID_REQUEST, "The target does not act in impersonation_store."),
        TARGET_NOT_READABLE(OAuth2ErrorCodes.ACCESS_DENIED, "The target holds no store-level read role.");

        private final String errorCode;

        private final String description;

        Refusal(String errorCode, String description) {
            this.errorCode = errorCode;
            this.description = description;
        }

    }

}
