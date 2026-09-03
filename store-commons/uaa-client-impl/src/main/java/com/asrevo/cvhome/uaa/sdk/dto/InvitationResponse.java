package com.asrevo.cvhome.uaa.sdk.dto;

import java.time.Instant;

/**
 * What an invitation answers: the account that now exists, and the link that will let its owner in.
 *
 * @param link the one-time URL — the only readable copy, never logged
 */
public record InvitationResponse(UserDto user, String link, Instant expiresAt) {

    public IssuedLink issuedLink() {
        return new IssuedLink(link, expiresAt);
    }

}
