package com.asrevo.cvhome.uaa.security;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.uaa.session.SessionAdminService;
import com.asrevo.cvhome.uaa.session.SessionMetadata;
import com.asrevo.cvhome.uaa.settings.RealmSettings;
import com.asrevo.cvhome.uaa.settings.SettingsService;

/**
 * What happens to the session the moment a sign-in succeeds, before the saved request resumes.
 *
 * <p>
 * The session learns who and where from (ip, user agent, how they signed in, when), takes the realm's idle
 * timeout, and — when the realm allows one session per person — every other session of that account is ended.
 * </p>
 */
@Component
public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final SettingsService settings;

    private final SessionAdminService sessions;

    public LoginSuccessHandler(SettingsService settings, SessionAdminService sessions, RequestCache requestCache) {
        this.settings = settings;
        this.sessions = sessions;
        setRequestCache(requestCache);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {
        RealmSettings.Sessions policy = settings.current().sessions();
        HttpSession session = request.getSession();
        SessionMetadata.stamp(session, request, LockoutService.VIA_PASSWORD);
        session.setMaxInactiveInterval(policy.idleSeconds());
        if (policy.singleSessionPerUser()) {
            sessions.revokeAll(authentication.getName(), session.getId());
        }
        super.onAuthenticationSuccess(request, response, authentication);
    }

}
