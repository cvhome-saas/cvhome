package com.asrevo.cvhome.tenancy.commons.dto;

/** Whether an organization may be used. Suspending one suspends the console for every store it owns. */
public enum OrgStatus {

    ACTIVE,

    /** Closed by an operator — non-payment, abuse, a dispute. Reversible. */
    SUSPENDED,

    /** The organization has left. Terminal in practice; the row stays for the same reasons a deleted store's does. */
    CLOSED;

    public boolean operable() {
        return this == ACTIVE;
    }

}
