package com.asrevo.cvhome.sso.idp;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * A brokered login that matched a local account by email and is waiting for that account's password. Kept in the
 * session between the failed OAuth2 login and the confirmation call; nothing in it is a credential.
 */
public record PendingLink(UUID providerId, String providerAlias, String providerName, String subject, String email,
                          UUID userId, String username) implements Serializable {

    public static final String SESSION_KEY = "uaa.pendingLink";

    @Serial
    private static final long serialVersionUID = 1L;

}
