package com.asrevo.cvhome.uaa.invitation;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * The public pages a one-time token lands on, on uaa's own origin.
 *
 * <p>
 * Built on the pinned issuer rather than on the request that issued the link: an administrator working through the
 * gateway must not produce a link that points at the gateway's host, where the page does not exist.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class LinkBuilder {

    static final String ACCEPT_INVITATION = "/accept-invitation";

    static final String RESET_PASSWORD = "/reset-password";

    private final AuthorizationServerSettings settings;

    public String invitation(String token) {
        return link(ACCEPT_INVITATION, token);
    }

    public String passwordReset(String token) {
        return link(RESET_PASSWORD, token);
    }

    private String link(String path, String token) {
        return String.format("%s%s?token=%s", settings.getIssuer(), path, URLEncoder.encode(token, StandardCharsets.UTF_8));
    }

}
