package com.asrevo.cvhome.uaa.web.pub;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.uaa.dto.LoginContext;
import com.asrevo.cvhome.uaa.idp.PendingLink;
import com.asrevo.cvhome.uaa.settings.RealmSettings;
import com.asrevo.cvhome.uaa.settings.SettingsService;

import lombok.RequiredArgsConstructor;

/**
 * What the sign-in page needs before anyone is signed in. Public and stateless; carries nothing a stranger should
 * not see — the display name, whether remember-me is offered, and the lockout policy the page explains.
 */
@RestController
@RequestMapping("/api/v1/public/login")
@RequiredArgsConstructor
public class PublicLoginController {

    private final SettingsService settings;

    private final RequestCache requestCache;

    private final RegisteredClientRepository clients;

    @GetMapping("settings")
    public LoginSettings settings() {
        RealmSettings current = settings.current();
        return new LoginSettings(current.displayName(), current.defaultLocale(), current.sessions().rememberMeEnabled(),
                current.lockout().threshold(), current.lockout().durationSeconds() / 60);
    }

    /**
     * Why the sign-in page is being shown: the client whose authorization is saved in the session, and the brokered
     * login waiting for a password, if any. Reads the session it finds and never creates one.
     */
    @GetMapping("context")
    public LoginContext context(HttpServletRequest request, HttpServletResponse response) {
        String clientId = null;
        String clientName = null;
        SavedRequest saved = requestCache.getRequest(request, response);
        if (saved != null) {
            String[] values = saved.getParameterValues(OAuth2ParameterNames.CLIENT_ID);
            if (values != null && values.length > 0) {
                clientId = values[0];
                RegisteredClient client = clients.findByClientId(clientId);
                clientName = client == null ? null : client.getClientName();
            }
        }
        HttpSession session = request.getSession(false);
        Object pending = session == null ? null : session.getAttribute(PendingLink.SESSION_KEY);
        LoginContext.PendingLinkView link = pending instanceof PendingLink p
                ? new LoginContext.PendingLinkView(p.providerAlias(), p.providerName(), p.email()) : null;
        return new LoginContext(clientId, clientName, link);
    }

    public record LoginSettings(String displayName, String defaultLocale, boolean rememberMeEnabled,
                                int lockoutThreshold, int lockoutMinutes) {
    }

}
