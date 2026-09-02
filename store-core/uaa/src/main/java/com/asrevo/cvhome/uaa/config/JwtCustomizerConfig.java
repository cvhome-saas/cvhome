package com.asrevo.cvhome.uaa.config;

import java.util.Set;
import java.util.TreeSet;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import com.asrevo.cvhome.uaa.domain.Role;
import com.asrevo.cvhome.uaa.domain.User;
import com.asrevo.cvhome.uaa.repo.UserRepository;

/**
 * What uaa puts into the tokens it mints.
 *
 * <p>
 * <strong>Access tokens</strong> carry {@code roles}, {@code uid} and the two tenancy claims — {@code org} and
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
 * <strong>ID tokens</strong> get the standard profile claims, which the gateway's OIDC principal exposes to console-ui.
 * </p>
 */
@Configuration
public class JwtCustomizerConfig {

    /** The user-metadata keys that may become claims. */
    static final Set<String> METADATA_CLAIMS = Set.of("org", "store");

    /** The client-setting keys that may become claims, beyond the {@code cvhome.} prefix. */
    static final Set<String> CLIENT_SETTING_CLAIMS = Set.of("resource");

    static final String CLIENT_SETTING_PREFIX = "cvhome.";

    static final String ROLES = "roles";

    static final String UID = "uid";

    private final UserRepository userRepository;

    public JwtCustomizerConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    OAuth2TokenCustomizer<JwtEncodingContext> oauth2TokenCustomizer() {
        return context -> {
            if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                addClientSettingClaims(context);
                addUserClaims(context, false);
            } else if (OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())) {
                addUserClaims(context, true);
            }
        };
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
        userRepository.findByUsername(principal.getName()).ifPresent(user -> {
            context.getClaims().claim(UID, user.getId().toString());
            if (profile) {
                addProfileClaims(context, user);
            } else {
                user.getMetadata().forEach((key, value) -> {
                    if (METADATA_CLAIMS.contains(key) && value != null) {
                        context.getClaims().claim(key, value);
                    }
                });
            }
            // Last on purpose: nothing written after this line can shadow it.
            Set<String> roles = new TreeSet<>();
            user.getRoles().stream().map(Role::getName).forEach(roles::add);
            if (!roles.isEmpty()) {
                context.getClaims().claim(ROLES, roles);
            }
        });
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
