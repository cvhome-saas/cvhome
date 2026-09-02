package com.asrevo.cvhome.cua.security;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Where an unauthenticated shopper is sent: the storefront's login page, with the marker that says cua is holding
 * their authorize request.
 *
 * <p>
 * Replaces {@code LoginUrlAuthenticationEntryPoint("/login")}, which resolved against cua's own context path and
 * landed on a Thymeleaf page nothing could theme. The saved request has already been stored by the time this runs,
 * so once the storefront posts the credentials back to {@code /cua/login} the flow resumes where it left off.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class StorefrontLoginEntryPoint implements AuthenticationEntryPoint {

    private final RequestCache requestCache;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException {
        response.sendRedirect(StorefrontUrls.loginPage(request, response, requestCache, true, null));
    }

}
