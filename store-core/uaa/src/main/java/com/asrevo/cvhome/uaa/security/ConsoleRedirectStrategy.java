package com.asrevo.cvhome.uaa.security;

import java.io.IOException;
import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.RequiredArgsConstructor;

/**
 * Sends uaa's own sign-in redirects to the console instead, when the console is what is in front.
 *
 * <p>
 * This is the seam rather than a second set of handlers, and that is the point. {@code LoginFailureHandler} knows
 * things no other class does — that the attempt which crossed the lockout threshold is reported as the lock and
 * never as "0 attempts left", that a disabled account is told so plainly — and {@code IdpLoginFailureHandler}
 * knows the brokered vocabulary. Rewriting only the last step, the redirect itself, keeps every one of those
 * decisions in one place and gives the console the same query uaa's own page has always read: {@code ?error}
 * alone is a wrong password, {@code error=locked}, {@code error=disabled}, {@code error=expired-password},
 * {@code error=idp_*}, {@code attemptsLeft=N}, {@code ?logout}.
 * </p>
 *
 * <p>
 * Only two targets are rewritten. {@code /login} is the sign-in page, which the console owns; {@code /} is where a
 * successful sign-in lands when there is no saved request to resume, and on the console's origin uaa's own root is
 * not somewhere a merchant can use. Everything else — above all the absolute authorize URL a saved request
 * resumes to — passes through untouched.
 * </p>
 */
@RequiredArgsConstructor
public class ConsoleRedirectStrategy implements RedirectStrategy {

    private static final String LOGIN = "/login";

    private static final String ROOT = "/";

    private final RedirectStrategy delegate = new DefaultRedirectStrategy();

    private final ConsoleUrls console;

    @Override
    public void sendRedirect(HttpServletRequest request, HttpServletResponse response, String url) throws IOException {
        String rewritten = console.isHandoff(request) ? rewrite(request, url) : null;
        if (Objects.nonNull(rewritten)) {
            response.sendRedirect(rewritten);
            return;
        }
        delegate.sendRedirect(request, response, url);
    }

    /** The console's equivalent of a uaa page, or null when this target is not one of uaa's pages. */
    private String rewrite(HttpServletRequest request, String url) {
        if (Objects.isNull(url) || url.isEmpty() || url.contains("://")) {
            return null;
        }
        UriComponents parts = UriComponentsBuilder.fromUriString(url).build();
        String path = parts.getPath();
        if (LOGIN.equals(path)) {
            return console.signInWithQuery(request, parts.getQuery());
        }
        if (ROOT.equals(path)) {
            return console.signIn(request);
        }
        return null;
    }

}
