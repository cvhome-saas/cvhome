package com.asrevo.cvhome.uaa.idp;

import java.util.Arrays;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.asrevo.cvhome.uaa.domain.IdentityProvider;
import com.asrevo.cvhome.uaa.domain.IdpType;
import com.asrevo.cvhome.uaa.errors.IdpConfigInvalidException;

import lombok.RequiredArgsConstructor;

/**
 * A stored provider as Spring's OAuth2 client sees it.
 *
 * <p>
 * Endpoints given by hand win; a generic OIDC provider with only an issuer is discovered from it — a network call,
 * which is why the registration repository caches what this makes. The redirect URI is the template Spring expands on
 * uaa's own origin: {@code {baseUrl}/login/oauth2/code/{alias}}.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class ClientRegistrationFactory {

    static final String REDIRECT_TEMPLATE = "{baseUrl}/login/oauth2/code/{registrationId}";

    private static final String OPENID = "openid";

    private final IdentityProviderMapper mapper;

    public ClientRegistration build(IdentityProvider p) throws IdpConfigInvalidException {
        ClientRegistration.Builder builder = hasEndpoints(p) ? manual(p) : discovered(p);
        String secret = mapper.clientSecret(p);
        builder.clientId(mapper.clientId(p))
                .clientName(p.getDisplayName())
                .redirectUri(REDIRECT_TEMPLATE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .clientAuthenticationMethod(new ClientAuthenticationMethod(p.getClientAuthMethod()))
                .scope(IdentityProviderMapper.scopes(p).toArray(String[]::new));
        if (StringUtils.hasText(secret)) {
            builder.clientSecret(secret);
        }
        return builder.build();
    }

    private static boolean hasEndpoints(IdentityProvider p) {
        return StringUtils.hasText(p.getAuthorizationUri()) && StringUtils.hasText(p.getTokenUri());
    }

    private static ClientRegistration.Builder manual(IdentityProvider p) throws IdpConfigInvalidException {
        if (p.getType() == IdpType.OIDC && !StringUtils.hasText(p.getJwkSetUri())) {
            throw IdpConfigInvalidException.of("jwkSetUri", "An OpenID Connect provider needs a JWK Set URI to verify id tokens.");
        }
        if (p.getType() == IdpType.OAUTH2 && !StringUtils.hasText(p.getUserInfoUri())) {
            throw IdpConfigInvalidException.of("userInfoUri", "An OAuth2 provider needs a user-info endpoint.");
        }
        ClientRegistration.Builder builder = ClientRegistration.withRegistrationId(p.getAlias())
                .authorizationUri(p.getAuthorizationUri())
                .tokenUri(p.getTokenUri())
                .userNameAttributeName(StringUtils.hasText(p.getUserNameAttribute()) ? p.getUserNameAttribute() : "sub");
        if (StringUtils.hasText(p.getUserInfoUri())) {
            builder.userInfoUri(p.getUserInfoUri());
        }
        if (StringUtils.hasText(p.getJwkSetUri())) {
            builder.jwkSetUri(p.getJwkSetUri());
        }
        if (StringUtils.hasText(p.getIssuerUri())) {
            builder.issuerUri(p.getIssuerUri());
        }
        return builder;
    }

    private static ClientRegistration.Builder discovered(IdentityProvider p) throws IdpConfigInvalidException {
        if (p.getType() != IdpType.OIDC || !StringUtils.hasText(p.getIssuerUri())) {
            throw IdpConfigInvalidException.of("issuerUri",
                    "Give an issuer to discover from, or the authorization and token endpoints by hand.");
        }
        return ClientRegistrations.fromOidcIssuerLocation(p.getIssuerUri()).registrationId(p.getAlias());
    }

    /** Whether the provider asks for an id token at all. */
    static boolean requestsOpenId(IdentityProvider p) {
        return Arrays.asList(p.getScopes() == null ? new String[0] : p.getScopes().split(" ")).contains(OPENID);
    }

}
