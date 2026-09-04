package com.asrevo.cvhome.sso.security;

import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.web.csrf.CsrfTokenRepository;

/**
 * The two things every hand-off needs, whichever page it hands off to.
 *
 * <p>
 * Both were written for cua first and are not cua's: an authorization server that does not render its own sign-in
 * page has to say which origin it was reached on, and has to plant the CSRF cookie itself, because the redirect is
 * the last response it sends before somebody else renders the form.
 * </p>
 */
public final class HandoffUrls {

    private static final String HTTP = "http";

    private static final String HTTPS = "https";

    private static final int HTTP_PORT = 80;

    private static final int HTTPS_PORT = 443;

    private HandoffUrls() {
    }

    /**
     * {@code scheme://host[:port]} of the request, port omitted when it is the scheme's default.
     *
     * <p>
     * Read from the request rather than configured, because neither deployment has a single answer: cua serves one
     * store per host across per-store subdomains and merchants' custom domains, and uaa is reached both on its own
     * host and through store-core-gateway on the console's. Behind a proxy the values arrive as {@code
     * X-Forwarded-*}, which is what keeps the port right on a shifted local stack.
     * </p>
     */
    public static String origin(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();

        StringBuilder origin = new StringBuilder();
        origin.append(scheme).append("://").append(serverName);
        if (HTTP.equals(scheme) && serverPort != HTTP_PORT || HTTPS.equals(scheme) && serverPort != HTTPS_PORT) {
            origin.append(":").append(serverPort);
        }
        return origin.toString();
    }

    /** Issues the CSRF cookie when the browser does not already hold one. */
    public static void plantCsrfCookie(CsrfTokenRepository csrfTokens, HttpServletRequest request,
                                       HttpServletResponse response) {
        if (Objects.isNull(csrfTokens.loadToken(request))) {
            csrfTokens.saveToken(csrfTokens.generateToken(request), request, response);
        }
    }

}
