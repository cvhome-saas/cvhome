package com.asrevo.cvhome.cua.security;

import java.util.Objects;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.web.util.UriComponentsBuilder;

import com.asrevo.cvhome.commons.domain.LanguageCode;

/**
 * Where cua sends a shopper's browser: always the storefront, never a page of its own.
 *
 * <p>
 * cua renders no HTML. The storefront (landing-ui) owns the login and registration pages, themed per store, and
 * cua's only job in the browser is to hand the shopper over to them and take them back once the form has been
 * posted to {@code /cua/login}. The two are same-origin behind spg — {@code /cua*} routes here, everything else to
 * the storefront — so the hand-off is a plain redirect on the request's own host, exactly the way the dynamic
 * client already derives its {@code redirect_uri}.
 * </p>
 *
 * <p>
 * The language survives here as a path segment only. Storefront routes are locale-prefixed ({@code /en/login},
 * {@code /ar/login}), so the redirect needs the shopper's locale; the strings on the page are the storefront's
 * business. It is read from the request first (the authorize request and the login POST both carry {@code lang}),
 * then from the saved request (a social-login callback carries neither), then defaults — and is validated as a
 * language code so a crafted parameter cannot become an arbitrary path.
 * </p>
 */
public final class StorefrontUrls {

    /** Tells the storefront that cua holds a saved authorize request, so it renders the form rather than starting one. */
    public static final String PENDING_PARAM = "auth";

    public static final String PENDING_VALUE = "1";

    public static final String ERROR_PARAM = "error";

    public static final String LANG_PARAM = "lang";

    private static final String LOGIN_PAGE = "login";

    private static final String HTTP = "http";

    private static final String HTTPS = "https";

    private static final int HTTP_PORT = 80;

    private static final int HTTPS_PORT = 443;

    private StorefrontUrls() {
    }

    /**
     * {@code scheme://host[:port]} of the request, port omitted when it is the scheme's default. Read from the
     * request rather than configured because the host is the store's own — a per-store subdomain or a merchant's
     * custom domain — and there is one cua for all of them. Behind spg the values come through
     * {@code X-Forwarded-*}, which is what keeps the port right on a shifted local stack.
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

    /** The storefront locale to send the shopper to: request param, then the saved request, then the default. */
    public static LanguageCode language(HttpServletRequest request, SavedRequest saved) {
        return fromParameter(request.getParameter(LANG_PARAM))
                .or(() -> Optional.ofNullable(saved)
                        .map(it -> it.getParameterValues(LANG_PARAM))
                        .filter(values -> values.length > 0)
                        .flatMap(values -> fromParameter(values[0])))
                .orElseGet(LanguageCode::defaultLanguage);
    }

    /**
     * The storefront's login page for this request. {@code pending} adds the marker that tells the storefront to
     * render the form; {@code error} is a machine token the storefront turns into a message in its own language.
     */
    public static String loginPage(HttpServletRequest request, HttpServletResponse response, RequestCache cache,
                                   boolean pending, String error) {
        SavedRequest saved = cache.getRequest(request, response);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(origin(request))
                .pathSegment(language(request, saved).code(), LOGIN_PAGE);
        if (pending) {
            builder.queryParam(PENDING_PARAM, PENDING_VALUE);
        }
        if (Objects.nonNull(error)) {
            builder.queryParam(ERROR_PARAM, error);
        }
        return builder.build().toUriString();
    }

    private static Optional<LanguageCode> fromParameter(String value) {
        return Optional.ofNullable(value).map(LanguageCode::new).filter(it -> it.isLanguage());
    }

}
