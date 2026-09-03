package com.asrevo.cvhome.sso.session;

import java.io.IOException;
import java.time.Instant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.filter.OncePerRequestFilter;

import com.asrevo.cvhome.sso.settings.SettingsService;

/**
 * The absolute session limit. Idle timeout is Spring Session's; this is the "however active you are" ceiling.
 */
public class SessionMaxAgeFilter extends OncePerRequestFilter {

    private final SettingsService settings;

    public SessionMaxAgeFilter(SettingsService settings) {
        this.settings = settings;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(SessionMetadata.CREATED_AT) instanceof Long created) {
            long maxMillis = settings.current().sessions().maxSeconds() * 1000L;
            if (Instant.now().toEpochMilli() - created > maxMillis) {
                session.invalidate();
            }
        }
        chain.doFilter(request, response);
    }

}
