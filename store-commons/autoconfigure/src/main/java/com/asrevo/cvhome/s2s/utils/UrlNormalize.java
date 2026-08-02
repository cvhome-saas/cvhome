package com.asrevo.cvhome.s2s.utils;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.security.oauth2.jwt.JwtException;

public class UrlNormalize {

    private UrlNormalize() {
    }

    public static String normalizeUri(String uriString) throws JwtException {
        if (uriString == null || uriString.trim().isEmpty()) {
            throw new JwtException("URI string cannot be null or empty for normalization.");
        }
        try {
            URI uri = new URI(uriString);

            String scheme = uri.getScheme();
            String host = uri.getHost();
            int port = uri.getPort();

            if (scheme == null || host == null) {
                throw new JwtException(String.format("Issuer URI must include scheme and host: %s", uriString));
            }

            scheme = scheme.toLowerCase();
            host = host.toLowerCase();

            if (("http".equals(scheme) && port == 80) || ("https".equals(scheme) && port == 443)) {
                port = -1;
            }

            return new URI(scheme, uri.getUserInfo(), host, port, uri.getPath(), uri.getQuery(), uri.getFragment())
                    .normalize()
                    .toString();

        } catch (URISyntaxException e) {
            throw new JwtException(String.format("Malformed issuer URI string: %s", uriString), e);
        }
    }

}
