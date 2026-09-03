package com.asrevo.cvhome.uaa.sdk.dto;

/**
 * @param revokeSessions end the account's sessions and tokens now, rather than leaving them alive until the new
 *                       password is set. What an incident wants; not what a routine reset needs.
 */
public record CreateResetLinkRequest(boolean revokeSessions) {
}
