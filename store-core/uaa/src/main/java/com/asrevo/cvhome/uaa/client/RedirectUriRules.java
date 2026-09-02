package com.asrevo.cvhome.uaa.client;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.uaa.errors.InvalidRedirectUriException;

/**
 * What a redirect URI must look like before uaa will send an authorization code to it.
 *
 * <p>
 * The authorization server compares redirect URIs exactly, so a wildcard would never match anyway — but a registration
 * that carries one reads as if it worked. A fragment is forbidden by RFC 6749 §3.1.2. Plain {@code http} is allowed only
 * for the hosts the realm names, which locally are the stack's own; anywhere else a code in clear text on the wire is a
 * code an on-path attacker can redeem.
 * </p>
 */
@Component
public class RedirectUriRules {

    static final String NOT_ABSOLUTE = "NOT_ABSOLUTE";

    static final String FRAGMENT = "FRAGMENT";

    static final String WILDCARD = "WILDCARD";

    static final String PLAIN_HTTP = "PLAIN_HTTP";

    private static final String HTTP = "http";

    private static final String HTTPS = "https";

    private final List<String> plainHttpHosts;

    public RedirectUriRules(ClientsProperties properties) {
        this.plainHttpHosts = properties.plainHttpHosts().stream().map(h -> h.toLowerCase(Locale.ROOT)).toList();
    }

    public void validate(Collection<String> uris) throws InvalidRedirectUriException {
        if (uris == null) {
            return;
        }
        for (String uri : uris) {
            validateOne(uri);
        }
    }

    private void validateOne(String raw) throws InvalidRedirectUriException {
        if (raw.contains("*")) {
            throw InvalidRedirectUriException.of(raw, WILDCARD);
        }
        URI uri;
        try {
            uri = new URI(raw);
        } catch (URISyntaxException e) {
            throw InvalidRedirectUriException.of(raw, NOT_ABSOLUTE);
        }
        if (!uri.isAbsolute() || uri.getHost() == null && !isCustomScheme(uri)) {
            throw InvalidRedirectUriException.of(raw, NOT_ABSOLUTE);
        }
        if (uri.getFragment() != null) {
            throw InvalidRedirectUriException.of(raw, FRAGMENT);
        }
        String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
        if (HTTP.equals(scheme) && !plainHttpAllowed(uri.getHost())) {
            throw InvalidRedirectUriException.of(raw, PLAIN_HTTP);
        }
    }

    /** A native app's {@code com.example.app:/callback} has no host and is neither http nor https. */
    private static boolean isCustomScheme(URI uri) {
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        return !HTTP.equals(scheme) && !HTTPS.equals(scheme);
    }

    boolean plainHttpAllowed(String host) {
        String candidate = host.toLowerCase(Locale.ROOT);
        for (String allowed : plainHttpHosts) {
            if (allowed.startsWith(".") ? candidate.endsWith(allowed) : candidate.equals(allowed)) {
                return true;
            }
        }
        return false;
    }

}
