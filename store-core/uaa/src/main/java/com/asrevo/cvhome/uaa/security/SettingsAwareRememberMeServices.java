package com.asrevo.cvhome.uaa.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;

import com.asrevo.cvhome.uaa.settings.SettingsService;

/**
 * Remember-me that the realm's settings can switch off.
 *
 * <p>
 * Wraps Spring's token-based services: when the setting is off no cookie is issued and an existing one is ignored,
 * so turning the feature off signs everyone out of it at their next request. The validity is read from the settings
 * on every sign-in; the token carries its own expiry, so a change applies to new cookies only.
 * </p>
 */
public class SettingsAwareRememberMeServices implements RememberMeServices {

    static final String PARAMETER = "remember-me";

    private final TokenBasedRememberMeServices delegate;

    private final SettingsService settings;

    public SettingsAwareRememberMeServices(String key, UserDetailsService userDetailsService, SettingsService settings) {
        this.delegate = new TokenBasedRememberMeServices(key, userDetailsService);
        this.delegate.setParameter(PARAMETER);
        this.delegate.setAlwaysRemember(false);
        this.settings = settings;
    }

    @Override
    public Authentication autoLogin(HttpServletRequest request, HttpServletResponse response) {
        if (!settings.current().sessions().rememberMeEnabled()) {
            return null;
        }
        return delegate.autoLogin(request, response);
    }

    @Override
    public void loginFail(HttpServletRequest request, HttpServletResponse response) {
        delegate.loginFail(request, response);
    }

    @Override
    public void loginSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        var policy = settings.current().sessions();
        if (!policy.rememberMeEnabled()) {
            return;
        }
        delegate.setTokenValiditySeconds(policy.rememberMeSeconds());
        delegate.loginSuccess(request, response, authentication);
    }

    public String getKey() {
        return delegate.getKey();
    }

}
