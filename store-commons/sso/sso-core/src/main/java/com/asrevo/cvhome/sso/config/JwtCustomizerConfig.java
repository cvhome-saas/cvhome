package com.asrevo.cvhome.sso.config;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import com.asrevo.cvhome.commons.domain.Permission;
import com.asrevo.cvhome.sso.domain.Role;
import com.asrevo.cvhome.sso.domain.User;
import com.asrevo.cvhome.sso.keys.KeyRotationService;
import com.asrevo.cvhome.sso.realm.SsoRealmProperties;
import com.asrevo.cvhome.sso.realm.SsoTenantIdentifierResolver;
import com.asrevo.cvhome.sso.security.PrincipalNames;
import com.asrevo.cvhome.sso.settings.SettingsService;
import com.asrevo.cvhome.sso.token.ImpersonationContext;

/**
 * What uaa puts into the tokens it mints.
 *
 * <p>
 * <strong>Access tokens</strong> carry {@code roles}, {@code permissions} (the effective set over those roles),
 * {@code uid} and the two tenancy claims — {@code org} and
 * {@code store} — copied from user metadata. Only those two: metadata is an open bag any {@code super_admin} caller can
 * write, and copying it whole used to let a key named {@code roles} or {@code scope} overwrite the real claim, since
 * the bag was written after them. The allow-list is the fix; {@code roles} is also written last so nothing can follow
 * it.
 * </p>
 *
 * <p>
 * A registered client's <em>custom</em> settings become claims only under the {@code cvhome.} prefix, plus the one
 * legacy key {@code resource} that the pods read to check a service token belongs to their pod.
 * </p>
 *
 * <p>
 * <strong>An access token never outlives the realm's ceiling.</strong> A client's own lifetime is validated at
 * registration, but the setting can be lowered afterwards; clamping {@code exp} here is what makes the new ceiling
 * apply to every client at the next token rather than only to the ones re-saved.
 * </p>
 *
 * <p>
 * <strong>An impersonated token says so.</strong> When the authorization behind the token is an impersonation
 * ({@link ImpersonationContext}), {@code act} names the operator (RFC 8693 §4.1: identity only) and {@code act_mode}
 * says read or write; a read-mode token has its {@code roles}, {@code permissions} and {@code store} replaced by the
 * ones the exchange decided on, and no impersonated token outlives the operator's own. This is the one branch that
 * runs after {@code roles} — deliberately, and it is the only thing that may.
 * </p>
 *
 * <p>
 * <strong>ID tokens</strong> get the standard profile claims, which the gateway's OIDC principal exposes to console-ui.
 * </p>
 */
@Configuration
public class JwtCustomizerConfig {

    static final String STORE = "store";

    /** The user-metadata keys that may become claims. */
    static final Set<String> METADATA_CLAIMS = Set.of("org", STORE);

    /** The client-setting keys that may become claims, beyond the {@code cvhome.} prefix. */
    static final Set<String> CLIENT_SETTING_CLAIMS = Set.of("resource");

    static final String CLIENT_SETTING_PREFIX = "cvhome.";

    static final String ROLES = "roles";

    static final String UID = "uid";

    static final String PERMISSIONS = "permissions";

    /** The store a shopper's token belongs to. */
    static final String REALM = "realm";

    /** The same value, under the name resource servers already read. */
    static final String CLIENT_ID = "clientId";

    /** The operator behind an impersonated token — RFC 8693 §4.1. */
    static final String ACT = "act";

    /** Whether an impersonated token acts read-only or as the target. */
    static final String ACT_MODE = "act_mode";

    private final PrincipalNames principals;

    private final SettingsService settings;

    private final KeyRotationService keys;

    private final Clock clock;

    private final SsoRealmProperties realmProperties;

    private final SsoTenantIdentifierResolver realms;

    public JwtCustomizerConfig(PrincipalNames principals, SettingsService settings, KeyRotationService keys,
                               Clock clock, SsoRealmProperties realmProperties, SsoTenantIdentifierResolver realms) {
        this.principals = principals;
        this.settings = settings;
        this.keys = keys;
        this.clock = clock;
        this.realmProperties = realmProperties;
        this.realms = realms;
    }

    @Bean
    OAuth2TokenCustomizer<JwtEncodingContext> oauth2TokenCustomizer() {
        return context -> {
            // The active key's kid on every header: with it the encoder selects one key even while a retiring key
            // shares the algorithm, and a verifier that meets an unknown kid knows to refetch the JWKS.
            context.getJwsHeader().keyId(keys.activeKid());
            if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                clampLifetime(context);
                addClientSettingClaims(context);
                addUserClaims(context, false);
                addImpersonationClaims(context);
            } else if (OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())) {
                addUserClaims(context, true);
            }
        };
    }

    private void clampLifetime(JwtEncodingContext context) {
        int maxSeconds = settings.current().tokens().maxAccessTokenTtlSeconds();
        Duration clientTtl = context.getRegisteredClient().getTokenSettings().getAccessTokenTimeToLive();
        if (maxSeconds > 0 && clientTtl.compareTo(Duration.ofSeconds(maxSeconds)) > 0) {
            Instant now = clock.instant();
            context.getClaims().issuedAt(now).expiresAt(now.plusSeconds(maxSeconds));
        }
    }

    private static void addClientSettingClaims(JwtEncodingContext context) {
        context.getRegisteredClient().getClientSettings().getSettings().forEach((key, value) -> {
            if (CLIENT_SETTING_CLAIMS.contains(key) || key.startsWith(CLIENT_SETTING_PREFIX)) {
                context.getClaims().claim(key, value);
            }
        });
    }

    private void addUserClaims(JwtEncodingContext context, boolean profile) {
        Authentication principal = context.getPrincipal();
        if (principal == null) {
            return;
        }
        // By id: the principal name is the account id, and on cua a username matches in every realm at once.
        principals.account(principal.getName()).ifPresent(user -> {
            context.getClaims().claim(UID, user.getId().toString());
            addRealmClaims(context, user);
            if (profile) {
                addProfileClaims(context, user);
            } else {
                user.getMetadata().forEach((key, value) -> {
                    if (METADATA_CLAIMS.contains(key) && value != null) {
                        context.getClaims().claim(key, value);
                    }
                });
            }
            // Last on purpose: nothing written after this line can shadow it, except addImpersonationClaims, which
            // is the one deliberate exception and replaces it with a narrower set. A plain List, not a Set: the
            // authorization store serialises claim values with type information, and only the JDK's common
            // collections are on its allow-list — a TreeSet here broke the gateway's UserInfo call.
            List<String> permissions = user.getRoles().stream().flatMap(r -> r.effectivePermissions().stream())
                    .map(Permission::key).distinct().sorted().toList();
            if (!permissions.isEmpty()) {
                context.getClaims().claim(PERMISSIONS, new ArrayList<>(permissions));
            }
            List<String> roles = Stream.concat(user.getRoles().stream().map(Role::getName),
                    realmProperties.getDefaultRoles().stream()).distinct().sorted().toList();
            if (!roles.isEmpty()) {
                context.getClaims().claim(ROLES, new ArrayList<>(roles));
            }
        });
    }

    /**
     * The impersonation claims, and the read-mode override.
     *
     * <p>
     * Runs after {@link #addUserClaims} on purpose: the user claims describe the target as they are, and this narrows
     * them to what the exchange decided. {@code exp} is pulled back to the exchange's ceiling — the operator's own
     * token's expiry or fifteen minutes — because the generator set it from the client's lifetime, which knows
     * nothing about either.
     * </p>
     */
    private static void addImpersonationClaims(JwtEncodingContext context) {
        ImpersonationContext.from(context.getAuthorization()).ifPresent(impersonation -> {
            Map<String, Object> act = new LinkedHashMap<>();
            act.put(JwtClaimNames.SUB, impersonation.operatorUsername());
            act.put(UID, impersonation.operatorId().toString());
            context.getClaims().claim(ACT, act).claim(ACT_MODE, impersonation.mode().wire());
            context.getClaims().claims(claims -> {
                Object exp = claims.get(JwtClaimNames.EXP);
                if (!(exp instanceof Instant expiresAt) || expiresAt.isAfter(impersonation.notAfter())) {
                    claims.put(JwtClaimNames.EXP, impersonation.notAfter());
                }
            });
            if (impersonation.overridesRoles()) {
                context.getClaims().claim(STORE, impersonation.store())
                        .claim(PERMISSIONS, new ArrayList<>(impersonation.permissions()))
                        .claim(ROLES, new ArrayList<>(impersonation.roles()));
            }
        });
    }

    /**
     * What a multi-realm token has to say about which store it belongs to.
     *
     * <p>
     * Both claims exist because resource servers already read {@code clientId} — {@code StoreRoleAccessChecker}
     * matches it against the {@code ?store=} of the request — and {@code realm} is what that check should read
     * once every service has been moved over. They carry the same value.
     * </p>
     *
     * <p>
     * The subject becomes the account id rather than the username. A username is unique within a realm and
     * nowhere else, so with a realm per store four demo shoppers are all called {@code user}; checkout joins its
     * own customer records on {@code sub}, and would have merged them. In a single-realm deployment the username
     * is already unique and the gateway's OIDC client pins {@code user-name-attribute: sub}, so uaa keeps it.
     * </p>
     */
    private void addRealmClaims(JwtEncodingContext context, User user) {
        if (realmProperties.single()) {
            return;
        }
        String realm = realms.resolveCurrentTenantIdentifier();
        context.getClaims().claim(REALM, realm);
        context.getClaims().subject(user.getId().toString());
    }

    private static void addProfileClaims(JwtEncodingContext context, User user) {
        if (user.getEmail() != null) {
            context.getClaims().claim("email", user.getEmail());
        }
        if (user.getFirstName() != null) {
            context.getClaims().claim("given_name", user.getFirstName());
        }
        if (user.getLastName() != null) {
            context.getClaims().claim("family_name", user.getLastName());
        }
        String name = String.join(" ", nullToEmpty(user.getFirstName()), nullToEmpty(user.getLastName())).trim();
        context.getClaims().claim("name", name.isEmpty() ? user.getUsername() : name);
        context.getClaims().claim("preferred_username", user.getUsername());
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

}
