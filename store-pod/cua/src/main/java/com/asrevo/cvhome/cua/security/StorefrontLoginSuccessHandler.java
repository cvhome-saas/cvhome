package com.asrevo.cvhome.cua.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.web.savedrequest.RequestCache;

import com.asrevo.cvhome.sso.security.LoginSuccessHandler;
import com.asrevo.cvhome.sso.session.SessionAdminService;
import com.asrevo.cvhome.sso.settings.SettingsService;

/**
 * A password sign-in, finished the storefront's way.
 *
 * <p>
 * It extends the shared handler rather than Spring's, and that is the whole fix: everything the shared one does
 * to a session at sign-in — stamp it with the address, the browser, how they signed in, when, and which realm;
 * take the realm's idle timeout; end the account's other sessions where the realm asks for one — cua was doing
 * for a brokered login and not for a password login. The merchant's session list said "address not recorded" for
 * every row because nothing had ever recorded one, and {@code SessionRealmFilter} could not protect a session
 * that carried no realm stamp.
 * </p>
 *
 * <p>
 * What is cua's own is only where the browser goes next. The saved authorize request resumes as it always did;
 * this decides the target when there is none — which happens when the session expired between the hand-off and
 * the POST, or when someone posts to {@code /cua/login} having never been sent there. cua's own root is a 404 the
 * shopper cannot act on; the storefront's login page without the pending marker is better, because the storefront
 * starts a fresh authorize and the session cua just created carries them through it.
 * </p>
 */
public class StorefrontLoginSuccessHandler extends LoginSuccessHandler {

    private final RequestCache requestCache;

    public StorefrontLoginSuccessHandler(SettingsService settings, SessionAdminService sessions,
                                         RequestCache requestCache) {
        super(settings, sessions, requestCache);
        this.requestCache = requestCache;
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
        return StorefrontUrls.loginPage(request, response, requestCache, false, null);
    }

}
