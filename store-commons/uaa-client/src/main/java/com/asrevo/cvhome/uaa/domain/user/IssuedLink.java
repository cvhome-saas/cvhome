package com.asrevo.cvhome.uaa.domain.user;

import java.io.Serializable;
import java.time.Instant;

/**
 * A one-time link and the account it belongs to.
 *
 * <p>
 * The link is a credential until it is spent: uaa keeps only its hash, so this is the one moment it exists in
 * readable form. Pass it to the person who needs it and let it go — do not log it, store it, or return it twice.
 * </p>
 *
 * @param user the account, which for an invitation has just been created
 */
public record IssuedLink(ReadableUser user, String link, Instant expiresAt) implements Serializable {
}
