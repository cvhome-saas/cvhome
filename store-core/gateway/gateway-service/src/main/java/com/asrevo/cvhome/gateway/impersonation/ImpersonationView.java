package com.asrevo.cvhome.gateway.impersonation;

import java.time.Instant;

/**
 * The impersonation as the console sees it — on {@code auth/me}, so a reload keeps the banner.
 *
 * @param actingAs  the merchant's username, for the banner
 * @param targetId  the merchant's uaa account id
 * @param reason    what the operator said this was for
 * @param expiresAt when the gateway hands the session back to the operator, whatever else happens
 */
public record ImpersonationView(String actingAs, String targetId, String reason, Instant expiresAt) {
}
