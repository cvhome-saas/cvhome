package com.asrevo.cvhome.uaa.invitation;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * One-time link policy.
 *
 * @param invitationValidity how long an invitation link works
 * @param resetValidity      how long a password-reset link works
 * @param logLinks           whether the logging consumer prints the link itself. Local only: the link is a
 *                           credential, and a log that carries it grants the account to anyone who can read it
 */
@ConfigurationProperties(prefix = "com.asrevo.cvhome.uaa.links")
public record LinksProperties(@DefaultValue("P7D") Duration invitationValidity, @DefaultValue("PT1H") Duration resetValidity,
                              @DefaultValue("false") boolean logLinks) {
}
