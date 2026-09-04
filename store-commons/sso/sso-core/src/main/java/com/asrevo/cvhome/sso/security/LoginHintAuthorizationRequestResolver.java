package com.asrevo.cvhome.sso.security;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.util.StringUtils;

import com.asrevo.cvhome.sso.idp.IdentityProviderService;
import com.asrevo.cvhome.sso.idp.IdpPreset;

/**
 * What uaa adds to the request it sends a provider: PKCE (every provider but Apple), the {@code login_hint} the
 * sign-in page learned from the email step, and Apple's {@code response_mode=form_post}.
 */
public class LoginHintAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    static final String LOGIN_HINT = "login_hint";

    static final String RESPONSE_MODE = "response_mode";

    static final String FORM_POST = "form_post";

    private final DefaultOAuth2AuthorizationRequestResolver pkce;

    private final DefaultOAuth2AuthorizationRequestResolver plain;

    private final IdentityProviderService providers;

    public LoginHintAuthorizationRequestResolver(ClientRegistrationRepository registrations, IdentityProviderService providers) {
        String base = OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI;
        this.pkce = new DefaultOAuth2AuthorizationRequestResolver(registrations, base);
        this.pkce.setAuthorizationRequestCustomizer(OAuth2AuthorizationRequestCustomizers.withPkce());
        this.plain = new DefaultOAuth2AuthorizationRequestResolver(registrations, base);
        // Explicitly customizer-free: since Spring Security 7 the default resolver applies PKCE itself, so a
        // resolver that merely omits `withPkce()` is no longer plain and Apple was being sent a code_challenge.
        this.plain.setAuthorizationRequestCustomizer(builder -> {
        });
        this.providers = providers;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest resolved = pkce.resolve(request);
        return resolved == null ? null : customize(request, resolved);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest resolved = pkce.resolve(request, clientRegistrationId);
        return resolved == null ? null : customize(request, resolved);
    }

    private OAuth2AuthorizationRequest customize(HttpServletRequest request, OAuth2AuthorizationRequest resolved) {
        String alias = resolved.getAttribute(org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.REGISTRATION_ID);
        boolean apple = alias != null && providers.byAlias(alias).map(p -> p.getPreset() == IdpPreset.APPLE).orElse(false);
        OAuth2AuthorizationRequest base = apple ? plain.resolve(request, alias) : resolved;
        if (base == null) {
            return null;
        }
        Map<String, Object> extra = new HashMap<>(base.getAdditionalParameters());
        String hint = request.getParameter(LOGIN_HINT);
        if (StringUtils.hasText(hint)) {
            extra.put(LOGIN_HINT, hint);
        }
        if (apple) {
            extra.put(RESPONSE_MODE, FORM_POST);
        }
        return OAuth2AuthorizationRequest.from(base).additionalParameters(extra).build();
    }

}
