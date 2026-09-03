package com.asrevo.cvhome.uaa.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.uaa.domain.IdentityProvider;
import com.asrevo.cvhome.uaa.domain.User;
import com.asrevo.cvhome.uaa.dto.LinkConfirmRequest;
import com.asrevo.cvhome.uaa.dto.LinkConfirmResponse;
import com.asrevo.cvhome.uaa.errors.LinkConfirmationInvalidException;
import com.asrevo.cvhome.uaa.idp.BrokerRefusedException;
import com.asrevo.cvhome.uaa.idp.IdentityBrokerService;
import com.asrevo.cvhome.uaa.idp.IdentityProviderService;
import com.asrevo.cvhome.uaa.idp.PendingLink;
import com.asrevo.cvhome.uaa.security.BrokeredLoginSuccessHandler;

import lombok.RequiredArgsConstructor;

/**
 * The password step of a brokered login that matched an existing account.
 *
 * <p>
 * On the application chain rather than the public one, so CSRF applies and the session the failure handler parked
 * the pending link in is the session that confirms it. The password goes through the same authentication manager as
 * the form — so a wrong one counts towards the lockout and a locked account is refused the same way — and a right one
 * links the identity and signs the session in exactly as the success handler would have.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/auth/link-confirm")
@RequiredArgsConstructor
public class LinkConfirmController {

    private static final String HOME = "/";

    private final AuthenticationConfiguration authentication;

    private final IdentityProviderService providers;

    private final IdentityBrokerService broker;

    private final BrokeredLoginSuccessHandler establish;

    private final RequestCache requestCache;

    @PostMapping
    public LinkConfirmResponse confirm(@Valid @RequestBody LinkConfirmRequest req, HttpServletRequest request,
                                       HttpServletResponse response) throws LinkConfirmationInvalidException {
        HttpSession session = request.getSession(false);
        Object parked = session == null ? null : session.getAttribute(PendingLink.SESSION_KEY);
        if (!(parked instanceof PendingLink pending)) {
            throw LinkConfirmationInvalidException.create();
        }
        try {
            authentication.getAuthenticationManager().authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(pending.username(), req.password()));
        } catch (AuthenticationException e) {
            throw LinkConfirmationInvalidException.create();
        } catch (Exception e) {
            throw new IllegalStateException("The authentication manager is not available", e);
        }
        IdentityProvider provider = providers.byId(pending.providerId()).orElseThrow(LinkConfirmationInvalidException::create);
        User user;
        try {
            user = broker.completeLink(pending, provider);
        } catch (BrokerRefusedException e) {
            throw LinkConfirmationInvalidException.create();
        }
        session.removeAttribute(PendingLink.SESSION_KEY);
        establish.establish(request, response, user.getUsername(), BrokeredLoginSuccessHandler.VIA_PREFIX + provider.getAlias());
        SavedRequest saved = requestCache.getRequest(request, response);
        String redirectTo = saved == null ? HOME : saved.getRedirectUrl();
        if (saved != null) {
            requestCache.removeRequest(request, response);
        }
        return new LinkConfirmResponse(user.getUsername(), redirectTo);
    }

}
