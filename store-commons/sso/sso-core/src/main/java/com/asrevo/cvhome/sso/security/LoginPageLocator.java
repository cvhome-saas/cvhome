package com.asrevo.cvhome.sso.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Where this deployment sends a browser that has to sign in.
 *
 * <p>
 * The one thing the two deployments genuinely disagree about at the edge. cua hands the shopper to their
 * storefront, which owns the themed page; uaa hands a merchant to the console, and renders its own page only for
 * whoever reached it on uaa's own host. Everything else about the hand-off — planting the CSRF cookie, leaving the
 * saved authorize request alone, turning a failure into a token rather than a message — is identical, and lives in
 * the handlers below rather than being written once per shell.
 * </p>
 *
 * <p>
 * {@code pending} says the server is holding a saved authorize request, so the page should render the form rather
 * than start a fresh flow; {@code error} is a machine token the page turns into a message in its own language, or
 * {@code null} when there is nothing to report.
 * </p>
 */
@FunctionalInterface
public interface LoginPageLocator {

    String loginPage(HttpServletRequest request, HttpServletResponse response, boolean pending, String error);

}
