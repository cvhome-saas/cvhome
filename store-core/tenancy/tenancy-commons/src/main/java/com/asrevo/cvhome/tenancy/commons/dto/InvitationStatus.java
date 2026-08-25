package com.asrevo.cvhome.tenancy.commons.dto;

/** Where an invitation to join an organization has got to. */
public enum InvitationStatus {

    PENDING,
    ACCEPTED,
    REVOKED,

    /**
     * Past its expiry. Recorded rather than deleted so that "the link stopped working" has an answer, and set
     * lazily when the invitation is next looked at — there is no job whose only purpose is to relabel rows.
     */
    EXPIRED

}
