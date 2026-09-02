package com.asrevo.cvhome.cua.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.RequestCache;

/**
 * After a successful sign-in the saved authorize request resumes, as it always did; this only decides where to go
 * when there is none.
 *
 * <p>
 * That happens when the cua session expired between the hand-off and the POST, or when someone posts to
 * {@code /cua/login} without ever having been sent there. The default target, cua's own root, is a 404 the shopper
 * cannot act on. The storefront's login page without the pending marker is better: the storefront starts a fresh
 * authorize, and the session cua just created carries them through it.
 * </p>
 */
public class StorefrontLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final RequestCache requestCache;

    public StorefrontLoginSuccessHandler(RequestCache requestCache) {
        this.requestCache = requestCache;
        setRequestCache(requestCache);
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
        return StorefrontUrls.loginPage(request, response, requestCache, false, null);
    }

}
