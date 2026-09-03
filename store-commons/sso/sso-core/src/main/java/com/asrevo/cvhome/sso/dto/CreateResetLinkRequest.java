package com.asrevo.cvhome.sso.dto;

/**
 * @param revokeSessions end the account's sessions and tokens now, rather than when the link is used — the choice
 *                       for an account whose password may already be in the wrong hands
 */
public record CreateResetLinkRequest(boolean revokeSessions) {
}
