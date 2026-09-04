package com.asrevo.cvhome.sso.invitation;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * The public pages a one-time token lands on.
 *
 * <p>
 * Built on configuration rather than on the request that issued the link: an administrator working through the
 * gateway must not produce a link that points at whatever host they happened to be on. The default is the pinned
 * issuer and uaa's own paths, which is what every deployment had before the seller console took these pages over;
 * {@link LinksProperties#baseUrl()} moves them, and nothing else changes — the token and its lifetime are the
 * same, and so is the public endpoint that redeems it.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class LinkBuilder {

    private final AuthorizationServerSettings settings;

    private final LinksProperties links;

    public String invitation(String token) {
        return link(links.invitationPage(), token);
    }

    public String passwordReset(String token) {
        return link(links.resetPasswordPage(), token);
    }

    private String link(String path, String token) {
        String origin = links.baseUrl().isEmpty() ? settings.getIssuer() : links.baseUrl();
        return String.format("%s%s?token=%s", origin, path, URLEncoder.encode(token, StandardCharsets.UTF_8));
    }

}
