package com.asrevo.cvhome.cua.web;

import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.asrevo.cvhome.cua.security.StorefrontUrls;
import com.asrevo.cvhome.sso.security.HandoffUrls;

import lombok.RequiredArgsConstructor;

/**
 * {@code GET /cua/login} used to be the login page. It is the storefront's now, so a stale bookmark or an old
 * link is sent there — marked pending when this session is mid-flow, so the form still works, and plain
 * otherwise, so the storefront starts a fresh one. {@code POST /login} is untouched: that is Spring's form-login
 * processing, and the form the storefront renders still posts to it.
 */
@Controller
@RequiredArgsConstructor
public class LoginRedirectController {

    private final RequestCache requestCache;

    private final CsrfTokenRepository csrfTokens;

    @GetMapping("/login")
    public String login(HttpServletRequest request, HttpServletResponse response) {
        HandoffUrls.plantCsrfCookie(csrfTokens, request, response);
        boolean pending = Objects.nonNull(requestCache.getRequest(request, response));
        return String.format("redirect:%s", StorefrontUrls.loginPage(request, response, requestCache, pending, null));
    }

}
