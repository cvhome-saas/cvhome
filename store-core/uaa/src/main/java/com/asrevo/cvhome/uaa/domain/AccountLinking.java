package com.asrevo.cvhome.uaa.domain;

/** What happens when a brokered login's email matches an existing local account that has no link yet. */
public enum AccountLinking {
    /** Link silently — only when the provider vouches for the email. Falls back to CONFIRM otherwise. */
    LINK,
    /** Ask the person for their local password once, then link. */
    CONFIRM,
    /** Refuse: the account must be linked from its own settings. */
    REJECT
}
