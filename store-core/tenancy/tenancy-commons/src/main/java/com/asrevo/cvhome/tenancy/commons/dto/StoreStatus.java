package com.asrevo.cvhome.tenancy.commons.dto;

/**
 * Whether a store may be used, which is a different question from how far it got being built.
 *
 * <p>
 * {@link ProvisioningState} is the machine's answer — did the pod create succeed. This is the operator's: an
 * ACTIVE store that failed provisioning is broken, and a SUSPENDED store that provisioned perfectly is
 * deliberately closed. Conflating them would mean an operator could not suspend a store that was still building.
 * </p>
 */
public enum StoreStatus {

    ACTIVE,

    /** Closed by an operator. Reversible, and the data is untouched. */
    SUSPENDED,

    /** Retired by its owner. Reversible in principle, but not offered in the console. */
    ARCHIVED,

    /**
     * Soft-deleted. The row stays because billing holds a subscription against this id and the pod registry holds
     * a placement; removing it would orphan both and lose the audit trail of a store that once existed.
     */
    DELETED;

    /** Whether the console may be used against this store. */
    public boolean operable() {
        return this == ACTIVE;
    }

}
