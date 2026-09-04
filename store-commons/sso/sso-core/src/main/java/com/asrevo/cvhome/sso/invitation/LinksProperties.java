package com.asrevo.cvhome.sso.invitation;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * One-time link policy, and where the links point.
 *
 * <p>
 * The three page settings default to uaa's own origin and uaa's own paths, so a deployment that says nothing
 * keeps exactly the behaviour it had. They exist because the pages moved: the seller console renders a merchant's
 * invitation and password-reset pages now, and a link built on the issuer would land a merchant on the identity
 * server instead — a page that exists but is not theirs.
 * </p>
 *
 * @param invitationValidity  how long an invitation link works
 * @param resetValidity       how long a password-reset link works
 * @param logLinks            whether the logging consumer prints the link itself. Local only: the link is a
 *                            credential, and a log that carries it grants the account to anyone who can read it
 * @param baseUrl             absolute origin the links point at; empty means the pinned issuer. Configured rather
 *                            than read off a request because a link is built while an administrator is calling an
 *                            API, and that request's host is the administrator's, not the recipient's
 * @param invitationPage      path an invitation link lands on
 * @param resetPasswordPage   path a password-reset link lands on
 */
@ConfigurationProperties(prefix = "com.asrevo.cvhome.uaa.links")
public record LinksProperties(@DefaultValue("P7D") Duration invitationValidity, @DefaultValue("PT1H") Duration resetValidity,
                              @DefaultValue("false") boolean logLinks, @DefaultValue("") String baseUrl,
                              @DefaultValue("/accept-invitation") String invitationPage,
                              @DefaultValue("/reset-password") String resetPasswordPage) {

    /*
     * Named explicitly because the convenience constructor below makes this record ambiguous to bind: with two
     * constructors Boot will not guess, and the whole application context fails to start with a bare
     * NoSuchMethodException that says nothing about configuration properties.
     */
    @ConstructorBinding
    public LinksProperties {
    }

    /** Policy only, with the links left on uaa's own origin and pages — what every deployment had before. */
    public LinksProperties(Duration invitationValidity, Duration resetValidity, boolean logLinks) {
        this(invitationValidity, resetValidity, logLinks, "", "/accept-invitation", "/reset-password");
    }

}
