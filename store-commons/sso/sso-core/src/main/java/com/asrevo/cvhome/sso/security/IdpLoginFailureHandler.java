package com.asrevo.cvhome.sso.security;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.sso.audit.AuditActor;
import com.asrevo.cvhome.sso.audit.AuditEventType;
import com.asrevo.cvhome.sso.audit.AuditRecord;
import com.asrevo.cvhome.sso.audit.AuditService;
import com.asrevo.cvhome.sso.idp.BrokerRefusedException;
import com.asrevo.cvhome.sso.idp.PendingLink;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Where a brokered login that did not complete goes: back to the sign-in page with a code it can explain.
 *
 * <p>
 * {@code link_required} is not a failure to the person — it is the password step. The pending link is parked in the
 * session so the confirmation call can finish it; everything else is a refusal, audited as a failed login with the
 * broker's reason.
 * </p>
 *
 * <p>
 * <strong>The exception itself never goes into the session.</strong> Sessions here are Spring Session JDBC rows, so
 * every attribute is serialised — and an {@code OAuth2AuthenticationException} from a failed token or user-info call
 * holds the HTTP response that caused it, which is not serialisable. Saving it turned a refusal into a 500. The code
 * is what the sign-in page needs, and it travels in the query string.
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IdpLoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    static final String LOGIN = "/login";

    private static final String GENERIC = "idp";

    private final AuditService audit;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String code = GENERIC;
        if (exception instanceof BrokerRefusedException refused) {
            code = refused.code();
            if (refused.pending() != null) {
                request.getSession().setAttribute(PendingLink.SESSION_KEY, refused.pending());
            }
        } else if (exception instanceof OAuth2AuthenticationException oauth) {
            log.warn("Brokered login failed: {} {}", oauth.getError().getErrorCode(), oauth.getError().getDescription());
        }
        if (!BrokerRefusedException.LINK_REQUIRED.equals(code)) {
            audit.recordDetached(AuditRecord.of(AuditEventType.USER_LOGIN_FAILED).actor(AuditActor.ANONYMOUS)
                    .failed(code.toUpperCase(java.util.Locale.ROOT)).detail(exception.getMessage()));
        }
        getRedirectStrategy().sendRedirect(request, response, String.format("%s?error=%s", LOGIN, code));
    }

}
