package com.asrevo.cvhome.uaa.sdk.dto;

import java.time.Instant;

/**
 * A one-time link, answered once and never again.
 *
 * <p>
 * uaa stores only the token's hash, so this is the single moment the link exists in readable form: a caller that
 * drops it must issue a new one. It is also why nothing here should be logged — the link is a credential until it
 * is spent.
 * </p>
 */
public record IssuedLink(String link, Instant expiresAt) {
}
