package com.asrevo.cvhome.cua.security;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.RequestCache;

import com.asrevo.cvhome.sso.idp.PendingLink;
import com.asrevo.cvhome.sso.security.BrokeredLoginSuccessHandler;
import com.asrevo.cvhome.sso.security.BrokeredPrincipal;

/**
 * A brokered sign-in, finished the storefront's way.
 *
 * <p>
 * Two things have to happen and neither stock handler does both. The principal has to stop being a
 * {@link BrokeredPrincipal}: the OAuth2 login filter authenticates one, but the authorization server writes the
 * principal into {@code oauth2_authorization} as JSON, and its Jackson allow-list has no idea what a
 * {@code BrokeredPrincipal} is. Left in place it serialises fine and then fails to <em>read back</em> — the
 * shopper signs in, the code is issued, and the token exchange answers 500 with "could not resolve type id". The
 * swap to the {@code UsernamePasswordAuthenticationToken} a password login produces is
 * {@link BrokeredLoginSuccessHandler#establish}, which also stamps the session, applies the realm's policy and
 * audits the login.
 * </p>
 *
 * <p>
 * Which is why this extends the plain Spring handler and not {@link StorefrontLoginSuccessHandler}: that one
 * stamps the session as a password sign-in, and doing it after {@code establish} would overwrite "signed in with
 * github" with "signed in with a password". The two share only where the browser goes next.
 * </p>
 */
public class StorefrontBrokeredLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final RequestCache requestCache;

    private final BrokeredLoginSuccessHandler brokered;

    public StorefrontBrokeredLoginSuccessHandler(RequestCache requestCache, BrokeredLoginSuccessHandler brokered) {
        this.requestCache = requestCache;
        this.brokered = brokered;
        setRequestCache(requestCache);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {
        String via = authentication.getPrincipal() instanceof BrokeredPrincipal principal
                ? BrokeredLoginSuccessHandler.VIA_PREFIX + principal.providerAlias()
                : BrokeredLoginSuccessHandler.VIA_PREFIX;
        Authentication local = brokered.establish(request, response, authentication.getName(), via);
        request.getSession().removeAttribute(PendingLink.SESSION_KEY);
        super.onAuthenticationSuccess(request, response, local);
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
        return StorefrontUrls.loginPage(request, response, requestCache, false, null);
    }

}
