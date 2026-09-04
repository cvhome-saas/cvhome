package com.asrevo.cvhome.sso.config;

/**
 * Where this deployment's tokens say they came from. One of the two seams a shell fills.
 *
 * <p>
 * Pinning is not optional and it is not a preference. Left unset, Spring Authorization Server derives the issuer
 * from each request's {@code Host} — and with forwarded headers honoured, from whatever a proxy claims — so a
 * token's {@code iss} depends on the path the request took. Resource servers trust a fixed list of issuers, so
 * the result is tokens that verify from one entry point and are rejected from another.
 * </p>
 *
 * <p>
 * It matters most for cua, whose browser traffic arrives same-origin on an unbounded set of merchant domains.
 * There the issuer is one value per pod, never the request host, because no trust list could enumerate the
 * hosts. An implementation that cannot determine its issuer must throw rather than return a guess: a server that
 * mints tokens nothing accepts is worse than one that refuses to start.
 * </p>
 */
public interface IssuerPin {

    /**
     * The issuer, already normalized (see {@code UrlNormalize}) so that {@code https://host} and
     * {@code https://host:443} cannot become two issuers for one deployment.
     *
     * @throws IllegalStateException when the deployment is not configured well enough to name one
     */
    String issuer();

}
